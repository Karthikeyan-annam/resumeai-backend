package com.resumeiq.service.impl;

import com.resumeiq.service.SkillExtractionService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service implementation for extracting and matching professional skills using an internal lookup catalog.
 */
@Service
public class SkillExtractionServiceImpl implements SkillExtractionService {

    // Internal catalog of standard skills grouped by category
    private static final Map<String, List<String>> SKILL_DICTIONARY = new LinkedHashMap<>();

    static {
        SKILL_DICTIONARY.put("Languages", Arrays.asList(
                "Java", "Python", "JavaScript", "TypeScript", "C++", "C#", "Ruby", "Go", "Rust", "Kotlin", "Swift", "PHP", "SQL", "HTML", "CSS"
        ));
        SKILL_DICTIONARY.put("Frameworks & Libraries", Arrays.asList(
                "Spring Boot", "Spring Framework", "Spring", "React", "Angular", "Vue", "Next.js", "Node.js", "Express.js", "Express", "Django", "Flask", "Ruby on Rails", "Rails", "Hibernate", "JPA", "MapStruct", "Lombok", "Tailwind CSS", "Tailwind", "Bootstrap"
        ));
        SKILL_DICTIONARY.put("Databases", Arrays.asList(
                "PostgreSQL", "Postgres", "MySQL", "MongoDB", "Redis", "SQLite", "Oracle", "Cassandra", "Elasticsearch", "Supabase", "Firebase"
        ));
        SKILL_DICTIONARY.put("DevOps & Cloud", Arrays.asList(
                "Docker", "Kubernetes", "Jenkins", "Git", "GitHub", "GitLab", "AWS", "Amazon Web Services", "Azure", "Google Cloud", "GCP", "CI/CD", "Maven", "Gradle", "Flyway", "OpenAPI", "Swagger"
        ));
        SKILL_DICTIONARY.put("Methodologies & Architectures", Arrays.asList(
                "Agile", "Scrum", "Project Management", "Teamwork", "Communication", "Problem Solving", "Leadership", "Microservices", "REST APIs", "RESTful", "System Design", "Data Structures", "Algorithms", "Machine Learning", "Artificial Intelligence", "AI", "NLP"
        ));
    }

    @Override
    public List<String> extractSkills(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> matchedSkills = new ArrayList<>();
        for (List<String> skills : SKILL_DICTIONARY.values()) {
            for (String skill : skills) {
                if (containsSkillWord(text, skill)) {
                    matchedSkills.add(skill);
                }
            }
        }
        return matchedSkills;
    }

    @Override
    public Map<String, List<String>> categorizeSkills(List<String> skills) {
        Map<String, List<String>> categorized = new LinkedHashMap<>();
        for (String skill : skills) {
            String category = findCategory(skill);
            categorized.computeIfAbsent(category, k -> new ArrayList<>()).add(skill);
        }
        return categorized;
    }

    @Override
    public Map<String, Object> analyzeJobDescriptionMatch(String resumeText, String jdText) {
        List<String> resumeSkills = extractSkills(resumeText);
        List<String> jdSkills = extractSkills(jdText);

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String skill : jdSkills) {
            boolean hasSkill = false;
            for (String rSkill : resumeSkills) {
                if (rSkill.equalsIgnoreCase(skill)) {
                    hasSkill = true;
                    break;
                }
            }
            if (hasSkill) {
                matched.add(skill);
            } else {
                missing.add(skill);
            }
        }

        double matchPercentage = 0.0;
        if (!jdSkills.isEmpty()) {
            matchPercentage = ((double) matched.size() / jdSkills.size()) * 100;
        } else if (!resumeSkills.isEmpty()) {
            matchPercentage = 100.0; // Default when no skills required in JD but some are present in resume
        }

        Map<String, Object> result = new HashMap<>();
        result.put("matchPercentage", Math.round(matchPercentage * 100.0) / 100.0);
        result.put("matchedSkills", matched);
        result.put("missingSkills", missing);
        result.put("resumeSkills", resumeSkills);
        result.put("jdSkills", jdSkills);

        return result;
    }

    private boolean containsSkillWord(String text, String skill) {
        String lowerText = text.toLowerCase();
        String lowerSkill = skill.toLowerCase();

        int index = lowerText.indexOf(lowerSkill);
        while (index != -1) {
            boolean startOk = (index == 0) || !Character.isLetterOrDigit(lowerText.charAt(index - 1));
            int endPos = index + lowerSkill.length();
            boolean endOk = (endPos == lowerText.length()) || !Character.isLetterOrDigit(lowerText.charAt(endPos));

            if (startOk && endOk) {
                return true;
            }
            index = lowerText.indexOf(lowerSkill, index + 1);
        }
        return false;
    }

    private String findCategory(String skill) {
        for (Map.Entry<String, List<String>> entry : SKILL_DICTIONARY.entrySet()) {
            if (entry.getValue().contains(skill)) {
                return entry.getKey();
            }
        }
        return "Others";
    }
}
