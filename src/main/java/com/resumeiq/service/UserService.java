package com.resumeiq.service;

import com.resumeiq.dto.*;
import com.resumeiq.entity.User;

/**
 * Service interface for User registration, login, profile updates, and session management.
 */
public interface UserService {

    /**
     * Registers a new user and provisions a free subscription plan.
     *
     * @param request user registration details
     * @return the registered User entity
     */
    User registerUser(RegisterRequest request);

    /**
     * Authenticates a user and issues access/refresh tokens.
     *
     * @param request login details
     * @return payload containing JWT tokens and profile metadata
     */
    JwtResponse authenticateUser(AuthRequest request);

    /**
     * Re-issues a fresh access token using a valid refresh token.
     *
     * @param request refresh token body
     * @return access and refresh token payload
     */
    TokenRefreshResponse refreshToken(TokenRefreshRequest request);

    /**
     * Logs out the user by revoking their refresh token.
     *
     * @param email the user's email
     */
    void logoutUser(String email);

    /**
     * Retrieves the current user's profile details.
     *
     * @param email the user's email
     * @return user profile details DTO
     */
    UserProfileResponse getUserProfile(String email);

    /**
     * Changes the current user's password.
     *
     * @param email the user's email
     * @param request old and new password payload
     */
    void changePassword(String email, PasswordChangeRequest request);

    /**
     * Registers a user activity log event.
     *
     * @param email the user's email
     * @param activityType the type of activity
     * @param details extra details of the activity
     * @param ipAddress client IP address
     */
    void logActivity(String email, String activityType, String details, String ipAddress);
}
