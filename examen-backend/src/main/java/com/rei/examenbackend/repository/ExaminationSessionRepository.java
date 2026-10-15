package com.rei.examenbackend.repository;

import com.rei.examenbackend.model.ExaminationSession;
import com.rei.examenbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExaminationSessionRepository extends JpaRepository<ExaminationSession, Long> {

    List<ExaminationSession> findAllByUserOrderByCompletedAtDesc(User user);

    Page<ExaminationSession> findAllByUserAndCompletedAtIsNotNullOrderByCompletedAtDesc(User user, Pageable pageable);

    Optional<ExaminationSession> findByUserAndCompletedAtIsNull(User user);

    List<ExaminationSession> findAllByUserAndCompletedAtBetweenOrderByCompletedAtDesc(User user, LocalDateTime start, LocalDateTime end);

    List<ExaminationSession> findAllByUserAndCompletedAtIsNotNullAndCompletedAtAfterOrderByCompletedAtDesc(User user, LocalDateTime after);

    void deleteByUser(User user);
}
