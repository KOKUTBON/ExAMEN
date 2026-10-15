package com.rei.examenbackend.repository;

import com.rei.examenbackend.model.HabitScore;
import com.rei.examenbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface HabitScoreRepository extends JpaRepository<HabitScore, Long> {
    List<HabitScore> findByUserAndScoreDateBetweenOrderByScoreDateDesc(User user, LocalDate start, LocalDate end);

    List<HabitScore> findByUserOrderByScoreDateDesc(User user);

    void deleteByUser(User user);
}
