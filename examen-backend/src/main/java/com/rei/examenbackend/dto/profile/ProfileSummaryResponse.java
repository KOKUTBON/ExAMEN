package com.rei.examenbackend.dto.profile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileSummaryResponse {
    private long examinationsCompleted;
    private long todosCompleted;
    private long categoriesUsed;
    private long streakDays;
    private Double spiritualProgressScore;
    private Double averageMoodLast30Days;
    private Integer todayMood;
    private long sessionsThisWeek;
    private long sessionsThisMonth;
    private boolean todayCompleted;
    private java.util.List<MoodPoint> recentMoodTrend;

    public record MoodPoint(java.time.LocalDate date, Integer mood) {}
}
