package com.resumeiq.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity storing AI-powered resume analysis results and scores.
 */
@Entity
@Table(name = "resume_analysis")
@Getter
@Setter
@NoArgsConstructor
public class ResumeAnalysis extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "ats_score", nullable = false)
    private Integer atsScore;

    @Column(name = "formatting_score", nullable = false)
    private Integer formattingScore;

    @Column(name = "keywords_score", nullable = false)
    private Integer keywordsScore;

    @Column(name = "experience_score", nullable = false)
    private Integer experienceScore;

    @Column(name = "education_score", nullable = false)
    private Integer educationScore;

    @Lob
    @Column(name = "overall_feedback", columnDefinition = "TEXT")
    private String overallFeedback;

    @Column(name = "candidate_name", length = 150)
    private String candidateName;

    @Column(name = "experience_level", length = 50)
    private String experienceLevel;

    @Lob
    @Column(name = "top_skills", columnDefinition = "TEXT")
    private String topSkills; // Stored as comma-separated values

    @Lob
    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths; // Stored as comma-separated values

    @Lob
    @Column(name = "missing_skills", columnDefinition = "TEXT")
    private String missingSkills; // JSON text: [{"skill":"...", "importance":"...", "reason":"..."}]

    @Lob
    @Column(name = "ai_suggestions", columnDefinition = "TEXT")
    private String aiSuggestions; // JSON text: [{"category":"...", "impact":"...", "suggestion":"..."}]

    @OneToMany(mappedBy = "resumeAnalysis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InterviewQuestion> interviewQuestions = new ArrayList<>();

    @OneToOne(mappedBy = "resumeAnalysis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CareerRecommendation careerRecommendation;
}
