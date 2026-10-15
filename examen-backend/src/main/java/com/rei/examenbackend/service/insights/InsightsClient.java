package com.rei.examenbackend.service.insights;

import com.rei.examenbackend.dto.insights.FollowUpPromptResponse;
import com.rei.examenbackend.dto.insights.InsightsSummaryResponse;
import com.rei.examenbackend.dto.insights.QuestionSuggestionResponse;
import com.rei.examenbackend.dto.insights.SemanticTagResponse;
import com.rei.examenbackend.dto.insights.SessionInsightResponse;
import com.rei.examenbackend.dto.insights.WeeklyNarrativeResponse;

import java.util.List;

public interface InsightsClient {
    InsightsSummaryResponse buildSummary(InsightsSummaryContext context);

    SessionInsightResponse buildSessionInsight(SessionInsightContext context);

    QuestionSuggestionResponse buildQuestionSuggestions(QuestionSuggestionContext context);

    List<FollowUpPromptResponse> buildFollowUpPrompts(QuestionSuggestionContext context);

    List<SemanticTagResponse> extractSemanticTags(SemanticTagContext context);

    WeeklyNarrativeResponse buildWeeklyNarrative(WeeklyNarrativeContext context);
}
