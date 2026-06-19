package com.resumeiq.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity storing AI-generated interview preparation questions.
 */
@Entity
@Table(name = "interview_questions")
@Getter
@Setter
@NoArgsConstructor
public class InterviewQuestion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_analysis_id", nullable = false)
    private ResumeAnalysis resumeAnalysis;

    @Lob
    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "type", length = 50, nullable = false)
    private String type; // Technical, Behavioral, Situational

    @Lob
    @Column(name = "tip", columnDefinition = "TEXT")
    private String tip;
}
