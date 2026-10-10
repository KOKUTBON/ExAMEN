package com.rei.examenbackend.service.insights;

import com.rei.examenbackend.model.User;

import java.time.LocalDate;
import java.util.List;

public record InsightsSummaryContext(
        User user,
        int periodDays,
        long sessionsCompleted,
        Double averageFeeling,
        LocalDate startDate,
        LocalDate endDate,
        List<SessionContextSnippet> recentSessions,
        List<String> gratitudeSnippets
) {
    public InsightsSummaryContext(User user, int periodDays, long sessionsCompleted, Double averageFeeling,
                                  LocalDate startDate, LocalDate endDate) {
        this(user, periodDays, sessionsCompleted, averageFeeling, startDate, endDate, List.of(), List.of());
    }

    public record SessionContextSnippet(
            Long sessionId,
            LocalDate date,
            Integer moodScore,
            String notes,
            List<String> reflectionSnippets
    ) {}
}
