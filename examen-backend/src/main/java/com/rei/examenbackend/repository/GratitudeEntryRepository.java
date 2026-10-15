package com.rei.examenbackend.repository;

import com.rei.examenbackend.model.GratitudeEntry;
import com.rei.examenbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface GratitudeEntryRepository extends JpaRepository<GratitudeEntry, Long> {
    List<GratitudeEntry> findByUserOrderByCreatedAtDesc(User user);
    List<GratitudeEntry> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(User user, LocalDateTime start, LocalDateTime end);

    void deleteByUser(User user);
}
