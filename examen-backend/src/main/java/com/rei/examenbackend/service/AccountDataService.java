package com.rei.examenbackend.service;

import com.rei.examenbackend.exception.ApiException;
import com.rei.examenbackend.model.Answer;
import com.rei.examenbackend.model.ExaminationSession;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.repository.CategoryRepository;
import com.rei.examenbackend.repository.DailyExaminationRepository;
import com.rei.examenbackend.repository.ExaminationSessionRepository;
import com.rei.examenbackend.repository.GratitudeEntryRepository;
import com.rei.examenbackend.repository.HabitScoreRepository;
import com.rei.examenbackend.repository.InsightFeedbackRepository;
import com.rei.examenbackend.repository.JournalEntryRepository;
import com.rei.examenbackend.repository.PasswordResetTokenRepository;
import com.rei.examenbackend.repository.QuestionRepository;
import com.rei.examenbackend.repository.SemanticTagRepository;
import com.rei.examenbackend.repository.ToDoItemRepository;
import com.rei.examenbackend.repository.UserMetricRepository;
import com.rei.examenbackend.repository.UserNotificationRepository;
import com.rei.examenbackend.repository.UserRepository;
import com.rei.examenbackend.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data portability and erasure for a single account: a complete JSON export of everything the
 * user has stored, and permanent deletion of the account with all of its data.
 */
@Service
@RequiredArgsConstructor
public class AccountDataService {

    public static final int EXPORT_FORMAT_VERSION = 1;

    private final UserRepository userRepository;
    private final UserSettingsRepository settingsRepository;
    private final ExaminationSessionRepository sessionRepository;
    private final DailyExaminationRepository dailyExaminationRepository;
    private final JournalEntryRepository journalRepository;
    private final GratitudeEntryRepository gratitudeRepository;
    private final HabitScoreRepository habitRepository;
    private final ToDoItemRepository todoRepository;
    private final SemanticTagRepository semanticTagRepository;
    private final InsightFeedbackRepository feedbackRepository;
    private final UserNotificationRepository notificationRepository;
    private final UserMetricRepository metricRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Map<String, Object> export(User principal) {
        User user = load(principal);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("formatVersion", EXPORT_FORMAT_VERSION);
        out.put("exportedAt", LocalDateTime.now());
        out.put("account", map(
                "id", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail()));

        out.put("settings", settingsRepository.findByUser(user).map(s -> map(
                "timeZone", s.getTimeZone(),
                "reminderTime", s.getReminderTime(),
                "emailReminder", s.getEmailReminder(),
                "inAppReminder", s.getInAppReminder())).orElse(null));

        out.put("categories", categoryRepository.findByOwnerId(user.getId()).stream().map(c -> map(
                "id", c.getId(),
                "name", c.getName(),
                "description", c.getDescription())).toList());

        out.put("customQuestions", questionRepository.findByOwnerId(user.getId()).stream().map(q -> map(
                "id", q.getId(),
                "text", q.getText(),
                "categoryId", q.getCategory().getId(),
                "responseType", q.getResponseType(),
                "archived", q.isArchived())).toList());

        out.put("examinationSessions", sessionRepository.findAllByUserOrderByCompletedAtDesc(user).stream()
                .map(this::session).toList());

        out.put("dailyExaminations", dailyExaminationRepository.findByUserOrderByExamDateDesc(user).stream().map(d -> map(
                "date", d.getExamDate(),
                "sessionId", d.getSession() == null ? null : d.getSession().getId(),
                "moodScore", d.getMoodScore(),
                "notes", d.getNotes())).toList());

        out.put("journalEntries", journalRepository.findByUserOrderByCreatedAtDesc(user).stream().map(j -> map(
                "id", j.getId(),
                "content", j.getContent(),
                "createdAt", j.getCreatedAt())).toList());

        out.put("gratitudeEntries", gratitudeRepository.findByUserOrderByCreatedAtDesc(user).stream().map(g -> map(
                "id", g.getId(),
                "content", g.getContent(),
                "createdAt", g.getCreatedAt())).toList());

        out.put("habitScores", habitRepository.findByUserOrderByScoreDateDesc(user).stream().map(h -> map(
                "habit", h.getHabit(),
                "score", h.getScore(),
                "date", h.getScoreDate())).toList());

        out.put("todos", todoRepository.findByUserOrderByDueAtAsc(user).stream().map(t -> map(
                "id", t.getId(),
                "title", t.getTitle(),
                "completed", t.isCompleted(),
                "dueAt", t.getDueAt(),
                "updatedAt", t.getUpdatedAt())).toList());

        out.put("semanticTags", semanticTagRepository.findByUserOrderByCreatedAtDesc(user).stream().map(t -> map(
                "sessionId", t.getSession() == null ? null : t.getSession().getId(),
                "answerId", t.getAnswer() == null ? null : t.getAnswer().getId(),
                "category", t.getCategory(),
                "tag", t.getTag(),
                "confidence", t.getConfidence(),
                "createdAt", t.getCreatedAt())).toList());

        out.put("insightFeedback", feedbackRepository.findByUserOrderByCreatedAtDesc(user).stream().map(f -> map(
                "targetType", f.getTargetType(),
                "targetId", f.getTargetId(),
                "helpful", f.isHelpful(),
                "comments", f.getComments(),
                "createdAt", f.getCreatedAt())).toList());

        return out;
    }

    @Transactional
    public void deleteAccount(User principal, String password) {
        User user = load(principal);
        if (password == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Password is incorrect");
        }

        // Children first, so this works without relying on database-level cascades.
        semanticTagRepository.deleteByUser(user);
        feedbackRepository.deleteByUser(user);
        notificationRepository.deleteByUser(user);
        metricRepository.deleteByUser(user);
        dailyExaminationRepository.deleteByUser(user);
        sessionRepository.deleteByUser(user);
        journalRepository.deleteByUser(user);
        gratitudeRepository.deleteByUser(user);
        habitRepository.deleteByUser(user);
        todoRepository.deleteByUser(user);
        settingsRepository.deleteByUser(user);
        resetTokenRepository.deleteByUser(user);
        questionRepository.deleteAll(questionRepository.findByOwnerId(user.getId()));
        categoryRepository.deleteAll(categoryRepository.findByOwnerId(user.getId()));
        userRepository.delete(user);
    }

    private Map<String, Object> session(ExaminationSession s) {
        List<Answer> answers = s.getAnswers() == null ? List.of() : s.getAnswers();
        return map(
                "id", s.getId(),
                "startedAt", s.getStartedAt(),
                "completedAt", s.getCompletedAt(),
                "moodScore", s.getMoodScore(),
                "notes", s.getNotes(),
                "answers", answers.stream().map(a -> map(
                        "id", a.getId(),
                        "questionId", a.getQuestion().getId(),
                        "questionText", a.getQuestion().getText(),
                        "category", a.getQuestion().getCategory().getName(),
                        "answer", a.getAnswerText(),
                        "reflection", a.getReflectionText(),
                        "feelingScore", a.getFeelingScore())).toList());
    }

    private User load(User principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private static Map<String, Object> map(Object... pairs) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            m.put((String) pairs[i], pairs[i + 1]);
        }
        return m;
    }
}
