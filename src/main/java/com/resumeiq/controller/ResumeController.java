package com.resumeiq.controller;

import com.resumeiq.dto.AnalysisResultDto;
import com.resumeiq.entity.Resume;
import com.resumeiq.service.ResumeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller mapping resume file uploads, AI analytical pipelines, and custom prep-work.
 */
@RestController
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping("/api/v1/resume/upload")
    public ResponseEntity<?> uploadResume(
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        Resume resume = resumeService.uploadResume(file, principal.getName());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Resume uploaded successfully!");
        response.put("resumeId", resume.getId());
        response.put("fileName", resume.getFileName());
        response.put("fileSize", resume.getFileSize());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/resume/analyze")
    public ResponseEntity<AnalysisResultDto> analyzeResume(
            @RequestParam("resumeId") Long resumeId,
            @RequestParam(value = "jobDescription", required = false) String jobDescription,
            Principal principal) {
        AnalysisResultDto analysis = resumeService.analyzeResume(resumeId, jobDescription, principal.getName());
        return ResponseEntity.ok(analysis);
    }

    // Direct upload and analyze endpoint - matching React UI axios.post('/api/analyze')
    @PostMapping(value = {"/api/analyze", "/api/v1/resume/analyze-with-jd"})
    public ResponseEntity<?> analyzeWithJdDirect(
            @RequestParam("resume") MultipartFile file,
            @RequestParam(value = "jobDescription", required = false) String jobDescription,
            Principal principal) {
        String email = (principal != null) ? principal.getName() : "guest@resumeiq.com";
        AnalysisResultDto analysis = resumeService.analyzeWithJdDirect(file, jobDescription, email);
        Map<String, Object> response = new HashMap<>();
        response.put("analysis", analysis);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/ai/interview-questions")
    public ResponseEntity<List<AnalysisResultDto.InterviewQuestionDto>> generateInterviewQuestions(
            @RequestParam("analysisId") Long analysisId,
            Principal principal) {
        List<AnalysisResultDto.InterviewQuestionDto> questions = resumeService.generateInterviewQuestions(analysisId, principal.getName());
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/api/v1/ai/suggestions")
    public ResponseEntity<String> generateCareerRoadmap(
            @RequestParam("analysisId") Long analysisId,
            Principal principal) {
        String roadmap = resumeService.generateCareerRoadmap(analysisId, principal.getName());
        return ResponseEntity.ok(roadmap);
    }
}
