package com.resumeiq.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity logging AI usage characteristics and cost metrics.
 */
@Entity
@Table(name = "ai_usage_logs")
@Getter
@Setter
@NoArgsConstructor
public class AiUsageLog extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "feature_used", length = 100, nullable = false)
    private String featureUsed; // ATS_ANALYSIS, INTERVIEW_PREP, ROADMAP

    @Column(name = "tokens_consumed")
    private Integer tokensConsumed = 0;
}
