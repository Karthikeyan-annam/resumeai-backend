package com.resumeiq.service;

import java.util.concurrent.CompletableFuture;

/**
 * Service interface for communicating with the Google Gemini API to analyze resumes,
 * suggest improvements, generate interview questions, and build career roadmaps.
 */
public interface GeminiService {

    /**
     * Sends the resume and target JD to Gemini to perform full ATS scoring and skill analysis.
     *
     * @param resumeText the extracted text from the resume
     * @param jobDescription the target job description (optional)
     * @param userId the ID of the user requesting the analysis (for token usage tracking)
     * @return a future resolving to the JSON analysis string
     */
    CompletableFuture<String> analyzeResumeAsync(String resumeText, String jobDescription, Long userId);

    /**
     * Generates interview questions based on the candidate's resume.
     *
     * @param resumeText the extracted text from the resume
     * @param userId the ID of the user requesting the questions (for token usage tracking)
     * @return a future resolving to the JSON array of questions
     */
    CompletableFuture<String> generateInterviewQuestionsAsync(String resumeText, Long userId);

    /**
     * Generates career roadmap recommendations based on the candidate's resume.
     *
     * @param resumeText the extracted text from the resume
     * @param userId the ID of the user requesting the roadmap (for token usage tracking)
     * @return a future resolving to the JSON career roadmap recommendations
     */
    CompletableFuture<String> generateCareerRoadmapAsync(String resumeText, Long userId);
}
