package com.rei.examenbackend.service.insights;

import com.rei.examenbackend.model.ExaminationSession;
import com.rei.examenbackend.model.GratitudeEntry;
import com.rei.examenbackend.model.InsightFeedback;
import com.rei.examenbackend.model.SemanticTag;
import com.rei.examenbackend.model.User;

import java.time.LocalDate;
import java.util.List;

public record WeeklyNarrativeContext(
        User user,
        LocalDate periodStart,
        LocalDate periodEnd,
        List<ExaminationSession> sessions,
        List<SemanticTag> recentTags,
        List<GratitudeEntry> recentGratitude,
        List<InsightFeedback> userFeedbackPreferences
) {}
