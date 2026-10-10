package com.rei.examenbackend.dto.profile;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ProfileAnalyticsResponse {
    private Double overallAverageScore;
    private Double overallMood;
    private List<CategoryBreakdown> categories;
    private List<WeeklyTrendPoint> weeklyTrend;
    private AdvancedScoreDetails advancedScore;
    private List<AnomalySignal> anomalies;
    private QualityMetrics qualityMetrics;

    @Data
    @Builder
    public static class CategoryBreakdown {
        private Long categoryId;
        private String categoryName;
        private Long answers;
        private Double averageScore;
        private Double yesRate;
    }

    public record WeeklyTrendPoint(LocalDate weekStart, long sessions, Double averageMood) {}
}
