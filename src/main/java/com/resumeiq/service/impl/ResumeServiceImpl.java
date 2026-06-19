package com.resumeiq.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeiq.dto.AnalysisResultDto;
import com.resumeiq.dto.DashboardStatsResponse;
import com.resumeiq.dto.RegisterRequest;
import com.resumeiq.entity.*;
import com.resumeiq.exception.CustomException;
import com.resumeiq.repository.*;
import com.resumeiq.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service implementation for Resume processing, AI calculations, and analytics caching.
 */
@Service
public class ResumeServiceImpl implements ResumeService {
    private static final Logger logger = LoggerFactory.getLogger(ResumeServiceImpl.class);

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final PDFService pdfService;
    private final GeminiService geminiService;
    private final SkillExtractionService skillExtractionService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public ResumeServiceImpl(ResumeRepository resumeRepository,
                             ResumeAnalysisRepository resumeAnalysisRepository,
                             UserRepository userRepository,
                             StorageService storageService,
                             PDFService pdfService,
                             GeminiService geminiService,
                             SkillExtractionService skillExtractionService,
                             UserService userService,
                             ObjectMapper objectMapper) {
        this.resumeRepository = resumeRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.pdfService = pdfService;
        this.geminiService = geminiService;
        this.skillExtractionService = skillExtractionService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Resume uploadResume(MultipartFile file, String email) {
        logger.info("Uploading resume for user: {}", email);
        User user = getOrCreateUser(email);

        String savedPath = storageService.storeFile(file, "resumes/" + user.getId());
        String extractedText;
        try {
            extractedText = pdfService.extractText(file.getInputStream());
        } catch (IOException e) {
            throw new CustomException("Failed to read upload input stream: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        Resume resume = new Resume();
        resume.setFileName(file.getOriginalFilename());
        resume.setFileType(file.getContentType());
        resume.setFileSize(file.getSize());
        resume.setStoragePath(savedPath);
        resume.setExtractedText(extractedText);
        resume.setUser(user);

        Resume savedResume = resumeRepository.save(resume);
        userService.logActivity(email, "UPLOAD", "Uploaded file: " + file.getOriginalFilename(), "0.0.0.0");

        return savedResume;
    }

    @Override
    @Transactional
    @CacheEvict(value = "dashboardStats", key = "#email")
    public AnalysisResultDto analyzeResume(Long resumeId, String jobDescription, String email) {
        logger.info("Performing ATS analysis for resume ID: {}", resumeId);
        User user = getOrCreateUser(email);

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new CustomException("Resume not found", HttpStatus.NOT_FOUND));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new CustomException("Access Denied to this resume resource", HttpStatus.FORBIDDEN);
        }

        // Trigger Async parallel requests to Gemini API
        CompletableFuture<String> analysisFuture = geminiService.analyzeResumeAsync(resume.getExtractedText(), jobDescription, user.getId());
        CompletableFuture<String> questionsFuture = geminiService.generateInterviewQuestionsAsync(resume.getExtractedText(), user.getId());
        CompletableFuture<String> roadmapFuture = geminiService.generateCareerRoadmapAsync(resume.getExtractedText(), user.getId());

        // Wait for all to finish in parallel
        CompletableFuture.allOf(analysisFuture, questionsFuture, roadmapFuture).join();

        String analysisJson;
        String questionsJson;
        String roadmapJson;
        try {
            analysisJson = analysisFuture.get();
            questionsJson = questionsFuture.get();
            roadmapJson = roadmapFuture.get();
        } catch (Exception e) {
            throw new CustomException("AI response resolution failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Parse and build entity structures
        try {
            AnalysisResultDto tempDto = objectMapper.readValue(analysisJson, AnalysisResultDto.class);

            // Persist ResumeAnalysis
            ResumeAnalysis analysis = new ResumeAnalysis();
            analysis.setResume(resume);
            analysis.setAtsScore(tempDto.getAtsScore());
            analysis.setFormattingScore(tempDto.getAtsBreakdown().getFormatting());
            analysis.setKeywordsScore(tempDto.getAtsBreakdown().getKeywords());
            analysis.setExperienceScore(tempDto.getAtsBreakdown().getExperience());
            analysis.setEducationScore(tempDto.getAtsBreakdown().getEducation());
            analysis.setOverallFeedback(tempDto.getOverallFeedback());
            analysis.setCandidateName(tempDto.getCandidateName());
            analysis.setExperienceLevel(tempDto.getExperienceLevel());
            
            // Format arrays to database fields
            analysis.setTopSkills(String.join(",", tempDto.getTopSkills()));
            analysis.setStrengths(String.join(",", tempDto.getStrengths()));
            analysis.setMissingSkills(objectMapper.writeValueAsString(tempDto.getMissingSkills()));
            analysis.setAiSuggestions(objectMapper.writeValueAsString(tempDto.getAiSuggestions()));

            // Parse and set Interview Questions
            List<AnalysisResultDto.InterviewQuestionDto> qDtos = objectMapper.readValue(
                    questionsJson, new TypeReference<List<AnalysisResultDto.InterviewQuestionDto>>() {});
            List<InterviewQuestion> questions = qDtos.stream().map(qDto -> {
                InterviewQuestion question = new InterviewQuestion();
                question.setQuestion(qDto.getQuestion());
                question.setType(qDto.getType());
                question.setTip(qDto.getTip());
                question.setResumeAnalysis(analysis);
                return question;
            }).collect(Collectors.toList());
            analysis.setInterviewQuestions(questions);

            // Set Career Recommendation Roadmap
            CareerRecommendation recommendation = new CareerRecommendation();
            recommendation.setRoadmap(roadmapJson);
            recommendation.setResumeAnalysis(analysis);
            analysis.setCareerRecommendation(recommendation);

            ResumeAnalysis savedAnalysis = resumeAnalysisRepository.save(analysis);
            userService.logActivity(email, "ANALYZE", "Analyzed resume: " + resume.getFileName(), "0.0.0.0");

            // Map and return final DTO matching React UI
            return mapToDto(savedAnalysis);
        } catch (Exception e) {
            logger.error("Failed to parse and map AI responses: {}", e.getMessage(), e);
            throw new CustomException("Failed to finalize resume analysis: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public AnalysisResultDto analyzeWithJdDirect(MultipartFile file, String jobDescription, String email) {
        // Upload & parse text
        Resume resume = uploadResume(file, email);
        // Analyze
        return analyzeResume(resume.getId(), jobDescription, email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalysisResultDto.InterviewQuestionDto> generateInterviewQuestions(Long analysisId, String email) {
        ResumeAnalysis analysis = resumeAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new CustomException("Analysis not found", HttpStatus.NOT_FOUND));

        return analysis.getInterviewQuestions().stream().map(q -> 
            AnalysisResultDto.InterviewQuestionDto.builder()
                    .question(q.getQuestion())
                    .type(q.getType())
                    .tip(q.getTip())
                    .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public String generateCareerRoadmap(Long analysisId, String email) {
        ResumeAnalysis analysis = resumeAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new CustomException("Analysis not found", HttpStatus.NOT_FOUND));
        if (analysis.getCareerRecommendation() != null) {
            return analysis.getCareerRecommendation().getRoadmap();
        }
        return "{}";
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalysisResultDto> getUserAnalysisHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        List<ResumeAnalysis> analyses = resumeAnalysisRepository.findByUserOrderByCreatedAtDesc(user);
        return analyses.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dashboardStats", key = "#email")
    public DashboardStatsResponse getUserDashboardStats(String email) {
        logger.info("Computing fresh dashboard statistics for: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        long totalAnalyses = resumeAnalysisRepository.countByUser(user);
        Double averageAts = resumeAnalysisRepository.averageAtsScoreByUser(user);
        double averageScore = averageAts != null ? Math.round(averageAts * 10.0) / 10.0 : 0.0;

        List<ResumeAnalysis> analyses = resumeAnalysisRepository.findByUserOrderByCreatedAtDesc(user);

        // Calculate Top Skills frequency
        Map<String, Integer> skillCounts = new HashMap<>();
        for (ResumeAnalysis ra : analyses) {
            if (ra.getTopSkills() != null && !ra.getTopSkills().trim().isEmpty()) {
                String[] skills = ra.getTopSkills().split(",");
                for (String skill : skills) {
                    String trimmedSkill = skill.trim();
                    skillCounts.put(trimmedSkill, skillCounts.getOrDefault(trimmedSkill, 0) + 1);
                }
            }
        }

        List<String> topSkills = skillCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Calculate User Activity Trends (last 7 days)
        Map<String, Long> dateCounts = new TreeMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        for (ResumeAnalysis ra : analyses) {
            if (ra.getCreatedAt().isAfter(sevenDaysAgo)) {
                String dateStr = ra.getCreatedAt().format(formatter);
                dateCounts.put(dateStr, dateCounts.getOrDefault(dateStr, 0L) + 1);
            }
        }

        // Fill in missing dates with zero count
        for (int i = 0; i < 7; i++) {
            String dateStr = LocalDateTime.now().minusDays(i).format(formatter);
            dateCounts.putIfAbsent(dateStr, 0L);
        }

        List<DashboardStatsResponse.ActivityTrend> trends = dateCounts.entrySet().stream()
                .map(e -> new DashboardStatsResponse.ActivityTrend(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        // Extract recent uploads
        List<Resume> resumes = resumeRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .limit(5)
                .collect(Collectors.toList());

        List<DashboardStatsResponse.RecentUploadDto> recentUploads = resumes.stream().map(r -> {
            ResumeAnalysis associatedAnalysis = resumeAnalysisRepository.findByResumeId(r.getId()).orElse(null);
            Integer score = associatedAnalysis != null ? associatedAnalysis.getAtsScore() : null;

            return DashboardStatsResponse.RecentUploadDto.builder()
                    .id(r.getId())
                    .fileName(r.getFileName())
                    .fileSize(r.getFileSize())
                    .uploadedAt(r.getCreatedAt())
                    .atsScore(score)
                    .build();
        }).collect(Collectors.toList());

        return DashboardStatsResponse.builder()
                .totalAnalyses(totalAnalyses)
                .averageAtsScore(averageScore)
                .topSkills(topSkills)
                .userActivityTrends(trends)
                .recentUploads(recentUploads)
                .build();
    }

    private AnalysisResultDto mapToDto(ResumeAnalysis entity) {
        try {
            List<String> skills = entity.getTopSkills() != null && !entity.getTopSkills().isEmpty()
                    ? Arrays.asList(entity.getTopSkills().split(","))
                    : Collections.emptyList();

            List<String> strengths = entity.getStrengths() != null && !entity.getStrengths().isEmpty()
                    ? Arrays.asList(entity.getStrengths().split(","))
                    : Collections.emptyList();

            List<AnalysisResultDto.MissingSkill> missing = entity.getMissingSkills() != null
                    ? objectMapper.readValue(entity.getMissingSkills(), new TypeReference<List<AnalysisResultDto.MissingSkill>>() {})
                    : Collections.emptyList();

            List<AnalysisResultDto.AiSuggestion> suggestions = entity.getAiSuggestions() != null
                    ? objectMapper.readValue(entity.getAiSuggestions(), new TypeReference<List<AnalysisResultDto.AiSuggestion>>() {})
                    : Collections.emptyList();

            List<AnalysisResultDto.InterviewQuestionDto> questions = entity.getInterviewQuestions().stream().map(q ->
                    new AnalysisResultDto.InterviewQuestionDto(q.getQuestion(), q.getType(), q.getTip())
            ).collect(Collectors.toList());

            return AnalysisResultDto.builder()
                    .candidateName(entity.getCandidateName())
                    .experienceLevel(entity.getExperienceLevel())
                    .atsScore(entity.getAtsScore())
                    .atsBreakdown(AnalysisResultDto.AtsBreakdown.builder()
                            .formatting(entity.getFormattingScore())
                            .keywords(entity.getKeywordsScore())
                            .experience(entity.getExperienceScore())
                            .education(entity.getEducationScore())
                            .build())
                    .topSkills(skills)
                    .strengths(strengths)
                    .missingSkills(missing)
                    .aiSuggestions(suggestions)
                    .interviewQuestions(questions)
                    .overallFeedback(entity.getOverallFeedback())
                    .build();
        } catch (Exception e) {
            logger.error("Error mapping ResumeAnalysis entity to DTO", e);
            throw new CustomException("Error mapping analysis results", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private User getOrCreateUser(String email) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    if ("guest@resumeiq.com".equals(email)) {
                        RegisterRequest req = new RegisterRequest();
                        req.setUsername("guest");
                        req.setEmail("guest@resumeiq.com");
                        req.setPassword("guestPassword123");
                        return userService.registerUser(req);
                    }
                    throw new CustomException("User not found", HttpStatus.NOT_FOUND);
                });
    }
}
