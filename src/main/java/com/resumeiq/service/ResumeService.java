package com.resumeiq.service;

import com.resumeiq.dto.AnalysisResultDto;
import com.resumeiq.dto.DashboardStatsResponse;
import com.resumeiq.entity.Resume;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for handling resume uploads, performing analysis steps,
 * and fetching metrics for dashboard views.
 */
public interface ResumeService {

    /**
     * Uploads and parses the raw text of a resume PDF.
     *
     * @param file the resume file
     * @param email the email of the uploading user
     * @return the saved Resume entity
     */
    Resume uploadResume(MultipartFile file, String email);

    /**
     * Performs a complete ATS analysis using Gemini AI.
     *
     * @param resumeId the ID of the resume to analyze
     * @param jobDescription the target job description (optional)
     * @param email the email of the requesting user
     * @return structured ATS assessment result DTO
     */
    AnalysisResultDto analyzeResume(Long resumeId, String jobDescription, String email);

    /**
     * Uploads and analyzes a resume directly in a single request.
     * Aligned with frontend drag-and-drop workflow.
     *
     * @param file the resume file
     * @param jobDescription the target job description (optional)
     * @param email the email of the requesting user
     * @return structured ATS assessment result DTO
     */
    AnalysisResultDto analyzeWithJdDirect(MultipartFile file, String jobDescription, String email);

    /**
     * Generates interview questions asynchronously for an existing analysis.
     *
     * @param analysisId the ID of the resume analysis
     * @param email the email of the requesting user
     * @return list of generated interview questions DTO
     */
    List<AnalysisResultDto.InterviewQuestionDto> generateInterviewQuestions(Long analysisId, String email);

    /**
     * Generates career roadmap recommendations asynchronously for an existing analysis.
     *
     * @param analysisId the ID of the resume analysis
     * @param email the email of the requesting user
     * @return the generated roadmap JSON string
     */
    String generateCareerRoadmap(Long analysisId, String email);

    /**
     * Retrieves the history of analyses for a user.
     *
     * @param email the email of the user
     * @return list of analysis results DTOs
     */
    List<AnalysisResultDto> getUserAnalysisHistory(String email);

    /**
     * Retrieves cached dashboard analytics for a user.
     *
     * @param email the email of the user
     * @return the dashboard statistics response DTO
     */
    DashboardStatsResponse getUserDashboardStats(String email);
}
