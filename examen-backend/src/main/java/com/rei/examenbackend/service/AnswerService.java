package com.rei.examenbackend.service;

import com.rei.examenbackend.dto.answer.AnswerRequest;
import com.rei.examenbackend.dto.answer.AnswerResponse;
import com.rei.examenbackend.model.User;

import java.util.List;

public interface AnswerService {

    AnswerResponse create(AnswerRequest request, User user);

    AnswerResponse update(Long id, AnswerRequest request, User user);

    void delete(Long id, User user);

    AnswerResponse getById(Long id, User user);

    List<AnswerResponse> getByQuestion(Long questionId, User user);

    List<AnswerResponse> getBySession(Long sessionId, User user);
}
