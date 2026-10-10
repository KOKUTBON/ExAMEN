package com.rei.examenbackend.dto.session;

import com.rei.examenbackend.dto.answer.AnswerResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ExaminationSessionResponse {
    private Long id;
    private Long userId;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private List<AnswerResponse> answers;
    private String notes;
    private Integer moodScore;
    private Double score;
    private Map<String, Double> categoryScores;

    public ExaminationSessionResponse() {}

    public ExaminationSessionResponse(Long id, Long userId, LocalDateTime startedAt, LocalDateTime completedAt,
                                      List<AnswerResponse> answers, String notes, Integer moodScore,
                                      Double score, Map<String, Double> categoryScores) {
        this.id = id;
        this.userId = userId;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.answers = answers;
        this.notes = notes;
        this.moodScore = moodScore;
        this.score = score;
        this.categoryScores = categoryScores;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public List<AnswerResponse> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerResponse> answers) {
        this.answers = answers;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getMoodScore() {
        return moodScore;
    }

    public void setMoodScore(Integer moodScore) {
        this.moodScore = moodScore;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Map<String, Double> getCategoryScores() {
        return categoryScores;
    }

    public void setCategoryScores(Map<String, Double> categoryScores) {
        this.categoryScores = categoryScores;
    }

    public static class Builder {
        private Long id;
        private Long userId;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private List<AnswerResponse> answers;
        private String notes;
        private Integer moodScore;
        private Double score;
        private Map<String, Double> categoryScores;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder startedAt(LocalDateTime startedAt) { this.startedAt = startedAt; return this; }
        public Builder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }
        public Builder answers(List<AnswerResponse> answers) { this.answers = answers; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder moodScore(Integer moodScore) { this.moodScore = moodScore; return this; }
        public Builder score(Double score) { this.score = score; return this; }
        public Builder categoryScores(Map<String, Double> categoryScores) { this.categoryScores = categoryScores; return this; }

        public ExaminationSessionResponse build() {
            return new ExaminationSessionResponse(id, userId, startedAt, completedAt, answers, notes, moodScore, score, categoryScores);
        }
    }
}
