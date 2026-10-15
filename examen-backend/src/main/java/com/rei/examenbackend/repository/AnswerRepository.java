package com.rei.examenbackend.repository;

import com.rei.examenbackend.model.Answer;
import com.rei.examenbackend.model.ExaminationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByQuestionId(Long questionId);

    List<Answer> findByExaminationSession(ExaminationSession session);
}
