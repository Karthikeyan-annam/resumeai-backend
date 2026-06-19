package com.resumeiq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for validating password change requests.
 */
@Getter
@Setter
public class PasswordChangeRequest {

    @NotBlank(message = "Current password is required")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 120, message = "New password must be at least 6 characters")
    private String newPassword;
}
