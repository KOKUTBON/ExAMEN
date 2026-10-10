package com.rei.examenbackend.service.insights;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rei.examenbackend.config.GeminiProperties;
import com.rei.examenbackend.dto.insights.EvidenceSnippetResponse;
import com.rei.examenbackend.dto.insights.FollowUpPromptResponse;
import com.rei.examenbackend.dto.insights.InsightsSummaryResponse;
import com.rei.examenbackend.dto.insights.QuestionSuggestionResponse;
import com.rei.examenbackend.dto.insights.SessionInsightResponse;
import com.rei.examenbackend.dto.insights.SemanticTagResponse;
import com.rei.examenbackend.dto.insights.WeeklyNarrativeResponse;
import com.rei.examenbackend.model.Answer;
import com.rei.examenbackend.model.ExaminationSession;
import com.rei.examenbackend.model.SemanticTagCategory;
import com.rei.examenbackend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GeminiInsightsClientTest {

    private GeminiProperties properties;
    private StubInsightsClient stubClient;
    private ObjectMapper objectMapper;
    private GeminiInsightsClient client;

    @BeforeEach
    void setUp() {
        properties = new GeminiProperties();
        stubClient = new StubInsightsClient();
        objectMapper = new ObjectMapper();
        client = new GeminiInsightsClient(properties, stubClient, objectMapper);
    }

    @Test
    @DisplayName("Falls back to deterministic engine when API key is blank")
    void fallsBackWhenApiKeyIsBlank() {
        properties.setApiKey("");
        properties.setEnabled(true);

        User user = new User();
        user.setId(1L);

        InsightsSummaryContext summaryContext = new InsightsSummaryContext(
                user,
                30,
                5,
                4.2,
                LocalDate.now().minusDays(30),
                LocalDate.now()
        );

        InsightsSummaryResponse response = client.buildSummary(summaryContext);
        assertThat(response).isNotNull();
        assertThat(response.getSummary()).contains("completed 5 sessions");
        assertThat(response.getAverageFeeling()).isEqualTo(4.2);
        assertThat(response.getConfidenceScore()).isGreaterThan(0.0);
        assertThat(response.getEvidenceSnippets()).isNotEmpty();
    }

    @Test
    @DisplayName("Falls back to deterministic engine when client is disabled")
    void fallsBackWhenDisabled() {
        properties.setApiKey("some-key");
        properties.setEnabled(false);

        User user = new User();
        user.setId(1L);

        QuestionSuggestionContext context = new QuestionSuggestionContext(
                user,
                "gratitude",
                3,
                List.of()
        );

        QuestionSuggestionResponse response = client.buildQuestionSuggestions(context);
        assertThat(response).isNotNull();
        assertThat(response.getSuggestions()).isNotEmpty();
        assertThat(response.getFollowUps()).isNotEmpty();
    }

    @Test
    @DisplayName("Builds follow-up prompts with explainable context via fallback engine")
    void buildsFollowUpPromptsViaFallback() {
        properties.setApiKey("");
        properties.setEnabled(true);

        User user = new User();
        user.setId(1L);

        QuestionSuggestionContext context = new QuestionSuggestionContext(
                user,
                "patience",
                3,
                List.of(),
                List.of("Rushed through meetings and lost composure"),
                List.of("Evening meditation"),
                List.of("work hurry")
        );

        List<FollowUpPromptResponse> followUps = client.buildFollowUpPrompts(context);
        assertThat(followUps).isNotNull().isNotEmpty();
        assertThat(followUps.get(0).getQuestion()).isNotEmpty();
        assertThat(followUps.get(0).getRationale()).isNotEmpty();
        assertThat(followUps.get(0).getSourceContext()).isNotEmpty();
    }

    @Test
    @DisplayName("Gracefully falls back to stub client when remote API call fails")
    void gracefullyFallsBackOnRemoteError() {
        properties.setApiKey("valid-looking-key");
        properties.setEnabled(true);
        properties.setBaseUrl("http://127.0.0.1:54321");
        properties.setTimeoutSeconds(1);

        User user = new User();
        user.setId(1L);

        ExaminationSession session = ExaminationSession.builder()
                .id(99L)
                .user(user)
                .startedAt(LocalDateTime.now().minusHours(1))
                .completedAt(LocalDateTime.now())
                .moodScore(4)
                .notes("Great session")
                .build();

        SessionInsightContext context = new SessionInsightContext(
                user,
                session,
                4.0,
                List.of("Gratitude"),
                List.of("Patience")
        );

        SessionInsightResponse response = client.buildSessionInsight(context);
        assertThat(response).isNotNull();
        assertThat(response.getSessionId()).isEqualTo(99L);
        assertThat(response.getSummary()).contains("Session average feeling");
        assertThat(response.getInsights()).isNotEmpty();
    }

    @Test
    @DisplayName("Extracts semantic tags via fallback engine when unconfigured")
    void extractsSemanticTagsViaFallback() {
        properties.setApiKey("");
        properties.setEnabled(true);

        User user = new User();
        user.setId(1L);

        ExaminationSession session = ExaminationSession.builder()
                .id(42L)
                .user(user)
                .notes("Felt peaceful and grateful today despite heavy workload deadline.")
                .build();

        Answer answer1 = Answer.builder()
                .id(101L)
                .reflectionText("I practiced patience with my colleague when conversations got tense.")
                .feelingScore(4)
                .build();

        Answer answer2 = Answer.builder()
                .id(102L)
                .reflectionText("I got anxious and rushed through my morning prayer.")
                .feelingScore(2)
                .build();

        SemanticTagContext context = new SemanticTagContext(user, session, List.of(answer1, answer2));

        List<SemanticTagResponse> tags = client.extractSemanticTags(context);
        assertThat(tags).isNotNull().isNotEmpty();

        boolean hasVirtue = tags.stream().anyMatch(t -> t.getCategory() == SemanticTagCategory.VIRTUE);
        boolean hasStruggle = tags.stream().anyMatch(t -> t.getCategory() == SemanticTagCategory.STRUGGLE);
        boolean hasGratitude = tags.stream().anyMatch(t -> t.getCategory() == SemanticTagCategory.GRATITUDE);
        boolean hasStressor = tags.stream().anyMatch(t -> t.getCategory() == SemanticTagCategory.STRESSOR);

        assertThat(hasVirtue).isTrue();
        assertThat(hasStruggle).isTrue();
        assertThat(hasGratitude).isTrue();
        assertThat(hasStressor).isTrue();
    }

    @Test
    @DisplayName("Builds weekly narrative via fallback engine when unconfigured")
    void buildsWeeklyNarrativeViaFallback() {
        properties.setApiKey("");
        properties.setEnabled(true);

        User user = new User();
        user.setId(1L);

        ExaminationSession session = ExaminationSession.builder()
                .id(1L)
                .user(user)
                .moodScore(4)
                .completedAt(LocalDateTime.now())
                .build();

        WeeklyNarrativeContext context = new WeeklyNarrativeContext(
                user,
                LocalDate.now().minusDays(7),
                LocalDate.now(),
                List.of(session),
                List.of(),
                List.of(),
                List.of()
        );

        WeeklyNarrativeResponse response = client.buildWeeklyNarrative(context);
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isNotEmpty();
        assertThat(response.getNarrative()).contains("completed 1 examination session");
        assertThat(response.getActionableAdvice()).isNotEmpty();
        assertThat(response.getVirtuesObserved()).isNotEmpty();
    }
}
