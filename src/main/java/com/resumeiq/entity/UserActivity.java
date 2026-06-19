package com.resumeiq.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity storing user activities for platform analytics and logging.
 */
@Entity
@Table(name = "user_activity")
@Getter
@Setter
@NoArgsConstructor
public class UserActivity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "activity_type", length = 50, nullable = false)
    private String activityType; // LOGIN, REGISTER, UPLOAD, ANALYZE, LOGOUT

    @Column(name = "details")
    private String details;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;
}
