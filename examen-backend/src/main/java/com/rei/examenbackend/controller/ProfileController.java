package com.rei.examenbackend.controller;

import com.rei.examenbackend.dto.profile.AdvancedScoreDetails;
import com.rei.examenbackend.dto.profile.AnomalySignal;
import com.rei.examenbackend.dto.profile.PeriodSummaryResponse;
import com.rei.examenbackend.dto.profile.ProfileAnalyticsResponse;
import com.rei.examenbackend.dto.profile.ProfileProgressResponse;
import com.rei.examenbackend.dto.profile.ProfileSummaryResponse;
import com.rei.examenbackend.dto.profile.QualityMetrics;
import com.rei.examenbackend.exception.ApiException;
import com.rei.examenbackend.model.Answer;
import com.rei.examenbackend.model.ExaminationSession;
import com.rei.examenbackend.model.InsightFeedback;
import com.rei.examenbackend.model.QuestionType;
import com.rei.examenbackend.model.SemanticTagCategory;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.repository.DailyExaminationRepository;
import com.rei.examenbackend.repository.ExaminationSessionRepository;
import com.rei.examenbackend.repository.HabitScoreRepository;
import com.rei.examenbackend.repository.InsightFeedbackRepository;
import com.rei.examenbackend.repository.SemanticTagRepository;
import com.rei.examenbackend.repository.ToDoItemRepository;
import com.rei.examenbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final ExaminationSessionRepository sessionRepository;
    private final DailyExaminationRepository dailyExaminationRepository;
    private final ToDoItemRepository todoRepository;
    private final SemanticTagRepository semanticTagRepository;
    private final InsightFeedbackRepository feedbackRepository;
    private final HabitScoreRepository habitScoreRepository;

    @GetMapping("/summary")
    public ResponseEntity<ProfileSummaryResponse> summary(@AuthenticationPrincipal User user) {
        User persisted = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        var allSessions = sessionRepository.findAllByUserOrderByCompletedAtDesc(persisted);

        long completedSessions = allSessions.stream()
                .filter(session -> session.getCompletedAt() != null)
                .count();

        long streak = computeStreakDays(persisted);

        long categoriesUsed = allSessions.stream()
                .flatMap(session -> session.getAnswers() == null ? java.util.stream.Stream.empty() : session.getAnswers().stream())
                .map(answer -> answer.getQuestion().getCategory().getId())
                .distinct()
                .count();

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime weekAgo = now.minusDays(7);
        java.time.LocalDateTime monthAgo = now.minusDays(30);

        long sessionsThisWeek = sessionRepository.findAllByUserAndCompletedAtBetweenOrderByCompletedAtDesc(persisted, weekAgo, now)
                .stream()
                .filter(s -> s.getCompletedAt() != null)
                .count();

        long sessionsThisMonth = sessionRepository.findAllByUserAndCompletedAtBetweenOrderByCompletedAtDesc(persisted, monthAgo, now)
                .stream()
                .filter(s -> s.getCompletedAt() != null)
                .count();

        var recentSessions = sessionRepository.findAllByUserAndCompletedAtBetweenOrderByCompletedAtDesc(
                persisted, monthAgo, now);

        Double avgMood = recentSessions.stream()
                .filter(s -> s.getAnswers() != null)
                .flatMap(s -> s.getAnswers().stream())
                .map(Answer::getFeelingScore)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(Double.NaN);

        if (Double.isNaN(avgMood)) {
            avgMood = recentSessions.stream()
                    .filter(s -> s.getMoodScore() != null)
                    .mapToInt(ExaminationSession::getMoodScore)
                    .average()
                    .orElse(Double.NaN);
        }

        var todayFeelingAvg = allSessions.stream()
                .filter(s -> s.getCompletedAt() != null && s.getCompletedAt().toLocalDate().isEqual(java.time.LocalDate.now()))
                .filter(s -> s.getAnswers() != null)
                .flatMap(s -> s.getAnswers().stream())
                .map(Answer::getFeelingScore)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average();

        Integer todayMood = todayFeelingAvg.isPresent()
                ? (int) Math.round(todayFeelingAvg.getAsDouble())
                : null;

        if (todayMood == null) {
            todayMood = allSessions.stream()
                    .filter(s -> s.getCompletedAt() != null && s.getCompletedAt().toLocalDate().isEqual(java.time.LocalDate.now()))
                    .map(ExaminationSession::getMoodScore)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        java.time.LocalDate endDate = java.time.LocalDate.now();
        java.time.LocalDate startDate = endDate.minusDays(13);
        var moodTrend = dailyExaminationRepository.findByUserAndExamDateBetweenOrderByExamDateDesc(persisted, startDate, endDate)
                .stream()
                .map(d -> new ProfileSummaryResponse.MoodPoint(d.getExamDate(), d.getMoodScore()))
                .toList();

        Double spiritualScore = computeSpiritualScore(
                Double.isNaN(avgMood) ? null : avgMood,
                streak,
                sessionsThisMonth,
                categoriesUsed
        );

        long completedTodos = todoRepository.countByUserAndCompletedTrue(persisted);

        ProfileSummaryResponse response = ProfileSummaryResponse.builder()
                .examinationsCompleted(completedSessions)
                .todosCompleted(completedTodos)
                .categoriesUsed(categoriesUsed)
                .streakDays(streak)
                .spiritualProgressScore(spiritualScore)
                .sessionsThisWeek(sessionsThisWeek)
                .sessionsThisMonth(sessionsThisMonth)
                .averageMoodLast30Days(Double.isNaN(avgMood) ? null : avgMood)
                .todayMood(todayMood)
                .todayCompleted(todayMood != null || dailyExaminationRepository.findByUserAndExamDate(persisted, java.time.LocalDate.now()).isPresent())
                .recentMoodTrend(moodTrend)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/progress")
    public ResponseEntity<ProfileProgressResponse> progress(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "14") int days
    ) {
        User persisted = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        int safeDays = Math.min(Math.max(days, 1), 365);
        var end = java.time.LocalDate.now();
        var start = end.minusDays(safeDays - 1L);

        var daily = dailyExaminationRepository.findByUserAndExamDateBetweenOrderByExamDateDesc(persisted, start, end)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        com.rei.examenbackend.model.DailyExamination::getExamDate,
                        d -> d
                ));

        var sessionMap = sessionRepository.findAllByUserAndCompletedAtIsNotNullAndCompletedAtAfterOrderByCompletedAtDesc(
                        persisted, start.atStartOfDay())
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(s -> s.getCompletedAt().toLocalDate()));

        java.util.List<ProfileProgressResponse.ProgressPoint> points = new java.util.ArrayList<>();

        for (int i = 0; i < safeDays; i++) {
            var date = end.minusDays(i);
            var dailyEntry = daily.get(date);
            boolean completed = dailyEntry != null || sessionMap.containsKey(date);
            Integer mood = dailyEntry != null ? dailyEntry.getMoodScore() : sessionMap.getOrDefault(date, java.util.Collections.emptyList())
                    .stream()
                    .map(ExaminationSession::getMoodScore)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            points.add(ProfileProgressResponse.ProgressPoint.builder()
                    .date(date)
                    .completed(completed)
                    .mood(mood)
                    .build());
        }

        points.sort(java.util.Comparator.comparing(ProfileProgressResponse.ProgressPoint::getDate));

        return ResponseEntity.ok(ProfileProgressResponse.builder()
                .points(points)
                .build());
    }

    @GetMapping("/summary/weekly")
    public ResponseEntity<PeriodSummaryResponse> weekly(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(periodSummary(user, 7, "week"));
    }

    @GetMapping("/summary/monthly")
    public ResponseEntity<PeriodSummaryResponse> monthly(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(periodSummary(user, 30, "month"));
    }

    @GetMapping("/analytics")
    @Transactional(readOnly = true)
    public ResponseEntity<ProfileAnalyticsResponse> analytics(@AuthenticationPrincipal User user) {
        User persisted = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        var now = java.time.LocalDateTime.now();
        var since = now.minusDays(30);

        var sessions = sessionRepository.findAllByUserAndCompletedAtBetweenOrderByCompletedAtDesc(persisted, since, now);

        var answers = sessions.stream()
                .filter(s -> s.getAnswers() != null)
                .flatMap(s -> s.getAnswers().stream())
                .toList();

        Double overallAvgScore = answers.stream()
                .map(this::calculateAnswerScore)
                .flatMapToDouble(java.util.OptionalDouble::stream)
                .average()
                .orElse(Double.NaN);

        Double overallMood = answers.stream()
                .map(Answer::getFeelingScore)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(Double.NaN);

        if (Double.isNaN(overallMood)) {
            overallMood = sessions.stream()
                    .map(ExaminationSession::getMoodScore)
                    .filter(java.util.Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(Double.NaN);
        }

        var categories = answers.stream()
                .collect(java.util.stream.Collectors.groupingBy(answer -> answer.getQuestion().getCategory()))
                .entrySet()
                .stream()
                .map(entry -> {
                    var answerList = entry.getValue();
                    Double avg = answerList.stream()
                            .map(this::calculateAnswerScore)
                            .flatMapToDouble(java.util.OptionalDouble::stream)
                            .average()
                            .orElse(Double.NaN);
                    return ProfileAnalyticsResponse.CategoryBreakdown.builder()
                            .categoryId(entry.getKey().getId())
                            .categoryName(entry.getKey().getName())
                            .answers((long) answerList.size())
                            .averageScore(Double.isNaN(avg) ? null : avg)
                            .yesRate(null)
                            .build();
                })
                .sorted(java.util.Comparator.comparing(ProfileAnalyticsResponse.CategoryBreakdown::getAverageScore,
                        java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())))
                .toList();

        var weekly = sessions.stream()
                .filter(s -> s.getCompletedAt() != null)
                .collect(java.util.stream.Collectors.groupingBy(s ->
                        s.getCompletedAt().toLocalDate().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                ))
                .entrySet()
                .stream()
                .map(entry -> {
                    Double moodAvg = entry.getValue().stream()
                            .map(ExaminationSession::getMoodScore)
                            .filter(java.util.Objects::nonNull)
                            .mapToInt(Integer::intValue)
                            .average()
                            .orElse(Double.NaN);
                    return new ProfileAnalyticsResponse.WeeklyTrendPoint(entry.getKey(), entry.getValue().size(),
                            Double.isNaN(moodAvg) ? null : moodAvg);
                })
                .sorted(java.util.Comparator.comparing(ProfileAnalyticsResponse.WeeklyTrendPoint::weekStart))
                .toList();

        long streak = computeStreakDays(persisted);
        AdvancedScoreDetails advancedScore = computeAdvancedScore(persisted, sessions, Double.isNaN(overallMood) ? null : overallMood, streak);
        java.util.List<AnomalySignal> anomalies = detectAnomalies(persisted, sessions, streak);
        QualityMetrics qualityMetrics = computeQualityMetrics(persisted);

        ProfileAnalyticsResponse response = ProfileAnalyticsResponse.builder()
                .overallAverageScore(Double.isNaN(overallAvgScore) ? null : overallAvgScore)
                .overallMood(Double.isNaN(overallMood) ? null : overallMood)
                .categories(categories)
                .weeklyTrend(weekly)
                .advancedScore(advancedScore)
                .anomalies(anomalies)
                .qualityMetrics(qualityMetrics)
                .build();

        return ResponseEntity.ok(response);
    }

    private AdvancedScoreDetails computeAdvancedScore(User persisted, java.util.List<ExaminationSession> sessions, Double overallMood, long streak) {
        if (sessions.isEmpty()) {
            // No completed reflections yet: there is nothing to score.
            return null;
        }

        // Consistency: 30-day session cadence + streak
        double sessionRatio = Math.min(1.0, (double) sessions.size() / 20.0);
        double streakBonus = Math.min(1.0, (double) streak / 14.0);
        double consistencyScore = Math.min(100.0, Math.round(((sessionRatio * 75.0) + (streakBonus * 25.0)) * 10.0) / 10.0);

        // Mood & Stability:
        java.util.List<Double> moodValues = sessions.stream()
                .map(ExaminationSession::getMoodScore)
                .filter(java.util.Objects::nonNull)
                .map(Integer::doubleValue)
                .toList();

        double meanMood = moodValues.isEmpty()
                ? (overallMood != null && !Double.isNaN(overallMood) ? overallMood : 3.0)
                : moodValues.stream().mapToDouble(Double::doubleValue).average().orElse(3.0);

        double variance = moodValues.size() > 1
                ? moodValues.stream().mapToDouble(v -> Math.pow(v - meanMood, 2)).average().orElse(0.0)
                : 0.0;
        double stdDev = Math.sqrt(variance);
        double normalizedMood = ((meanMood - 1.0) / 4.0) * 100.0;
        double stabilityBonus = Math.max(0.0, 100.0 - (stdDev * 20.0));
        double moodStabilityScore = Math.min(100.0, Math.max(0.0, Math.round(((normalizedMood * 0.7) + (stabilityBonus * 0.3)) * 10.0) / 10.0));

        // Virtue / Struggle balance from semantic tags
        var now = java.time.LocalDateTime.now();
        var tags = semanticTagRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(persisted, now.minusDays(30), now);
        long positiveTags = tags.stream()
                .filter(t -> t.getCategory() == SemanticTagCategory.VIRTUE || t.getCategory() == SemanticTagCategory.GRATITUDE)
                .count();
        long totalTags = tags.size();
        Double virtueStruggleRatio = totalTags > 0
                ? Math.round(((double) positiveTags / totalTags * 100.0) * 10.0) / 10.0
                : null;

        // Goals execution (todos + habit scores)
        long completedTodos = todoRepository.countByUserAndCompletedTrue(persisted);
        var habits = habitScoreRepository.findByUserAndScoreDateBetweenOrderByScoreDateDesc(persisted, java.time.LocalDate.now().minusDays(30), java.time.LocalDate.now());
        double todoNorm = Math.min(100.0, completedTodos * 10.0);
        java.util.OptionalDouble habitAvg = habits.stream()
                .map(h -> h.getScore())
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average();
        double goalRaw = habitAvg.isPresent()
                ? (todoNorm * 0.5) + (((habitAvg.getAsDouble() - 1.0) / 4.0) * 100.0 * 0.5)
                : todoNorm;
        double goalExecutionScore = Math.min(100.0, Math.round(goalRaw * 10.0) / 10.0);

        // Weighted composite (30% consistency, 30% mood stability, 20% virtues, 20% goals).
        // Factors without data are left out and the remaining weights are renormalized.
        double weighted = (consistencyScore * 0.30) + (moodStabilityScore * 0.30) + (goalExecutionScore * 0.20);
        double totalWeight = 0.80;
        if (virtueStruggleRatio != null) {
            weighted += virtueStruggleRatio * 0.20;
            totalWeight += 0.20;
        }
        double composite = Math.round((weighted / totalWeight) * 10.0) / 10.0;

        // 95% Confidence Interval
        int sampleSize = sessions.size();
        double sampleStdDev = stdDev > 0 ? (stdDev / 4.0 * 100.0) : 15.0;
        double standardError = sampleStdDev / Math.sqrt(sampleSize);
        double margin = Math.min(22.0, Math.max(2.5, 1.96 * standardError));
        margin = Math.round(margin * 10.0) / 10.0;
        double lower = Math.max(0.0, Math.round((composite - margin) * 10.0) / 10.0);
        double upper = Math.min(100.0, Math.round((composite + margin) * 10.0) / 10.0);

        String rationale = String.format(java.util.Locale.US,
                "Weighted score from %d completed reflection(s), %d completed todo(s), and %d reflection theme(s) (95%% confidence interval ±%.1f%%).",
                sampleSize, completedTodos, totalTags, margin);

        return AdvancedScoreDetails.builder()
                .compositeScore(composite)
                .consistencyScore(consistencyScore)
                .moodStabilityScore(moodStabilityScore)
                .virtueStruggleRatio(virtueStruggleRatio)
                .goalExecutionScore(goalExecutionScore)
                .confidenceLower(lower)
                .confidenceUpper(upper)
                .confidenceLevel(0.95)
                .marginOfError(margin)
                .sampleSize(sampleSize)
                .scoringRationale(rationale)
                .build();
    }

    private java.util.List<AnomalySignal> detectAnomalies(User persisted, java.util.List<ExaminationSession> sessions, long streak) {
        java.util.List<AnomalySignal> anomalies = new java.util.ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();

        // 1. Mood Drop or Surge
        if (sessions.size() >= 2) {
            ExaminationSession latest = sessions.get(0);
            if (latest.getMoodScore() != null && latest.getCompletedAt() != null) {
                double prevAvg = sessions.stream()
                        .skip(1)
                        .map(ExaminationSession::getMoodScore)
                        .filter(java.util.Objects::nonNull)
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(Double.NaN);

                if (!Double.isNaN(prevAvg)) {
                    double diff = prevAvg - latest.getMoodScore();
                    if (diff >= 1.8) {
                        anomalies.add(AnomalySignal.builder()
                                .type("MOOD_DROP")
                                .severity("MEDIUM")
                                .title("Noticeable Drop in Feeling")
                                .description(String.format(java.util.Locale.US,
                                        "Latest session feeling (%d/5) was %.1f points below your recent baseline (%.1f/5). Allow yourself restorative stillness.",
                                        latest.getMoodScore(), diff, prevAvg))
                                .detectedDate(latest.getCompletedAt().toLocalDate())
                                .deviation(Math.round(diff * 10.0) / 10.0)
                                .build());
                    } else if (latest.getMoodScore() - prevAvg >= 1.8) {
                        anomalies.add(AnomalySignal.builder()
                                .type("BREAKTHROUGH")
                                .severity("LOW")
                                .title("Positive Feeling Surge")
                                .description(String.format(java.util.Locale.US,
                                        "Latest session feeling (%d/5) represents a marked positive uplift over your recent baseline (%.1f/5).",
                                        latest.getMoodScore(), prevAvg))
                                .detectedDate(latest.getCompletedAt().toLocalDate())
                                .deviation(Math.round((latest.getMoodScore() - prevAvg) * 10.0) / 10.0)
                                .build());
                    }
                }
            }
        }

        // 2. Streak Break
        if (!sessions.isEmpty() && sessions.get(0).getCompletedAt() != null) {
            java.time.LocalDate lastDate = sessions.get(0).getCompletedAt().toLocalDate();
            long daysSince = java.time.temporal.ChronoUnit.DAYS.between(lastDate, today);
            if (daysSince >= 2 && streak == 0) {
                anomalies.add(AnomalySignal.builder()
                        .type("STREAK_BREAK")
                        .severity("LOW")
                        .title("Reflection Rhythm Paused")
                        .description(String.format(java.util.Locale.US,
                                "It has been %d days since your last examination session. Even a 3-minute evening pause can reignite your habit.",
                                daysSince))
                        .detectedDate(today)
                        .deviation((double) daysSince)
                        .build());
            }
        }

        // 3. Stressor Cluster
        var recentTags = semanticTagRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
                persisted, java.time.LocalDateTime.now().minusDays(7), java.time.LocalDateTime.now());
        long stressorCount = recentTags.stream().filter(t -> t.getCategory() == SemanticTagCategory.STRESSOR).count();
        if (stressorCount >= 3) {
            anomalies.add(AnomalySignal.builder()
                    .type("STRESSOR_SURGE")
                    .severity("HIGH")
                    .title("Stressor Cluster Detected")
                    .description(String.format(java.util.Locale.US,
                            "%d stressor signals detected across recent reflections. Consider focusing on gratitude and peaceful detachment.",
                            stressorCount))
                    .detectedDate(today)
                    .deviation((double) stressorCount)
                    .build());
        }

        return anomalies;
    }

    private QualityMetrics computeQualityMetrics(User persisted) {
        var feedbacks = feedbackRepository.findByUserOrderByCreatedAtDesc(persisted);
        long totalFeedback = feedbacks.size();
        long positiveFeedback = feedbacks.stream().filter(InsightFeedback::isHelpful).count();
        Double satisfaction = totalFeedback > 0 ? (double) positiveFeedback / totalFeedback : null;

        var now = java.time.LocalDateTime.now();
        var last7Days = sessionRepository.findAllByUserAndCompletedAtBetweenOrderByCompletedAtDesc(persisted, now.minusDays(7), now);
        var prev23Days = sessionRepository.findAllByUserAndCompletedAtBetweenOrderByCompletedAtDesc(persisted, now.minusDays(30), now.minusDays(7));

        Double avg7 = last7Days.stream().map(ExaminationSession::getMoodScore).filter(java.util.Objects::nonNull).mapToInt(Integer::intValue).average().orElse(Double.NaN);
        Double avgPrev = prev23Days.stream().map(ExaminationSession::getMoodScore).filter(java.util.Objects::nonNull).mapToInt(Integer::intValue).average().orElse(Double.NaN);

        String driftIndicator = "STABLE";
        if (!Double.isNaN(avg7) && !Double.isNaN(avgPrev)) {
            double diff = avg7 - avgPrev;
            if (diff >= 0.4) driftIndicator = "POSITIVE_DRIFT";
            else if (diff <= -0.4) driftIndicator = "NEGATIVE_DRIFT";
        }

        return QualityMetrics.builder()
                .feedbackSatisfactionRate(satisfaction == null ? null : Math.round(satisfaction * 100.0) / 100.0)
                .totalFeedbackCount(totalFeedback)
                .sentimentDriftIndicator(driftIndicator)
                .evaluatedPeriodDays(30)
                .build();
    }

    private PeriodSummaryResponse periodSummary(User user, int days, String label) {
        User persisted = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        var end = java.time.LocalDate.now();
        var start = end.minusDays(days);

        var daily = dailyExaminationRepository.findByUserAndExamDateBetweenOrderByExamDateDesc(persisted, start, end);
        long completedDays = daily.size();

        Double avgMood = daily.stream()
                .filter(d -> d.getMoodScore() != null)
                .mapToInt(com.rei.examenbackend.model.DailyExamination::getMoodScore)
                .average()
                .orElse(Double.NaN);

        long sessions = sessionRepository.findAllByUserAndCompletedAtBetweenOrderByCompletedAtDesc(
                        persisted, start.atStartOfDay(), end.atTime(23,59,59))
                .stream()
                .filter(s -> s.getCompletedAt() != null)
                .count();

        return PeriodSummaryResponse.builder()
                .period(label)
                .sessions(sessions)
                .completedDays(completedDays)
                .averageMood(Double.isNaN(avgMood) ? null : avgMood)
                .build();
    }

    private long computeStreakDays(User user) {
        var sessions = sessionRepository.findAllByUserOrderByCompletedAtDesc(user)
                .stream()
                .filter(s -> s.getCompletedAt() != null)
                .toList();
        if (sessions.isEmpty()) {
            return 0;
        }

        long streak = 0;
        java.time.LocalDate current = java.time.LocalDate.now();

        java.util.Set<java.time.LocalDate> uniqueDays = new java.util.LinkedHashSet<>();
        sessions.forEach(s -> uniqueDays.add(s.getCompletedAt().toLocalDate()));

        for (java.time.LocalDate day : uniqueDays) {
            if (day.isEqual(current)) {
                streak++;
                current = current.minusDays(1);
            } else if (day.isBefore(current)) {
                // break streak when a day is missing
                break;
            }
        }
        return streak;
    }

    private java.util.OptionalDouble calculateAnswerScore(Answer answer) {
        Integer feelingScore = answer.getFeelingScore();
        if (feelingScore != null) {
            double normalized = ((double) (feelingScore - 1) / 4d) * 100d;
            return java.util.OptionalDouble.of(normalized);
        }

        var question = answer.getQuestion();
        var response = answer.getAnswerText();
        if (response == null) {
            return java.util.OptionalDouble.empty();
        }

        var type = question.getResponseType() == null ? QuestionType.SCALE_1_5 : question.getResponseType();

        if (type == QuestionType.YES_NO) {
            String normalized = response.trim().toLowerCase();
            if (normalized.equals("yes") || normalized.equals("true") || normalized.equals("1")) {
                return java.util.OptionalDouble.of(100);
            }
            if (normalized.equals("no") || normalized.equals("false") || normalized.equals("0")) {
                return java.util.OptionalDouble.of(0);
            }
        } else if (type == QuestionType.SCALE_1_5) {
            try {
                int value = Integer.parseInt(response.trim());
                if (value >= 1 && value <= 5) {
                    double normalized = ((double) (value - 1) / 4d) * 100d;
                    return java.util.OptionalDouble.of(normalized);
                }
            } catch (NumberFormatException ignored) {
                return java.util.OptionalDouble.empty();
            }
        }
        return java.util.OptionalDouble.empty();
    }

    private Double computeSpiritualScore(Double mood, long streakDays, long sessionsThisMonth, long categoriesUsed) {
        double moodScore = mood == null ? 0 : Math.min(5.0, Math.max(0.0, mood)) * 10; // 0-50
        double streakScore = Math.min(30, streakDays) / 30.0 * 20; // up to 20
        double sessionsScore = Math.min(20, sessionsThisMonth * 2); // 2 pts per session, capped 20
        double diversityScore = Math.min(10, categoriesUsed * 2); // breadth up to 10
        double total = moodScore + streakScore + sessionsScore + diversityScore;
        return Math.round(total * 10.0) / 10.0; // one decimal place
    }
}
