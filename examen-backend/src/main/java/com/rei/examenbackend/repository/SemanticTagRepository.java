package com.rei.examenbackend.repository;

import com.rei.examenbackend.model.ExaminationSession;
import com.rei.examenbackend.model.SemanticTag;
import com.rei.examenbackend.model.SemanticTagCategory;
import com.rei.examenbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SemanticTagRepository extends JpaRepository<SemanticTag, Long> {
    List<SemanticTag> findBySession(ExaminationSession session);

    List<SemanticTag> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(User user, LocalDateTime start, LocalDateTime end);

    List<SemanticTag> findByUserAndCategoryAndCreatedAtBetweenOrderByCreatedAtDesc(
            User user, SemanticTagCategory category, LocalDateTime start, LocalDateTime end
    );

    List<SemanticTag> findByUserOrderByCreatedAtDesc(User user);

    void deleteBySession(ExaminationSession session);

    void deleteByUser(User user);
}
