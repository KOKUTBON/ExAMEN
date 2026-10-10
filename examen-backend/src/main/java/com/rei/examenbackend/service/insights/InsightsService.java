package com.rei.examenbackend.service.insights;

import com.rei.examenbackend.dto.insights.FollowUpPromptResponse;
import com.rei.examenbackend.dto.insights.InsightFeedbackRequest;
import com.rei.examenbackend.dto.insights.InsightFeedbackResponse;
import com.rei.examenbackend.dto.insights.InsightsSummaryResponse;
import com.rei.examenbackend.dto.insights.QuestionSuggestionRequest;
import com.rei.examenbackend.dto.insights.QuestionSuggestionResponse;
import com.rei.examenbackend.dto.insights.SemanticTagResponse;
import com.rei.examenbackend.dto.insights.SemanticTagSummaryResponse;
import com.rei.examenbackend.dto.insights.SessionInsightResponse;
import com.rei.examenbackend.dto.insights.WeeklyNarrativeResponse;
import com.rei.examenbackend.exception.ApiException;
import com.rei.examenbackend.model.Answer;
import com.rei.examenbackend.model.ExaminationSession;
import com.rei.examenbackend.model.GratitudeEntry;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class InsightsService {
    private final InsightsClient insightsClient;
    private final UserRepository userRepository;
    private final ExaminationSessionRepository sessionRepository;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final SemanticTagRepository semanticTagRepository;
    private final InsightFeedbackRepository feedbackRepository;
    private final GratitudeEntryRepository gratitudeRepository;
    private final ToDoItemRepository todoRepository;
    private final HabitScoreRepository habitScoreRepository;

    public InsightsService(InsightsClient insightsClient,
                           UserRepository userRepository,
                           ExaminationSessionRepository sessionRepository,
                           AnswerRepository answerRepository,
                           QuestionRepository questionRepository,
                           SemanticTagRepository semanticTagRepository,
                           InsightFeedbackRepository feedbackRepository,
                           GratitudeEntryRepository gratitudeRepository) {
        this(insightsClient, userRepository, sessionRepository, answerRepository, questionRepository,
                semanticTagRepository, feedbackRepository, gratitudeRepository, null, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public InsightsService(InsightsClient insightsClient,
                           UserRepository userRepository,
                           ExaminationSessionRepository sessionRepository,
                           AnswerRepository answerRepository,
                           QuestionRepository questionRepository,
                           SemanticTagRepository semanticTagRepository,
                           InsightFeedbackRepository feedbackRepository,
                           GratitudeEntryRepository gratitudeRepository,
                           ToDoItemRepository todoRepository,
                           HabitScoreRepository habitScoreRepository) {
        this.insightsClient = insightsClient;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.semanticTagRepository = semanticTagRepository;
        this.feedbackRepository = feedbackRepository;
        this.gratitudeRepository = gratitudeRepository;
        this.todoRepository = todoRepository;
        this.habitScoreRepository = habitScoreRepository;
    }

    public InsightsSummaryResponse summary(User user, int days) {
        User persisted = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        int safeDays = Math.min(Math.max(days, 1), 90);
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(safeDays);

        List<ExaminationSession> sessions = sessionRepository
                .findAllByUserAndCompletedAtBetweenOrderByCompletedAtDesc(persisted, start, end)
                .stream()
                .filter(session -> session.getCompletedAt() != null)
                .toList();

        Double averageFeeling = sessions.stream()
                .map(ExaminationSession::getMoodScore)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(Double.NaN);

        Double normalizedFeeling = Double.isNaN(averageFeeling) ? null : averageFeeling;

        List<InsightsSummaryContext.SessionContextSnippet> sessionSnippets = sessions.stream()
                .limit(10)
                .map(s -> {
                    List<String> reflections = new ArrayList<>();
                    if (s.getAnswers() != null) {
                        for (Answer a : s.getAnswers()) {
                            if (StringUtils.hasText(a.getReflectionText())) {
                                reflections.add(a.getReflectionText().trim());
                            }
                        }
                    }
                    LocalDate sessionDate = s.getCompletedAt() != null
                            ? s.getCompletedAt().toLocalDate()
                            : (s.getStartedAt() != null ? s.getStartedAt().toLocalDate() : LocalDate.now());
                    return new InsightsSummaryContext.SessionContextSnippet(
                            s.getId(),
                            sessionDate,
                            s.getMoodScore(),
                            s.getNotes(),
                            reflections
                    );
                })
                .toList();

        List<String> gratitudeSnippets = gratitudeRepository
                .findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(persisted, start, end)
                .stream()
                .map(GratitudeEntry::getContent)
                .filter(StringUtils::hasText)
                .limit(5)
                .toList();

        InsightsSummaryContext context = new InsightsSummaryContext(
                persisted,
                safeDays,
                sessions.size(),
                normalizedFeeling,
                start.toLocalDate(),
                end.toLocalDate(),
                sessionSnippets,
                gratitudeSnippets
        );

        return insightsClient.buildSummary(context);
    }

    public SessionInsightResponse analyzeSession(User user, Long sessionId) {
        if (sessionId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Session id is required");
        }

        User persisted = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        ExaminationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Session not found"));

        if (!session.getUser().getId().equals(persisted.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Session does not belong to user");
        }

        List<Answer> answers = answerRepository.findByExaminationSession(session);
        Double averageFeeling = answers.stream()
                .map(Answer::getFeelingScore)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(Double.NaN);

        Double normalizedFeeling = Double.isNaN(averageFeeling) ? null : averageFeeling;
        if (normalizedFeeling == null && session.getMoodScore() != null) {
            normalizedFeeling = session.getMoodScore().doubleValue();
        }

        Map<String, Double> categoryAverages = answers.stream()
                .filter(answer -> answer.getFeelingScore() != null)
                .collect(Collectors.groupingBy(
                        answer -> answer.getQuestion().getCategory().getName(),
                        Collectors.averagingInt(Answer::getFeelingScore)
                ));

        List<String> strongest = categoryAverages.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(2)
                .map(Map.Entry::getKey)
                .toList();

        List<String> needsAttention = categoryAverages.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(2)
                .map(Map.Entry::getKey)
                .toList();

        SessionInsightContext context = new SessionInsightContext(
                persisted,
                session,
                normalizedFeeling,
                strongest,
                needsAttention
        );

        return insightsClient.buildSessionInsight(context);
    }

    public QuestionSuggestionResponse suggestQuestions(User user, QuestionSuggestionRequest request) {
        User persisted = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        String focus = request == null ? null : request.getFocus();
        int count = request == null || request.getCount() == null ? 5 : request.getCount();
        int safeCount = Math.min(Math.max(count, 1), 10);

        QuestionSuggestionContext context = buildQuestionSuggestionContext(persisted, focus, safeCount);
        QuestionSuggestionResponse response = insightsClient.buildQuestionSuggestions(context);
        response.setFocus(response.getFocus() == null ? null : response.getFocus().trim());
        return response;
    }

    public List<FollowUpPromptResponse> getFollowUpPrompts(User user, int count) {
        User persisted = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        int safeCount = Math.min(Math.max(count, 1), 10);
        QuestionSuggestionContext context = buildQuestionSuggestionContext(persisted, "reflection deepening", safeCount);
        return insightsClient.buildFollowUpPrompts(context);
    }

    private QuestionSuggestionContext buildQuestionSuggestionContext(User persisted, String focus, int safeCount) {
        List<String> existing = questionRepository.findByOwnerIsNull().stream()
                .map(q -> q.getText() == null ? null : q.getText().trim())
                .filter(text -> text != null && !text.isBlank())
                .map(text -> text.toLowerCase(Locale.US))
                .toList();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recentStart = now.minusDays(14);
        List<ExaminationSession> recentSessions = sessionRepository
                .findAllByUserAndCompletedAtBetweenOrderByCompletedAtDesc(persisted, recentStart, now);

        List<String> recentAnswers = recentSessions.stream()
                .filter(s -> s.getAnswers() != null)
                .flatMap(s -> s.getAnswers().stream())
                .map(a -> a.getReflectionText() != null ? a.getReflectionText().trim() : (a.getAnswerText() != null ? a.getAnswerText().trim() : null))
                .filter(t -> t != null && !t.isBlank())
                .limit(8)
                .toList();

        List<String> activeGoals = new ArrayList<>();
        if (todoRepository != null) {
            List<ToDoItem> todos = todoRepository.findByUser(persisted);
            todos.stream()
                    .filter(t -> !t.isCompleted() && StringUtils.hasText(t.getTitle()))
                    .map(t -> t.getTitle().trim())
                    .limit(5)
                    .forEach(activeGoals::add);
        }

        if (habitScoreRepository != null) {
            LocalDate habitStart = LocalDate.now().minusDays(7);
            habitScoreRepository.findByUserAndScoreDateBetweenOrderByScoreDateDesc(persisted, habitStart, LocalDate.now())
                    .stream()
                    .map(h -> h.getHabit() == null ? null : h.getHabit().trim())
                    .filter(StringUtils::hasText)
                    .distinct()
                    .limit(3)
                    .forEach(activeGoals::add);
        }

        List<String> recentStruggles = semanticTagRepository
                .findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(persisted, recentStart, now)
                .stream()
                .filter(tag -> tag.getCategory() == SemanticTagCategory.STRUGGLE || tag.getCategory() == SemanticTagCategory.STRESSOR)
                .map(SemanticTag::getTag)
                .distinct()
                .limit(6)
                .toList();

        return new QuestionSuggestionContext(
                persisted,
                focus,
                safeCount,
                existing,
                recentAnswers,
                activeGoals,
                recentStruggles
        );
    }

    @Transactional
    public List<SemanticTagResponse> tagSession(User user, Long sessionId) {
        if (sessionId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Session id is required");
        }

        User persisted = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        ExaminationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Session not found"));

        if (!session.getUser().getId().equals(persisted.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Session does not belong to user");
        }

        List<Answer> answers = answerRepository.findByExaminationSession(session);
        SemanticTagContext context = new SemanticTagContext(persisted, session, answers);

        List<SemanticTagResponse> extracted = insightsClient.extractSemanticTags(context);

        // Map answer IDs if possible
        Map<Long, Answer> answerMap = answers.stream()
                .collect(Collectors.toMap(Answer::getId, a -> a, (k1, k2) -> k1));

        semanticTagRepository.deleteBySession(session);

        List<SemanticTag> entities = extracted.stream().map(dto -> {
            Answer ans = dto.getAnswerId() != null ? answerMap.get(dto.getAnswerId()) : null;
            return SemanticTag.builder()
                    .user(persisted)
                    .session(session)
                    .answer(ans)
                    .category(dto.getCategory())
                    .tag(dto.getTag())
                    .confidence(dto.getConfidence() != null ? dto.getConfidence() : 1.0)
                    .createdAt(LocalDateTime.now())
                    .build();
        }).toList();

        List<SemanticTag> saved = semanticTagRepository.saveAll(entities);

        return saved.stream()
                .map(entity -> new SemanticTagResponse(
                        entity.getId(),
                        entity.getSession() != null ? entity.getSession().getId() : null,
                        entity.getAnswer() != null ? entity.getAnswer().getId() : null,
                        entity.getCategory(),
                        entity.getTag(),
                        entity.getConfidence(),
                        entity.getCreatedAt()
                ))
                .toList();
    }

    public List<SemanticTagResponse> getSessionTags(User user, Long sessionId) {
        if (sessionId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Session id is required");
        }

        User persisted = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        ExaminationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Session not found"));

        if (!session.getUser().getId().equals(persisted.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Session does not belong to user");
        }

        return semanticTagRepository.findBySession(session).stream()
                .map(entity -> new SemanticTagResponse(
                        entity.getId(),
                        entity.getSession() != null ? entity.getSession().getId() : null,
                        entity.getAnswer() != null ? entity.getAnswer().getId() : null,
                        entity.getCategory(),
                        entity.getTag(),
                        entity.getConfidence(),
                        entity.getCreatedAt()
                ))
                .toList();
    }

    public SemanticTagSummaryResponse getTagSummary(User user, int days) {
        User persisted = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        int safeDays = Math.min(Math.max(days, 1), 365);
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(safeDays);

        List<SemanticTag> tags = semanticTagRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
                persisted, start, end
        );

        List<SemanticTagSummaryResponse.TagCount> virtues = aggregateCategory(tags, SemanticTagCategory.VIRTUE);
        List<SemanticTagSummaryResponse.TagCount> struggles = aggregateCategory(tags, SemanticTagCategory.STRUGGLE);
        List<SemanticTagSummaryResponse.TagCount> gratitude = aggregateCategory(tags, SemanticTagCategory.GRATITUDE);
        List<SemanticTagSummaryResponse.TagCount> stressors = aggregateCategory(tags, SemanticTagCategory.STRESSOR);

        return new SemanticTagSummaryResponse(
                safeDays,
                tags.size(),
                virtues,
                struggles,
                gratitude,
                stressors,
                LocalDateTime.now()
        );
    }

    private List<SemanticTagSummaryResponse.TagCount> aggregateCategory(List<SemanticTag> tags, SemanticTagCategory category) {
        return tags.stream()
                .filter(t -> t.getCategory() == category)
                .collect(Collectors.groupingBy(SemanticTag::getTag, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8)
                .map(e -> new SemanticTagSummaryResponse.TagCount(e.getKey(), e.getValue()))
                .toList();
    }

    public WeeklyNarrativeResponse getWeeklyNarrative(User user, int days) {
        User persisted = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        int safeDays = Math.min(Math.max(days, 1), 90);
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(safeDays);

        List<ExaminationSession> sessions = sessionRepository
                .findAllByUserAndCompletedAtBetweenOrderByCompletedAtDesc(persisted, start, end)
                .stream()
                .filter(s -> s.getCompletedAt() != null)
                .toList();

        List<SemanticTag> recentTags = semanticTagRepository
                .findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(persisted, start, end);

        List<GratitudeEntry> recentGratitude = gratitudeRepository
                .findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(persisted, start, end);

        List<InsightFeedback> feedbackPreferences = feedbackRepository
                .findByUserAndCreatedAtAfterOrderByCreatedAtDesc(persisted, start.minusDays(30));

        WeeklyNarrativeContext context = new WeeklyNarrativeContext(
                persisted,
                start.toLocalDate(),
                end.toLocalDate(),
                sessions,
                recentTags,
                recentGratitude,
                feedbackPreferences
        );

        return insightsClient.buildWeeklyNarrative(context);
    }

    public InsightFeedbackResponse recordFeedback(User user, InsightFeedbackRequest request) {
        if (request == null || request.getTargetType() == null || request.getHelpful() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Target type and helpful status are required");
        }

        User persisted = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        InsightFeedback feedback = InsightFeedback.builder()
                .user(persisted)
                .targetType(request.getTargetType().trim())
                .targetId(request.getTargetId() != null ? request.getTargetId().trim() : null)
                .helpful(request.getHelpful())
                .comments(request.getComments() != null ? request.getComments().trim() : null)
                .createdAt(LocalDateTime.now())
                .build();

        InsightFeedback saved = feedbackRepository.save(feedback);

        return new InsightFeedbackResponse(
                saved.getId(),
                saved.getTargetType(),
                saved.getTargetId(),
                saved.isHelpful(),
                saved.getComments(),
                saved.getCreatedAt()
        );
    }

    public List<InsightFeedbackResponse> getUserFeedback(User user) {
        User persisted = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        return feedbackRepository.findByUserOrderByCreatedAtDesc(persisted).stream()
                .limit(20)
                .map(f -> new InsightFeedbackResponse(
                        f.getId(),
                        f.getTargetType(),
                        f.getTargetId(),
                        f.isHelpful(),
                        f.getComments(),
                        f.getCreatedAt()
                ))
                .toList();
    }
}
