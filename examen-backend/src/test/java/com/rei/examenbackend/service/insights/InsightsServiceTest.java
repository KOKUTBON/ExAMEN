package com.rei.examenbackend.service.insights;

import com.rei.examenbackend.dto.insights.EvidenceSnippetResponse;
import com.rei.examenbackend.dto.insights.FollowUpPromptResponse;
import com.rei.examenbackend.dto.insights.InsightFeedbackRequest;
import com.rei.examenbackend.dto.insights.InsightFeedbackResponse;
import com.rei.examenbackend.dto.insights.InsightsSummaryResponse;
import com.rei.examenbackend.dto.insights.QuestionSuggestionRequest;
import com.rei.examenbackend.dto.insights.QuestionSuggestionResponse;
import com.rei.examenbackend.dto.insights.SemanticTagResponse;
import com.rei.examenbackend.dto.insights.SemanticTagSummaryResponse;
import com.rei.examenbackend.dto.insights.WeeklyNarrativeResponse;
import com.rei.examenbackend.model.Answer;
import com.rei.examenbackend.model.ExaminationSession;
import com.rei.examenbackend.model.InsightFeedback;
import com.rei.examenbackend.model.SemanticTag;
import com.rei.examenbackend.model.SemanticTagCategory;
import com.rei.examenbackend.model.ToDoItem;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.repository.AnswerRepository;
import com.rei.examenbackend.repository.ExaminationSessionRepository;
import com.rei.examenbackend.repository.GratitudeEntryRepository;
import com.rei.examenbackend.repository.HabitScoreRepository;
import com.rei.examenbackend.repository.InsightFeedbackRepository;
import com.rei.examenbackend.repository.QuestionRepository;
import com.rei.examenbackend.repository.SemanticTagRepository;
import com.rei.examenbackend.repository.ToDoItemRepository;
import com.rei.examenbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InsightsServiceTest {

    private InsightsClient insightsClient;
    private UserRepository userRepository;
    private ExaminationSessionRepository sessionRepository;
    private AnswerRepository answerRepository;
    private QuestionRepository questionRepository;
    private SemanticTagRepository semanticTagRepository;
    private InsightFeedbackRepository feedbackRepository;
    private GratitudeEntryRepository gratitudeRepository;
    private ToDoItemRepository todoRepository;
    private HabitScoreRepository habitScoreRepository;
    private InsightsService insightsService;

    @BeforeEach
    void setUp() {
        insightsClient = mock(InsightsClient.class);
        userRepository = mock(UserRepository.class);
        sessionRepository = mock(ExaminationSessionRepository.class);
        answerRepository = mock(AnswerRepository.class);
        questionRepository = mock(QuestionRepository.class);
        semanticTagRepository = mock(SemanticTagRepository.class);
        feedbackRepository = mock(InsightFeedbackRepository.class);
        gratitudeRepository = mock(GratitudeEntryRepository.class);
        todoRepository = mock(ToDoItemRepository.class);
        habitScoreRepository = mock(HabitScoreRepository.class);

        insightsService = new InsightsService(
                insightsClient,
                userRepository,
                sessionRepository,
                answerRepository,
                questionRepository,
                semanticTagRepository,
                feedbackRepository,
                gratitudeRepository,
                todoRepository,
                habitScoreRepository
        );
    }

    @Test
    @DisplayName("tagSession extracts tags, persists them, and returns DTOs")
    void tagSessionSuccess() {
        User user = new User();
        user.setId(10L);

        ExaminationSession session = ExaminationSession.builder()
                .id(20L)
                .user(user)
                .build();

        Answer answer = Answer.builder()
                .id(30L)
                .reflectionText("Patience practiced today")
                .examinationSession(session)
                .build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(sessionRepository.findById(20L)).thenReturn(Optional.of(session));
        when(answerRepository.findByExaminationSession(session)).thenReturn(List.of(answer));

        SemanticTagResponse tagResponse = new SemanticTagResponse(
                null, 20L, 30L, SemanticTagCategory.VIRTUE, "Patience", 0.95, LocalDateTime.now()
        );
        when(insightsClient.extractSemanticTags(any())).thenReturn(List.of(tagResponse));

        when(semanticTagRepository.saveAll(any())).thenAnswer(invocation -> {
            List<SemanticTag> list = invocation.getArgument(0);
            list.forEach(t -> t.setId(99L));
            return list;
        });

        List<SemanticTagResponse> result = insightsService.tagSession(user, 20L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTag()).isEqualTo("Patience");
        assertThat(result.get(0).getCategory()).isEqualTo(SemanticTagCategory.VIRTUE);
        verify(semanticTagRepository).deleteBySession(session);
        verify(semanticTagRepository).saveAll(any());
    }

    @Test
    @DisplayName("getTagSummary aggregates tags by category")
    void getTagSummaryAggregatesCorrectly() {
        User user = new User();
        user.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        SemanticTag t1 = SemanticTag.builder().user(user).category(SemanticTagCategory.VIRTUE).tag("Patience").build();
        SemanticTag t2 = SemanticTag.builder().user(user).category(SemanticTagCategory.VIRTUE).tag("Patience").build();
        SemanticTag t3 = SemanticTag.builder().user(user).category(SemanticTagCategory.STRUGGLE).tag("Anxiety").build();
        SemanticTag t4 = SemanticTag.builder().user(user).category(SemanticTagCategory.GRATITUDE).tag("Family").build();
        SemanticTag t5 = SemanticTag.builder().user(user).category(SemanticTagCategory.STRESSOR).tag("Work Deadline").build();

        when(semanticTagRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(eq(user), any(), any()))
                .thenReturn(List.of(t1, t2, t3, t4, t5));

        SemanticTagSummaryResponse summary = insightsService.getTagSummary(user, 30);

        assertThat(summary.getTotalTags()).isEqualTo(5);
        assertThat(summary.getVirtues()).hasSize(1);
        assertThat(summary.getVirtues().get(0).getTag()).isEqualTo("Patience");
        assertThat(summary.getVirtues().get(0).getCount()).isEqualTo(2);
        assertThat(summary.getStruggles().get(0).getTag()).isEqualTo("Anxiety");
        assertThat(summary.getGratitude().get(0).getTag()).isEqualTo("Family");
        assertThat(summary.getStressors().get(0).getTag()).isEqualTo("Work Deadline");
    }

    @Test
    @DisplayName("recordFeedback validates and saves user feedback")
    void recordFeedbackSuccess() {
        User user = new User();
        user.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        InsightFeedback savedFeedback = InsightFeedback.builder()
                .id(1L)
                .user(user)
                .targetType("WEEKLY_NARRATIVE")
                .targetId("narrative-1")
                .helpful(true)
                .comments("Very encouraging")
                .createdAt(LocalDateTime.now())
                .build();

        when(feedbackRepository.save(any(InsightFeedback.class))).thenReturn(savedFeedback);

        InsightFeedbackRequest request = new InsightFeedbackRequest(
                "WEEKLY_NARRATIVE", "narrative-1", true, "Very encouraging"
        );

        InsightFeedbackResponse response = insightsService.recordFeedback(user, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.isHelpful()).isTrue();
        assertThat(response.getTargetType()).isEqualTo("WEEKLY_NARRATIVE");
        assertThat(response.getComments()).isEqualTo("Very encouraging");
    }

    @Test
    @DisplayName("getWeeklyNarrative delegates to insightsClient with complete context")
    void getWeeklyNarrativeSuccess() {
        User user = new User();
        user.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        WeeklyNarrativeResponse mockResponse = new WeeklyNarrativeResponse(
                "Weekly Growth", "Detailed synthesis...", LocalDate.now().minusDays(7), LocalDate.now(),
                5, 4.0, List.of("Patience"), List.of("Distraction"), List.of("Family"), List.of("Work"),
                List.of("Breathe deeply"), LocalDateTime.now()
        );

        when(insightsClient.buildWeeklyNarrative(any(WeeklyNarrativeContext.class))).thenReturn(mockResponse);

        WeeklyNarrativeResponse response = insightsService.getWeeklyNarrative(user, 7);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Weekly Growth");
        assertThat(response.getSessionsAnalyzed()).isEqualTo(5);
        assertThat(response.getVirtuesObserved()).contains("Patience");
    }

    @Test
    @DisplayName("summary queries sessions and gratitude, passes enriched context, and returns confidence with evidence")
    void summaryWithConfidenceAndEvidenceSuccess() {
        User user = new User();
        user.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        ExaminationSession session = ExaminationSession.builder()
                .id(100L)
                .user(user)
                .completedAt(LocalDateTime.now().minusDays(2))
                .moodScore(4)
                .notes("Reflected on daily peace")
                .answers(List.of(
                        Answer.builder().reflectionText("Grateful for kindness").build()
                ))
                .build();

        when(sessionRepository.findAllByUserAndCompletedAtBetweenOrderByCompletedAtDesc(eq(user), any(), any()))
                .thenReturn(List.of(session));

        EvidenceSnippetResponse snippet = new EvidenceSnippetResponse(
                "Session Note", LocalDate.now().minusDays(2), "Reflected on daily peace", "Reflects direct self-examination"
        );
        InsightsSummaryResponse mockSummary = new InsightsSummaryResponse(
                "Strong consistency observed.",
                List.of("Mindful presence", "Gratitude habit"),
                30,
                1,
                4.0,
                0.85,
                List.of(snippet),
                LocalDateTime.now()
        );

        when(insightsClient.buildSummary(any(InsightsSummaryContext.class))).thenReturn(mockSummary);

        InsightsSummaryResponse response = insightsService.summary(user, 30);

        assertThat(response).isNotNull();
        assertThat(response.getSummary()).isEqualTo("Strong consistency observed.");
        assertThat(response.getConfidenceScore()).isEqualTo(0.85);
        assertThat(response.getEvidenceSnippets()).hasSize(1);
        assertThat(response.getEvidenceSnippets().get(0).getSnippet()).isEqualTo("Reflected on daily peace");

        verify(insightsClient).buildSummary(argThat(ctx ->
                ctx.recentSessions() != null &&
                ctx.recentSessions().size() == 1 &&
                ctx.recentSessions().get(0).notes().equals("Reflected on daily peace")
        ));
    }

    @Test
    @DisplayName("getFollowUpPrompts builds enriched context from answers, todos, and struggles")
    void getFollowUpPromptsSuccess() {
        User user = new User();
        user.setId(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        // Mock recent session with answer
        ExaminationSession session = ExaminationSession.builder()
                .id(101L)
                .user(user)
                .completedAt(LocalDateTime.now().minusDays(1))
                .build();
        Answer answer = Answer.builder()
                .id(201L)
                .examinationSession(session)
                .reflectionText("Struggled with work deadlines and felt rushed")
                .build();
        session.setAnswers(List.of(answer));
        when(sessionRepository.findAllByUserAndCompletedAtBetweenOrderByCompletedAtDesc(eq(user), any(), any()))
                .thenReturn(List.of(session));

        // Mock pending todo
        ToDoItem todo = new ToDoItem();
        todo.setId(301L);
        todo.setUser(user);
        todo.setTitle("Evening prayer before sleep");
        todo.setCompleted(false);
        when(todoRepository.findByUser(user)).thenReturn(List.of(todo));

        // Mock struggle tag
        SemanticTag tag = SemanticTag.builder()
                .id(401L)
                .user(user)
                .category(SemanticTagCategory.STRUGGLE)
                .tag("time pressure")
                .build();
        when(semanticTagRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(eq(user), any(), any()))
                .thenReturn(List.of(tag));

        FollowUpPromptResponse expectedPrompt = new FollowUpPromptResponse(
                "How can you make space for evening prayer amid work deadlines?",
                "Directly connects your incomplete goal with work deadline stress",
                "ACTIVE_GOAL",
                "Peace"
        );
        when(insightsClient.buildFollowUpPrompts(any(QuestionSuggestionContext.class)))
                .thenReturn(List.of(expectedPrompt));

        List<FollowUpPromptResponse> result = insightsService.getFollowUpPrompts(user, 3);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuestion()).contains("evening prayer");
        assertThat(result.get(0).getSourceContext()).isEqualTo("ACTIVE_GOAL");

        verify(insightsClient).buildFollowUpPrompts(argThat(ctx ->
                ctx.recentAnswers().contains("Struggled with work deadlines and felt rushed") &&
                ctx.activeGoals().contains("Evening prayer before sleep") &&
                ctx.recentStruggles().contains("time pressure")
        ));
    }

    @Test
    @DisplayName("suggestQuestions returns suggestions and followUps")
    void suggestQuestionsSuccess() {
        User user = new User();
        user.setId(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        FollowUpPromptResponse followUp = new FollowUpPromptResponse(
                "Where did you notice peace today?",
                "Grounded in your recent prayer focus",
                "RECENT_REFLECTION",
                "Peace"
        );
        QuestionSuggestionResponse mockResponse = new QuestionSuggestionResponse(
                "gratitude",
                List.of("What small gift did you overlook?"),
                List.of(followUp),
                LocalDateTime.now()
        );
        when(insightsClient.buildQuestionSuggestions(any(QuestionSuggestionContext.class)))
                .thenReturn(mockResponse);

        QuestionSuggestionRequest request = new QuestionSuggestionRequest();
        request.setFocus("gratitude");
        request.setCount(3);

        QuestionSuggestionResponse response = insightsService.suggestQuestions(user, request);

        assertThat(response).isNotNull();
        assertThat(response.getFocus()).isEqualTo("gratitude");
        assertThat(response.getSuggestions()).contains("What small gift did you overlook?");
        assertThat(response.getFollowUps()).hasSize(1);
        assertThat(response.getFollowUps().get(0).getSourceContext()).isEqualTo("RECENT_REFLECTION");
    }
}
