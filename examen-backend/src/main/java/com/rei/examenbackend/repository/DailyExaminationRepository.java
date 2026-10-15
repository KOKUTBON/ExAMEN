package com.rei.examenbackend.repository;

import com.rei.examenbackend.model.DailyExamination;
import com.rei.examenbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyExaminationRepository extends JpaRepository<DailyExamination, Long> {

    Optional<DailyExamination> findByUserAndExamDate(User user, LocalDate date);

    List<DailyExamination> findByUserAndExamDateBetweenOrderByExamDateDesc(User user, LocalDate start, LocalDate end);

    List<DailyExamination> findByUserOrderByExamDateDesc(User user);

    void deleteByUser(User user);
}
