package com.resumeiq.service;

import com.resumeiq.service.impl.SkillExtractionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the local SkillExtractionService algorithm.
 */
public class SkillExtractionServiceImplTest {

    private SkillExtractionService skillExtractionService;

    @BeforeEach
    public void setUp() {
        this.skillExtractionService = new SkillExtractionServiceImpl();
    }

    @Test
    public void testExtractSkills_Success() {
        String resumeText = "Experienced developer working with Java, Spring Boot, PostgreSQL and Docker. Solid understanding of Git.";
        List<String> skills = skillExtractionService.extractSkills(resumeText);

        assertTrue(skills.contains("Java"));
        assertTrue(skills.contains("Spring Boot"));
        assertTrue(skills.contains("PostgreSQL"));
        assertTrue(skills.contains("Docker"));
        assertTrue(skills.contains("Git"));
        assertFalse(skills.contains("Python")); // Not present in text
    }

    @Test
    public void testExtractSkills_BoundaryCheck() {
        String textWithFalseMatches = "I love Google and Django frameworks. Going to write script files.";
        List<String> skills = skillExtractionService.extractSkills(textWithFalseMatches);

        // "Go" should not be falsely extracted from "Google" or "Going"
        assertFalse(skills.contains("Go"));
        assertTrue(skills.contains("Django"));
    }

    @Test
    public void testCategorizeSkills() {
        List<String> skills = Arrays.asList("Java", "React", "PostgreSQL", "Docker", "Agile");
        Map<String, List<String>> categorized = skillExtractionService.categorizeSkills(skills);

        assertNotNull(categorized);
        assertTrue(categorized.containsKey("Languages"));
        assertTrue(categorized.get("Languages").contains("Java"));
        assertTrue(categorized.get("Frameworks & Libraries").contains("React"));
    }

    @Test
    public void testAnalyzeJobDescriptionMatch() {
        String resumeText = "Skillset includes: Java, Spring Boot, Git, SQL.";
        String jdText = "Required: Java, Python, Spring Boot, AWS, Docker.";

        Map<String, Object> analysis = skillExtractionService.analyzeJobDescriptionMatch(resumeText, jdText);

        assertNotNull(analysis);
        assertEquals(40.0, (Double) analysis.get("matchPercentage")); // 2 out of 5 skills matched (Java, Spring Boot)
        
        List<String> missing = (List<String>) analysis.get("missingSkills");
        assertTrue(missing.contains("Python"));
        assertTrue(missing.contains("AWS"));
        assertTrue(missing.contains("Docker"));
    }
}
