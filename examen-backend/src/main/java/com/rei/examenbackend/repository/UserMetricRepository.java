package com.rei.examenbackend.repository;

import com.rei.examenbackend.model.User;
import com.rei.examenbackend.model.UserMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserMetricRepository extends JpaRepository<UserMetric, Long> {
    Optional<UserMetric> findByUserAndMetricDate(User user, LocalDate date);
    List<UserMetric> findByUserOrderByMetricDateDesc(User user);

    void deleteByUser(User user);
}
