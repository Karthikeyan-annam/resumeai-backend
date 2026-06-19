package com.resumeiq.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO representing user profile data and subscription status.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private String planType; // FREE, PRO, TEAM
    private String subscriptionStatus; // ACTIVE, EXPIRED, CANCELLED
}
