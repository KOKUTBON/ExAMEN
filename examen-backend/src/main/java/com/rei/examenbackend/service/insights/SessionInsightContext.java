package com.rei.examenbackend.service.insights;

import com.rei.examenbackend.model.ExaminationSession;
import com.rei.examenbackend.model.User;

import java.util.List;

public record SessionInsightContext(
        User user,
        ExaminationSession session,
        Double averageFeeling,
        List<String> strongestCategories,
        List<String> needsAttentionCategories
) {}
