package com.rei.examenbackend.service.impl;

import com.rei.examenbackend.dto.answer.AnswerResponse;
import com.rei.examenbackend.dto.session.ExaminationSessionResponse;
import com.rei.examenbackend.dto.session.ExaminationSessionSubmitRequest;
import com.rei.examenbackend.exception.ApiException;
import com.rei.examenbackend.model.Answer;
import com.rei.examenbackend.model.DailyExamination;
import com.rei.examenbackend.model.ExaminationSession;
import com.rei.examenbackend.model.Question;
import com.rei.examenbackend.model.QuestionType;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.repository.*;
import com.rei.examenbackend.service.ExaminationSessionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.time.Duration;

@Service
public class ExaminationSessionServiceImpl implements ExaminationSessionService {

    private final ExaminationSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final DailyExaminationRepository dailyExaminationRepository;
    private final com.rei.examenbackend.service.insights.InsightsService insightsService;

    public ExaminationSessionServiceImpl(
            ExaminationSessionRepository sessionRepository,
            UserRepository userRepository,
            AnswerRepository answerRepository,
            QuestionRepository questionRepository,
            DailyExaminationRepository dailyExaminationRepository
    ) {
        this(sessionRepository, userRepository, answerRepository, questionRepository, dailyExaminationRepository, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public ExaminationSessionServiceImpl(
            ExaminationSessionRepository sessionRepository,
            UserRepository userRepository,
            AnswerRepository answerRepository,
            QuestionRepository questionRepository,
            DailyExaminationRepository dailyExaminationRepository,
            @org.springframework.beans.factory.annotation.Autowired(required = false)
            com.rei.examenbackend.service.insights.InsightsService insightsService
    ) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.dailyExaminationRepository = dailyExaminationRepository;
        this.insightsService = insightsService;
    }

    @Override
    public ExaminationSessionResponse startSession(User user) {
        User persistedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        // enforce 24h cooldown: if a completed session exists in last 24h, block
        var recent = sessionRepository.findAllByUserAndCompletedAtIsNotNullAndCompletedAtAfterOrderByCompletedAtDesc(
                persistedUser, LocalDateTime.now().minusHours(24));
        if (!recent.isEmpty()) {
            var last = recent.getFirst();
            Duration remaining = Duration.between(LocalDateTime.now(), last.getCompletedAt().plusHours(24));
            long hoursLeft = Math.max(0, remaining.toHours());
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "You can start a new examination after " + hoursLeft + " hour(s).");
        }

        sessionRepository.findByUserAndCompletedAtIsNull(persistedUser)
                .ifPresent(session -> {
                    throw new ApiException(HttpStatus.CONFLICT, "You already have an active session");
                });

        ExaminationSession session = ExaminationSession.builder()
                .user(persistedUser)
                .startedAt(LocalDateTime.now())
                .build();

        sessionRepository.save(session);

        return toResponse(session);
    }

    @Override
    public ExaminationSessionResponse submitAnswers(Long sessionId, ExaminationSessionSubmitRequest request, User user) {

        ExaminationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot submit answers for this session");
        }

