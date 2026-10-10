package com.rei.examenbackend.controller;

import com.rei.examenbackend.dto.insights.FollowUpPromptResponse;
import com.rei.examenbackend.dto.insights.InsightFeedbackRequest;
import com.rei.examenbackend.dto.insights.InsightFeedbackResponse;
import com.rei.examenbackend.dto.insights.InsightsSummaryResponse;
import com.rei.examenbackend.dto.insights.QuestionSuggestionRequest;
import com.rei.examenbackend.dto.insights.QuestionSuggestionResponse;
import com.rei.examenbackend.dto.insights.SemanticTagResponse;
import com.rei.examenbackend.dto.insights.SemanticTagSummaryResponse;
import com.rei.examenbackend.dto.insights.SessionInsightRequest;
import com.rei.examenbackend.dto.insights.SessionInsightResponse;
import com.rei.examenbackend.dto.insights.WeeklyNarrativeResponse;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.dto.insights.InsightsStatusResponse;
import com.rei.examenbackend.service.insights.GeminiInsightsClient;
import com.rei.examenbackend.service.insights.InsightsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {
    private final InsightsService insightsService;
    private final GeminiInsightsClient geminiClient;

    public InsightsController(InsightsService insightsService, GeminiInsightsClient geminiClient) {
        this.insightsService = insightsService;
        this.geminiClient = geminiClient;
    }

    /**
     * Tells the client whether insights come from Gemini or from the local heuristic fallback,
     * so the UI can label generated content honestly.
     */
    @GetMapping("/status")
    public ResponseEntity<InsightsStatusResponse> status() {
        boolean gemini = geminiClient.isConfigured();
        return ResponseEntity.ok(new InsightsStatusResponse(gemini, gemini ? "gemini" : "local"));
    }

    @GetMapping("/summary")
    public ResponseEntity<InsightsSummaryResponse> summary(@AuthenticationPrincipal User user,
                                                           @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(insightsService.summary(user, days));
    }

    @PostMapping("/session")
    public ResponseEntity<SessionInsightResponse> analyzeSession(@AuthenticationPrincipal User user,
                                                                 @Valid @RequestBody SessionInsightRequest request) {
        return ResponseEntity.ok(insightsService.analyzeSession(user, request.getSessionId()));
    }

    @PostMapping("/questions")
    public ResponseEntity<QuestionSuggestionResponse> suggestQuestions(@AuthenticationPrincipal User user,
                                                                       @Valid @RequestBody QuestionSuggestionRequest request) {
        return ResponseEntity.ok(insightsService.suggestQuestions(user, request));
    }

    @GetMapping("/follow-ups")
    public ResponseEntity<List<FollowUpPromptResponse>> getFollowUpPrompts(@AuthenticationPrincipal User user,
                                                                          @RequestParam(defaultValue = "4") int count) {
        return ResponseEntity.ok(insightsService.getFollowUpPrompts(user, count));
    }

    @PostMapping("/sessions/{id}/tag")
    public ResponseEntity<List<SemanticTagResponse>> tagSession(@AuthenticationPrincipal User user,
                                                                @PathVariable Long id) {
        return ResponseEntity.ok(insightsService.tagSession(user, id));
    }

    @GetMapping("/sessions/{id}/tags")
    public ResponseEntity<List<SemanticTagResponse>> getSessionTags(@AuthenticationPrincipal User user,
                                                                    @PathVariable Long id) {
        return ResponseEntity.ok(insightsService.getSessionTags(user, id));
    }

    @GetMapping("/tags/summary")
    public ResponseEntity<SemanticTagSummaryResponse> getTagSummary(@AuthenticationPrincipal User user,
                                                                    @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(insightsService.getTagSummary(user, days));
    }

    @GetMapping("/weekly-narrative")
    public ResponseEntity<WeeklyNarrativeResponse> getWeeklyNarrative(@AuthenticationPrincipal User user,
                                                                      @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(insightsService.getWeeklyNarrative(user, days));
    }

    @PostMapping("/feedback")
    public ResponseEntity<InsightFeedbackResponse> recordFeedback(@AuthenticationPrincipal User user,
                                                                  @Valid @RequestBody InsightFeedbackRequest request) {
        return ResponseEntity.ok(insightsService.recordFeedback(user, request));
    }

    @GetMapping("/feedback")
    public ResponseEntity<List<InsightFeedbackResponse>> getUserFeedback(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(insightsService.getUserFeedback(user));
    }
}
