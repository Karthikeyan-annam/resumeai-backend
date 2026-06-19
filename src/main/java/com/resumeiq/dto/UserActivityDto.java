package com.resumeiq.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO representing a simplified log of user activity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityDto {
    private Long id;
    private String activityType;
    private String details;
    private String ipAddress;
    private LocalDateTime createdAt;
}
