package com.rei.examenbackend.service.insights;

import com.rei.examenbackend.model.User;

import java.util.List;

public record QuestionSuggestionContext(
        User user,
        String focus,
        int count,
        List<String> existingQuestions,
        List<String> recentAnswers,
        List<String> activeGoals,
        List<String> recentStruggles
) {
    public QuestionSuggestionContext(User user, String focus, int count, List<String> existingQuestions) {
        this(user, focus, count, existingQuestions, List.of(), List.of(), List.of());
    }
}
