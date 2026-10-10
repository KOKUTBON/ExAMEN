package com.rei.examenbackend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "habit_scores")
public class HabitScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String habit;

    @Column(nullable = false)
    private Integer score; // 1-5

    @Column(name = "score_date", nullable = false)
    private LocalDate scoreDate;

    public HabitScore() {}

    public HabitScore(Long id, User user, String habit, Integer score, LocalDate scoreDate) {
        this.id = id;
        this.user = user;
        this.habit = habit;
        this.score = score;
        this.scoreDate = scoreDate;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getHabit() { return habit; }
    public void setHabit(String habit) { this.habit = habit; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public LocalDate getScoreDate() { return scoreDate; }
    public void setScoreDate(LocalDate scoreDate) { this.scoreDate = scoreDate; }

    public static class Builder {
        private Long id;
        private User user;
        private String habit;
        private Integer score;
        private LocalDate scoreDate;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder habit(String habit) { this.habit = habit; return this; }
        public Builder score(Integer score) { this.score = score; return this; }
        public Builder scoreDate(LocalDate scoreDate) { this.scoreDate = scoreDate; return this; }
        public HabitScore build() { return new HabitScore(id, user, habit, score, scoreDate); }
    }
}
