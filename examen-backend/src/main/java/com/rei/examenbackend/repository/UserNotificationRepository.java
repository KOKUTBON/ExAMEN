package com.rei.examenbackend.repository;

import com.rei.examenbackend.model.User;
import com.rei.examenbackend.model.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    List<UserNotification> findByUserOrderByCreatedAtDesc(User user);

    List<UserNotification> findByUserAndReadAtIsNullOrderByCreatedAtDesc(User user);

    Optional<UserNotification> findByIdAndUser(Long id, User user);

    void deleteByUser(User user);
}
