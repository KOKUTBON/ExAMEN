package com.rei.examenbackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_notifications",
        indexes = {
                @Index(name = "idx_notifications_user_created_at", columnList = "user_id,created_at"),
                @Index(name = "idx_notifications_user_unread", columnList = "user_id,read_at")
        })
public class UserNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 140)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "notification_type", nullable = false, length = 40)
    private String notificationType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    public UserNotification() {}

    public UserNotification(Long id, User user, String title, String message, String notificationType,
                            LocalDateTime createdAt, LocalDateTime readAt) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }

    public static class Builder {
        private Long id;
        private User user;
        private String title;
        private String message;
        private String notificationType;
        private LocalDateTime createdAt;
        private LocalDateTime readAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder notificationType(String notificationType) { this.notificationType = notificationType; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder readAt(LocalDateTime readAt) { this.readAt = readAt; return this; }

        public UserNotification build() {
            return new UserNotification(id, user, title, message, notificationType, createdAt, readAt);
        }
    }
}
