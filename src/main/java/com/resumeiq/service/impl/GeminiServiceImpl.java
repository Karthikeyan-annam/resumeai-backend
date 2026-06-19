package com.resumeiq.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeiq.entity.AiUsageLog;
import com.resumeiq.entity.User;
import com.resumeiq.exception.CustomException;
import com.resumeiq.repository.AiUsageLogRepository;
import com.resumeiq.repository.UserRepository;
import com.resumeiq.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service implementation for interacting with the Google Gemini API.
 */
@Service
public class GeminiServiceImpl implements GeminiService {
    private static final Logger logger = LoggerFactory.getLogger(GeminiServiceImpl.class);
    private static final Logger aiLogger = LoggerFactory.getLogger("com.resumeiq.ai");

    private final RestClient restClient;
    private final AiUsageLogRepository aiUsageLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.base-url:https://generativelanguage.googleapis.com}")
    private String baseUrl;

    @Value("${gemini.api.model:gemini-1.5-flash}")
    private String model;

    public GeminiServiceImpl(AiUsageLogRepository aiUsageLogRepository,
                             UserRepository userRepository,
                             ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().build();
        this.aiUsageLogRepository = aiUsageLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Async
    public CompletableFuture<String> analyzeResumeAsync(String resumeText, String jobDescription, Long userId) {
        logger.info("Starting asynchronous resume analysis for user ID: {}", userId);
        String promptTemplate = loadPromptTemplate("ats-analysis.txt");
        String finalPrompt = promptTemplate
                .replace("{resumeText}", resumeText)
                .replace("{jobDescription}", jobDescription != null ? jobDescription : "None");

        String response = callGeminiApi(finalPrompt, "ATS_ANALYSIS", userId);
        return CompletableFuture.completedFuture(response);
    }

    @Override
    @Async
    public CompletableFuture<String> generateInterviewQuestionsAsync(String resumeText, Long userId) {
        logger.info("Starting asynchronous interview question generation for user ID: {}", userId);
        String promptTemplate = loadPromptTemplate("interview-generation.txt");
        String finalPrompt = promptTemplate.replace("{resumeText}", resumeText);

        String response = callGeminiApi(finalPrompt, "INTERVIEW_PREP", userId);
        return CompletableFuture.completedFuture(response);
    }

    @Override
    @Async
    public CompletableFuture<String> generateCareerRoadmapAsync(String resumeText, Long userId) {
        logger.info("Starting asynchronous career roadmap generation for user ID: {}", userId);
        String promptTemplate = loadPromptTemplate("career-roadmap.txt");
        String finalPrompt = promptTemplate.replace("{resumeText}", resumeText);

        String response = callGeminiApi(finalPrompt, "ROADMAP", userId);
        return CompletableFuture.completedFuture(response);
    }

    private String callGeminiApi(String prompt, String featureName, Long userId) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.error("Gemini API key is not configured");
            throw new CustomException("AI Service is temporarily unavailable (API key missing)", HttpStatus.SERVICE_UNAVAILABLE);
        }

        aiLogger.info("--- Sending Request to Gemini ---");
        aiLogger.info("Feature: {}", featureName);
        aiLogger.info("User ID: {}", userId);
        aiLogger.debug("Prompt content: {}", prompt);

        GeminiRequestBody requestBody = new GeminiRequestBody(prompt);

        try {
            String finalUrl = baseUrl + "/v1beta/models/" + model + ":generateContent?key=" + apiKey;
            String responseJson = restClient.post()
                    .uri(finalUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            aiLogger.info("--- Received Response from Gemini ---");
            aiLogger.debug("Response content: {}", responseJson);

            JsonNode rootNode = objectMapper.readTree(responseJson);
            JsonNode textNode = rootNode.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (textNode.isMissingNode()) {
                logger.error("Failed to parse text from Gemini response. Full response: {}", responseJson);
                throw new CustomException("Invalid response format received from AI provider", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            String aiResponseText = textNode.asText().trim();

            // Strip out any triple backtick wrappers markdown (e.g. ```json ... ```)
            if (aiResponseText.startsWith("```")) {
                aiResponseText = aiResponseText.substring(aiResponseText.indexOf("\n") + 1);
                if (aiResponseText.endsWith("```")) {
                    aiResponseText = aiResponseText.substring(0, aiResponseText.lastIndexOf("```")).trim();
                }
            }

            // Record Usage Analytics
            saveUsageLog(userId, featureName, prompt.length() / 4 + aiResponseText.length() / 4);

            return aiResponseText;
        } catch (Exception e) {
            logger.error("Error occurred while calling Gemini API: {}", e.getMessage(), e);
            throw new CustomException("Failed to analyze resume content using Gemini: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String loadPromptTemplate(String promptFileName) {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/" + promptFileName);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Failed to load prompt template from classpath: {}", promptFileName, e);
            throw new CustomException("Required prompt template is missing from resources", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void saveUsageLog(Long userId, String feature, int estimatedTokens) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                AiUsageLog log = new AiUsageLog();
                log.setUser(user);
                log.setFeatureUsed(feature);
                log.setTokensConsumed(estimatedTokens);
                aiUsageLogRepository.save(log);
                logger.info("Saved AI Usage log: {} tokens for feature: {}", estimatedTokens, feature);
            }
        } catch (Exception e) {
            logger.warn("Could not save AI Usage log: {}", e.getMessage());
        }
    }

    // Type-safe JSON body structure matching Gemini expectations
    private static class GeminiRequestBody {
        public List<Content> contents;
        public GenerationConfig generationConfig;

        public GeminiRequestBody(String text) {
            this.contents = List.of(new Content(List.of(new Part(text))));
            this.generationConfig = new GenerationConfig("application/json");
        }

        public static class Content {
            public List<Part> parts;
            public Content(List<Part> parts) {
                this.parts = parts;
            }
        }

        public static class Part {
            public String text;
            public Part(String text) {
                this.text = text;
            }
        }

        public static class GenerationConfig {
            public String responseMimeType;
            public GenerationConfig(String responseMimeType) {
                this.responseMimeType = responseMimeType;
            }
        }
    }
}
