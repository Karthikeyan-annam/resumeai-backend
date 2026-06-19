package com.resumeiq.controller;

import com.resumeiq.repository.AiUsageLogRepository;
import com.resumeiq.repository.ResumeAnalysisRepository;
import com.resumeiq.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller exposing platform analytics and admin controls.
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final AiUsageLogRepository aiUsageLogRepository;

    public AdminController(UserRepository userRepository,
                           ResumeAnalysisRepository resumeAnalysisRepository,
                           AiUsageLogRepository aiUsageLogRepository) {
        this.userRepository = userRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.aiUsageLogRepository = aiUsageLogRepository;
    }

    @GetMapping("/analytics")
    public ResponseEntity<?> getPlatformAnalytics() {
        long totalUsers = userRepository.count();
        long totalAnalyses = resumeAnalysisRepository.count();
        
        // Sum total estimated tokens
        long totalTokens = aiUsageLogRepository.findAll().stream()
                .mapToLong(log -> log.getTokensConsumed() != null ? log.getTokensConsumed() : 0L)
                .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalAnalyses", totalAnalyses);
        stats.put("totalTokensConsumed", totalTokens);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}
