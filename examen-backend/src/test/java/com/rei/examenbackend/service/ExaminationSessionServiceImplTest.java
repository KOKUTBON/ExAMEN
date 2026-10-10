package com.rei.examenbackend.service;

import com.rei.examenbackend.dto.answer.AnswerRequest;
import com.rei.examenbackend.dto.session.ExaminationSessionSubmitRequest;
import com.rei.examenbackend.model.*;
import com.rei.examenbackend.repository.AnswerRepository;
import com.rei.examenbackend.repository.ExaminationSessionRepository;
import com.rei.examenbackend.repository.QuestionRepository;
import com.rei.examenbackend.repository.UserRepository;
import com.rei.examenbackend.repository.DailyExaminationRepository;
import com.rei.examenbackend.service.impl.ExaminationSessionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ExaminationSessionServiceImplTest {

    private ExaminationSessionRepository sessionRepository;
    private UserRepository userRepository;
    private AnswerRepository answerRepository;
    private QuestionRepository questionRepository;
    private DailyExaminationRepository dailyExaminationRepository;
    private ExaminationSessionServiceImpl sessionService;

    @BeforeEach
    void setUp() {
        sessionRepository = mock(ExaminationSessionRepository.class);
        userRepository = mock(UserRepository.class);
        answerRepository = mock(AnswerRepository.class);
        questionRepository = mock(QuestionRepository.class);
        dailyExaminationRepository = mock(DailyExaminationRepository.class);
        sessionService = new ExaminationSessionServiceImpl(
                sessionRepository,
                userRepository,
                answerRepository,
                questionRepository,
                dailyExaminationRepository
        );
    }

    @Test
    void submitAnswersCalculatesScoreAndCompletesSession() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        Category category = new Category();
        ReflectionTestUtils.setField(category, "id", 10L);
        ReflectionTestUtils.setField(category, "name", "Discipline");

        Question scaleQuestion = new Question();
        ReflectionTestUtils.setField(scaleQuestion, "id", 100L);
        ReflectionTestUtils.setField(scaleQuestion, "text", "Did you keep focus?");
        ReflectionTestUtils.setField(scaleQuestion, "responseType", QuestionType.SCALE_1_5);
        ReflectionTestUtils.setField(scaleQuestion, "category", category);

        ExaminationSession session = new ExaminationSession();
        ReflectionTestUtils.setField(session, "id", 55L);
        ReflectionTestUtils.setField(session, "user", user);
        ReflectionTestUtils.setField(session, "startedAt", LocalDateTime.now());

        AnswerRequest answerRequest = new AnswerRequest();
        ReflectionTestUtils.setField(answerRequest, "questionId", 100L);
        ReflectionTestUtils.setField(answerRequest, "answerText", "5");
        ReflectionTestUtils.setField(answerRequest, "reflectionText", "Stayed focused during prayer.");
        ReflectionTestUtils.setField(answerRequest, "feelingScore", 5);

        ExaminationSessionSubmitRequest request = new ExaminationSessionSubmitRequest();
        ReflectionTestUtils.setField(request, "answers", List.of(answerRequest));
        ReflectionTestUtils.setField(request, "notes", "Felt focused.");
        ReflectionTestUtils.setField(request, "moodScore", 4);

        when(sessionRepository.findById(55L)).thenReturn(Optional.of(session));
        when(questionRepository.findById(100L)).thenReturn(Optional.of(scaleQuestion));
        when(dailyExaminationRepository.findByUserAndExamDate(eq(user), any())).thenReturn(Optional.empty());
        when(answerRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = sessionService.submitAnswers(55L, request, user);

        assertThat(ReflectionTestUtils.getField(response, "completedAt")).isNotNull();
        assertThat(ReflectionTestUtils.getField(response, "score")).isEqualTo(100.0);
        assertThat(ReflectionTestUtils.getField(response, "notes")).isEqualTo("Felt focused.");
        assertThat(ReflectionTestUtils.getField(response, "moodScore")).isEqualTo(4);
        verify(sessionRepository).save(session);
        verify(dailyExaminationRepository).save(any());
    }
}
