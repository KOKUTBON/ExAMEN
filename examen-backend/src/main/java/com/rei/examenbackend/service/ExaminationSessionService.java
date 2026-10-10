package com.rei.examenbackend.service;

import com.rei.examenbackend.dto.session.ExaminationSessionResponse;
import com.rei.examenbackend.dto.session.ExaminationSessionSubmitRequest;
import com.rei.examenbackend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface ExaminationSessionService {

    ExaminationSessionResponse startSession(User user);

    ExaminationSessionResponse submitAnswers(Long sessionId, ExaminationSessionSubmitRequest request, User user);

    ExaminationSessionResponse getById(Long id, User user);

    Page<ExaminationSessionResponse> getByUser(User user, Pageable pageable);

    Optional<ExaminationSessionResponse> getActive(User user);
}
