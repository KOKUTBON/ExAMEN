package com.rei.examenbackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "examination_sessions",
        indexes = {
                @Index(name = "idx_session_user_completed", columnList = "user_id,completed_at")
        })
public class ExaminationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user that this session belongs to
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // All answers provided in this session
    @OneToMany(mappedBy = "examinationSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    // Optional mood score 1-5
    private Integer moodScore;

    // Optional: summary or notes after the examination
    @Column(length = 1000)
    private String notes;

    public ExaminationSession() {}

    public ExaminationSession(Long id, User user, List<Answer> answers, LocalDateTime startedAt,
                              LocalDateTime completedAt, Integer moodScore, String notes) {
        this.id = id;
        this.user = user;
        this.answers = answers;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.moodScore = moodScore;
        this.notes = notes;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Answer> getAnswers() { return answers; }
    public void setAnswers(List<Answer> answers) { this.answers = answers; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Integer getMoodScore() { return moodScore; }
    public void setMoodScore(Integer moodScore) { this.moodScore = moodScore; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public static class Builder {
        private Long id;
        private User user;
        private List<Answer> answers;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Integer moodScore;
        private String notes;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder answers(List<Answer> answers) { this.answers = answers; return this; }
        public Builder startedAt(LocalDateTime startedAt) { this.startedAt = startedAt; return this; }
        public Builder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }
        public Builder moodScore(Integer moodScore) { this.moodScore = moodScore; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }

        public ExaminationSession build() {
            return new ExaminationSession(id, user, answers, startedAt, completedAt, moodScore, notes);
        }
    }
}
