package com.resumeiq.service;

import java.util.List;
import java.util.Map;

/**
 * Service interface for local text parsing, skill identification, and JD compatibility calculations.
 */
public interface SkillExtractionService {

    /**
     * Extracts known professional skills from a text corpus.
     *
     * @param text the raw resume text
     * @return list of matched normalized skills
     */
    List<String> extractSkills(String text);

    /**
     * Group a list of flat skill strings into structured categories.
     *
     * @param skills flat list of skills
     * @return map of category to skills list
     */
    Map<String, List<String>> categorizeSkills(List<String> skills);

    /**
     * Compares skills in a resume against skills required in a JD and returns matching stats.
     *
     * @param resumeText the extracted text from the resume
     * @param jdText the job description text
     * @return a map containing match details (matchPercentage, matchedSkills, missingSkills)
     */
    Map<String, Object> analyzeJobDescriptionMatch(String resumeText, String jdText);
}
