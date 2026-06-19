package com.resumeiq.controller;

import com.resumeiq.dto.PasswordChangeRequest;
import com.resumeiq.dto.UserProfileResponse;
import com.resumeiq.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller managing User profile and password updates.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(Principal principal) {
        UserProfileResponse profile = userService.getUserProfile(principal.getName());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(Principal principal, @Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(principal.getName(), request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password updated successfully!");
        return ResponseEntity.ok(response);
    }
}
