package com.rei.examenbackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "insight_feedbacks", indexes = {
        @Index(name = "idx_insight_feedback_user", columnList = "user_id, created_at"),
        @Index(name = "idx_insight_feedback_target", columnList = "target_type, target_id")
})
public class InsightFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "target_type", nullable = false, length = 64)
    private String targetType;

    @Column(name = "target_id", length = 128)
    private String targetId;

    @Column(name = "is_helpful", nullable = false)
    private boolean helpful;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public InsightFeedback() {}

    public InsightFeedback(Long id, User user, String targetType, String targetId,
                           boolean helpful, String comments, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.targetType = targetType;
        this.targetId = targetId;
        this.helpful = helpful;
        this.comments = comments;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public boolean isHelpful() {
        return helpful;
    }

    public void setHelpful(boolean helpful) {
        this.helpful = helpful;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static class Builder {
        private Long id;
        private User user;
        private String targetType;
        private String targetId;
        private boolean helpful;
        private String comments;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder targetType(String targetType) { this.targetType = targetType; return this; }
        public Builder targetId(String targetId) { this.targetId = targetId; return this; }
        public Builder helpful(boolean helpful) { this.helpful = helpful; return this; }
        public Builder comments(String comments) { this.comments = comments; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public InsightFeedback build() {
            return new InsightFeedback(id, user, targetType, targetId, helpful, comments, createdAt);
        }
    }
}
