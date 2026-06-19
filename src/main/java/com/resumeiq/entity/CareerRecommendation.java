package com.resumeiq.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity storing AI-generated career recommendations and roadmap plans.
 */
@Entity
@Table(name = "career_recommendations")
@Getter
@Setter
@NoArgsConstructor
public class CareerRecommendation extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_analysis_id", nullable = false)
    private ResumeAnalysis resumeAnalysis;

    @Lob
    @Column(name = "roadmap", nullable = false, columnDefinition = "TEXT")
    private String roadmap; // Stored as JSON text
}
