package com.rei.examenbackend.repository;

import com.rei.examenbackend.model.InsightFeedback;
import com.rei.examenbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InsightFeedbackRepository extends JpaRepository<InsightFeedback, Long> {
    List<InsightFeedback> findByUserOrderByCreatedAtDesc(User user);

    List<InsightFeedback> findByUserAndTargetTypeOrderByCreatedAtDesc(User user, String targetType);

    List<InsightFeedback> findByUserAndCreatedAtAfterOrderByCreatedAtDesc(User user, LocalDateTime after);

    void deleteByUser(User user);
}
