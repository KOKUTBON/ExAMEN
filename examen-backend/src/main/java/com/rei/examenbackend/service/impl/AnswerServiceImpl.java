package com.rei.examenbackend.service.impl;

import com.rei.examenbackend.dto.answer.AnswerRequest;
import com.rei.examenbackend.dto.answer.AnswerResponse;
import com.rei.examenbackend.exception.ApiException;
import com.rei.examenbackend.model.Answer;
import com.rei.examenbackend.model.ExaminationSession;
import com.rei.examenbackend.model.Question;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.repository.AnswerRepository;
import com.rei.examenbackend.repository.ExaminationSessionRepository;
import com.rei.examenbackend.repository.QuestionRepository;
import com.rei.examenbackend.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Direct answer access. Every operation is scoped to sessions owned by the caller,
 * and answers can only be changed while their session is still in progress.
 */
@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final ExaminationSessionRepository examinationSessionRepository;

    @Override
    public AnswerResponse create(AnswerRequest request, User user) {
        Question question = findQuestion(request.getQuestionId(), user);
        ExaminationSession session = findOpenSession(request.getExaminationSessionId(), user);

        Answer answer = Answer.builder()
                .answerText(request.getAnswerText())
                .correct(request.isCorrect())
                .reflectionText(request.getReflectionText())
                .feelingScore(request.getFeelingScore())
                .question(question)
                .examinationSession(session)
                .build();

        return toResponse(answerRepository.save(answer));
    }

    @Override
    public AnswerResponse update(Long id, AnswerRequest request, User user) {
        Answer answer = findAnswer(id, user);
        requireOpen(answer.getExaminationSession());
        Question question = findQuestion(request.getQuestionId(), user);
        ExaminationSession session = findOpenSession(request.getExaminationSessionId(), user);

        answer.setAnswerText(request.getAnswerText());
        answer.setCorrect(request.isCorrect());
        answer.setReflectionText(request.getReflectionText());
        answer.setFeelingScore(request.getFeelingScore());
        answer.setQuestion(question);
        answer.setExaminationSession(session);

        return toResponse(answerRepository.save(answer));
    }

    @Override
    public void delete(Long id, User user) {
        Answer answer = findAnswer(id, user);
        requireOpen(answer.getExaminationSession());
        answerRepository.delete(answer);
    }

    @Override
    public AnswerResponse getById(Long id, User user) {
        return toResponse(findAnswer(id, user));
    }

    @Override
    public List<AnswerResponse> getByQuestion(Long questionId, User user) {
        return answerRepository.findByQuestionId(questionId)
                .stream()
                .filter(a -> ownsSession(a.getExaminationSession(), user))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<AnswerResponse> getBySession(Long sessionId, User user) {
        ExaminationSession session = findSession(sessionId, user);
        return answerRepository.findByExaminationSession(session)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AnswerResponse toResponse(Answer a) {
        return AnswerResponse.builder()
                .id(a.getId())
                .answerText(a.getAnswerText())
                .correct(a.isCorrect())
                .reflectionText(a.getReflectionText())
                .feelingScore(a.getFeelingScore())
                .questionId(a.getQuestion().getId())
                .examinationSessionId(a.getExaminationSession().getId())
                .build();
    }

    private Answer findAnswer(Long id, User user) {
        return answerRepository.findById(id)
                .filter(a -> ownsSession(a.getExaminationSession(), user))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Answer not found"));
    }

    private Question findQuestion(Long id, User user) {
        return questionRepository.findById(id)
                .filter(q -> !q.isArchived())
                .filter(q -> q.getOwner() == null || q.getOwner().getId().equals(user.getId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found"));
    }

    private ExaminationSession findSession(Long id, User user) {
        return examinationSessionRepository.findById(id)
                .filter(s -> ownsSession(s, user))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Session not found"));
    }

    private ExaminationSession findOpenSession(Long id, User user) {
        ExaminationSession session = findSession(id, user);
        requireOpen(session);
        return session;
    }

    private void requireOpen(ExaminationSession session) {
        if (session.getCompletedAt() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "Answers of a completed session cannot be changed");
        }
    }

    private boolean ownsSession(ExaminationSession session, User user) {
        return session != null && session.getUser() != null && user != null
                && session.getUser().getId().equals(user.getId());
    }
}