        if (session.getCompletedAt() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "Session already completed");
        }

        List<Answer> answerEntities = request.getAnswers().stream().map(req -> {
            Question q = questionRepository.findById(req.getQuestionId())
                    .filter(question -> question.getOwner() == null
                            || question.getOwner().getId().equals(user.getId()))
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found"));

            return Answer.builder()
                    .answerText(req.getAnswerText())
                    .correct(req.isCorrect())
                    .reflectionText(req.getReflectionText())
                    .feelingScore(req.getFeelingScore())
                    .question(q)
                    .examinationSession(session)
                    .build();

        }).collect(java.util.stream.Collectors.toList());

        answerRepository.saveAll(answerEntities);

        if (session.getAnswers() == null) {
            session.setAnswers(new java.util.ArrayList<>());
        } else {
            session.getAnswers().clear();
        }
        session.getAnswers().addAll(answerEntities);
        session.setCompletedAt(LocalDateTime.now());
        Integer moodScore = request.getMoodScore();
        if (moodScore == null) {
            var feelingAvg = answerEntities.stream()
                    .map(Answer::getFeelingScore)
                    .filter(java.util.Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .average();
            moodScore = feelingAvg.isPresent()
                    ? (int) Math.round(feelingAvg.getAsDouble())
                    : null;
        }

        session.setNotes(request.getNotes());
        session.setMoodScore(moodScore);
        sessionRepository.save(session);

        upsertDaily(session, request.getNotes(), moodScore);

        if (insightsService != null) {
            try {
                insightsService.tagSession(session.getUser(), session.getId());
            } catch (Exception ignored) {
                // Non-blocking auto-tagging
            }
        }

        return toResponse(session);
    }

    @Override
    public ExaminationSessionResponse getById(Long id, User user) {
        ExaminationSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot access this session");
        }
        return toResponse(session);
    }

    @Override
    public Page<ExaminationSessionResponse> getByUser(User user, Pageable pageable) {
        User persistedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        return sessionRepository.findAllByUserAndCompletedAtIsNotNullOrderByCompletedAtDesc(persistedUser, pageable)
                .map(this::toResponse);
    }

    @Override
    public java.util.Optional<ExaminationSessionResponse> getActive(User user) {
        User persistedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return sessionRepository.findByUserAndCompletedAtIsNull(persistedUser)
                .map(this::toResponse);
    }

    private ExaminationSessionResponse toResponse(ExaminationSession session) {
        Map<String, Double> categoryScores = session.getAnswers() == null ? null : calculateCategoryScores(session);
        Double overallScore = session.getAnswers() == null ? null : calculateOverallScore(session);

        return ExaminationSessionResponse.builder()
                .id(session.getId())
                .userId(session.getUser().getId())
                .startedAt(session.getStartedAt())
                .completedAt(session.getCompletedAt())
                .notes(session.getNotes())
                .moodScore(session.getMoodScore())
                .score(overallScore)
                .categoryScores(categoryScores)
                .answers(
                        session.getAnswers() == null ? null :
                                session.getAnswers().stream()
                                        .map(a -> AnswerResponse.builder()
                                                .id(a.getId())
                                                .answerText(a.getAnswerText())
                                                .correct(a.isCorrect())
                                                .reflectionText(a.getReflectionText())
                                                .feelingScore(a.getFeelingScore())
                                                .questionId(a.getQuestion().getId())
                                                .examinationSessionId(session.getId())
                                                .build()
                                        )
                                        .toList()
                )
                .build();
    }

    private Double calculateOverallScore(ExaminationSession session) {
        List<Double> scores = session.getAnswers().stream()
                .map(this::calculateAnswerScore)
                .flatMapToDouble(OptionalDouble::stream)
                .boxed()
                .toList();
        if (scores.isEmpty()) {
            return null;
        }
        return scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private Map<String, Double> calculateCategoryScores(ExaminationSession session) {
        Map<String, List<Answer>> grouped = session.getAnswers().stream()
                .collect(java.util.stream.Collectors.groupingBy(answer -> answer.getQuestion().getCategory().getName()));

        Map<String, Double> scores = new java.util.HashMap<>();
        grouped.forEach((category, answers) -> {
            List<Double> values = answers.stream()
                    .map(this::calculateAnswerScore)
                    .flatMapToDouble(OptionalDouble::stream)
                    .boxed()
                    .toList();
            if (!values.isEmpty()) {
                scores.put(category, values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
            }
        });
        return scores;
    }

    private OptionalDouble calculateAnswerScore(Answer answer) {
        Integer feelingScore = answer.getFeelingScore();
        if (feelingScore != null) {
            double normalized = ((double) (feelingScore - 1) / 4d) * 100d;
            return OptionalDouble.of(normalized);
        }

        Question question = answer.getQuestion();
        String response = answer.getAnswerText();
        if (response == null) {
            return OptionalDouble.empty();
        }

        QuestionType type = question.getResponseType() == null ? QuestionType.SCALE_1_5 : question.getResponseType();

        if (type == QuestionType.YES_NO) {
            String normalized = response.trim().toLowerCase();
            if (normalized.equals("yes") || normalized.equals("true") || normalized.equals("1")) {
                return OptionalDouble.of(100);
            }
            if (normalized.equals("no") || normalized.equals("false") || normalized.equals("0")) {
                return OptionalDouble.of(0);
            }
        } else if (type == QuestionType.SCALE_1_5) {
            try {
                int value = Integer.parseInt(response.trim());
                if (value >= 1 && value <= 5) {
                    double normalized = ((double) (value - 1) / 4d) * 100d;
                    return OptionalDouble.of(normalized);
                }
            } catch (NumberFormatException ignored) {
                return OptionalDouble.empty();
            }
        }

        return OptionalDouble.empty();
    }

    private void upsertDaily(ExaminationSession session, String notes, Integer moodScore) {
        var date = session.getCompletedAt() == null ? LocalDateTime.now().toLocalDate() : session.getCompletedAt().toLocalDate();
        var existing = dailyExaminationRepository.findByUserAndExamDate(session.getUser(), date);
        var daily = existing.orElseGet(() -> DailyExamination.builder()
                .user(session.getUser())
                .examDate(date)
                .build());

        daily.setSession(session);
        daily.setNotes(notes);
        daily.setMoodScore(moodScore);
        dailyExaminationRepository.save(daily);
    }
}
