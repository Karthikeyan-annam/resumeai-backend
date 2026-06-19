package com.resumeiq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing dashboard usage analytics and statistics.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private Long totalAnalyses;
    private Double averageAtsScore;
    
    @Builder.Default
    private List<String> topSkills = new ArrayList<>();
    
    @Builder.Default
    private List<ActivityTrend> userActivityTrends = new ArrayList<>();
    
    @Builder.Default
    private List<RecentUploadDto> recentUploads = new ArrayList<>();

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActivityTrend {
        private String date; // YYYY-MM-DD
        private Long count;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentUploadDto {
        private Long id;
        private String fileName;
        private Long fileSize;
        private LocalDateTime uploadedAt;
        private Integer atsScore;
    }
}
