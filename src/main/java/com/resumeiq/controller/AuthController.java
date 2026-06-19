package com.resumeiq.controller;

import com.resumeiq.dto.*;
import com.resumeiq.entity.User;
import com.resumeiq.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller handling user authentication, registration, logout, and token refresh.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        User user = userService.registerUser(signUpRequest);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully!");
        response.put("userId", user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody AuthRequest loginRequest) {
        JwtResponse jwtResponse = userService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshJwtToken(@Valid @RequestBody TokenRefreshRequest refreshRequest) {
        TokenRefreshResponse tokenRefreshResponse = userService.refreshToken(refreshRequest);
        return ResponseEntity.ok(tokenRefreshResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(Principal principal) {
        if (principal != null) {
            userService.logoutUser(principal.getName());
            SecurityContextHolder.clearContext();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logged out successfully!");
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body("No active session found.");
    }
}
