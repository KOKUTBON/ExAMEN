package com.rei.examenbackend.service.insights;

import com.rei.examenbackend.model.Answer;
import com.rei.examenbackend.model.ExaminationSession;
import com.rei.examenbackend.model.User;

import java.util.List;

public record SemanticTagContext(
        User user,
        ExaminationSession session,
        List<Answer> answers
) {}
