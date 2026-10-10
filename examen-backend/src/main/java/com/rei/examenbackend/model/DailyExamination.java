package com.rei.examenbackend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "daily_examinations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "exam_date"}),
        indexes = {
                @Index(name = "idx_daily_exam_user_date", columnList = "user_id,exam_date")
        })
public class DailyExamination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "session_id")
    private ExaminationSession session;

    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    private Integer moodScore;

    @Column(length = 1000)
    private String notes;

    public DailyExamination() {}

    public DailyExamination(Long id, User user, ExaminationSession session, LocalDate examDate, Integer moodScore, String notes) {
        this.id = id;
        this.user = user;
        this.session = session;
        this.examDate = examDate;
        this.moodScore = moodScore;
        this.notes = notes;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ExaminationSession getSession() { return session; }
    public void setSession(ExaminationSession session) { this.session = session; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

    public Integer getMoodScore() { return moodScore; }
    public void setMoodScore(Integer moodScore) { this.moodScore = moodScore; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public static class Builder {
        private Long id;
        private User user;
        private ExaminationSession session;
        private LocalDate examDate;
        private Integer moodScore;
        private String notes;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder session(ExaminationSession session) { this.session = session; return this; }
        public Builder examDate(LocalDate examDate) { this.examDate = examDate; return this; }
        public Builder moodScore(Integer moodScore) { this.moodScore = moodScore; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }

        public DailyExamination build() {
            return new DailyExamination(id, user, session, examDate, moodScore, notes);
        }
    }
}
