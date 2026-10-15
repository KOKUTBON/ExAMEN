package com.rei.examenbackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "semantic_tags", indexes = {
        @Index(name = "idx_semantic_tags_user", columnList = "user_id, created_at"),
        @Index(name = "idx_semantic_tags_session", columnList = "session_id"),
        @Index(name = "idx_semantic_tags_category", columnList = "category")
})
public class SemanticTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private ExaminationSession session;

    @ManyToOne
    @JoinColumn(name = "answer_id")
    private Answer answer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SemanticTagCategory category;

    @Column(nullable = false, length = 128)
    private String tag;

    private Double confidence;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public SemanticTag() {}

    public SemanticTag(Long id, User user, ExaminationSession session, Answer answer,
                       SemanticTagCategory category, String tag, Double confidence, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.session = session;
        this.answer = answer;
        this.category = category;
        this.tag = tag;
        this.confidence = confidence;
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

    public ExaminationSession getSession() {
        return session;
    }

    public void setSession(ExaminationSession session) {
        this.session = session;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    public SemanticTagCategory getCategory() {
        return category;
    }

    public void setCategory(SemanticTagCategory category) {
        this.category = category;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
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
        private ExaminationSession session;
        private Answer answer;
        private SemanticTagCategory category;
        private String tag;
        private Double confidence = 1.0;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder session(ExaminationSession session) { this.session = session; return this; }
        public Builder answer(Answer answer) { this.answer = answer; return this; }
        public Builder category(SemanticTagCategory category) { this.category = category; return this; }
        public Builder tag(String tag) { this.tag = tag; return this; }
        public Builder confidence(Double confidence) { this.confidence = confidence; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public SemanticTag build() {
            return new SemanticTag(id, user, session, answer, category, tag, confidence, createdAt);
        }
    }
}
