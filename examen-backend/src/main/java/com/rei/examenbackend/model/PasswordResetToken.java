package com.rei.examenbackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens",
        indexes = @Index(name = "idx_reset_token_user", columnList = "user_id"))
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private boolean used;

    public PasswordResetToken() {}

    public PasswordResetToken(Long id, String token, User user, LocalDateTime expiresAt, LocalDateTime createdAt, boolean used) {
        this.id = id;
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.used = used;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

    public static class Builder {
        private Long id;
        private String token;
        private User user;
        private LocalDateTime expiresAt;
        private LocalDateTime createdAt;
        private boolean used;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder token(String token) { this.token = token; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder used(boolean used) { this.used = used; return this; }

        public PasswordResetToken build() {
            return new PasswordResetToken(id, token, user, expiresAt, createdAt, used);
        }
    }
}
