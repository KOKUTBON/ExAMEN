package com.rei.examenbackend.repository;

import com.rei.examenbackend.model.User;
import com.rei.examenbackend.model.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    Optional<UserSettings> findByUser(User user);

    @Query("""
            select s from UserSettings s
            join fetch s.user
            where s.reminderTime is not null
            and (s.emailReminder = true or s.inAppReminder = true)
            """)
    List<UserSettings> findReminderCandidates();

    void deleteByUser(User user);
}
