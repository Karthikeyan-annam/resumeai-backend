package com.resumeiq.controller;

import com.resumeiq.dto.AnalysisResultDto;
import com.resumeiq.dto.DashboardStatsResponse;
import com.resumeiq.service.ResumeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/**
 * Controller exposing endpoints for personal resume dashboard listings and stats.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final ResumeService resumeService;

    public DashboardController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @GetMapping("/history")
    public ResponseEntity<List<AnalysisResultDto>> getHistory(Principal principal) {
        List<AnalysisResultDto> history = resumeService.getUserAnalysisHistory(principal.getName());
        return ResponseEntity.ok(history);
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats(Principal principal) {
        DashboardStatsResponse stats = resumeService.getUserDashboardStats(principal.getName());
        return ResponseEntity.ok(stats);
    }
}
