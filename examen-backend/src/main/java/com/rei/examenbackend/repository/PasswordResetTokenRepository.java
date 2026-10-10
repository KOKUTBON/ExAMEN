package com.rei.examenbackend.repository;

import com.rei.examenbackend.model.PasswordResetToken;
import com.rei.examenbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    long countByUserAndCreatedAtAfter(User user, LocalDateTime createdAfter);

    void deleteByUser(User user);
}
