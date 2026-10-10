package com.rei.examenbackend.dto.growth;

import java.time.LocalDate;

public class HabitScoreResponse {
    private Long id;
    private String habit;
    private Integer score;
    private LocalDate scoreDate;

    public HabitScoreResponse() {}

    public HabitScoreResponse(Long id, String habit, Integer score, LocalDate scoreDate) {
        this.id = id;
        this.habit = habit;
        this.score = score;
        this.scoreDate = scoreDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHabit() {
        return habit;
    }

    public void setHabit(String habit) {
        this.habit = habit;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public LocalDate getScoreDate() {
        return scoreDate;
    }

    public void setScoreDate(LocalDate scoreDate) {
        this.scoreDate = scoreDate;
    }
}
