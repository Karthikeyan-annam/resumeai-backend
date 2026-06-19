package com.resumeiq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Combined DTO representing the complete ATS analysis and AI suggestions output.
 * Fully aligned with React frontend state expectations.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResultDto {

    private String candidateName;
    private String experienceLevel;
    private Integer atsScore;
    private AtsBreakdown atsBreakdown;
    
    @Builder.Default
    private List<String> topSkills = new ArrayList<>();
    
    @Builder.Default
    private List<String> strengths = new ArrayList<>();
    
    @Builder.Default
    private List<MissingSkill> missingSkills = new ArrayList<>();
    
    @Builder.Default
    private List<AiSuggestion> aiSuggestions = new ArrayList<>();
    
    @Builder.Default
    private List<InterviewQuestionDto> interviewQuestions = new ArrayList<>();
    
    private String overallFeedback;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AtsBreakdown {
        private Integer formatting;  // 0-25
        private Integer keywords;    // 0-25
        private Integer experience;  // 0-25
        private Integer education;   // 0-25
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MissingSkill {
        private String skill;
        private String importance; // High, Medium, Low
        private String reason;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiSuggestion {
        private String category;
        private String impact; // High, Medium, Low
        private String suggestion;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterviewQuestionDto {
        private String question;
        private String type; // Technical, Behavioral, Situational
        private String tip;
    }
}
