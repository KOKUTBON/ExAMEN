package com.rei.examenbackend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The actual answer text (fixes AnswerBuilder errors)
    @Column(nullable = false)
    private String answerText;

    // Whether this answer is the correct one
    private boolean correct;

    @Column(name = "reflection_text", length = 2000)
    private String reflectionText;

    @Column(name = "feeling_score")
    private Integer feelingScore;

    // RELATION WITH QUESTION
    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // RELATION WITH EXAM SESSION
    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private ExaminationSession examinationSession;

    public Answer() {}

    public Answer(Long id, String answerText, boolean correct, String reflectionText, Integer feelingScore,
                  Question question, ExaminationSession examinationSession) {
        this.id = id;
        this.answerText = answerText;
        this.correct = correct;
        this.reflectionText = reflectionText;
        this.feelingScore = feelingScore;
        this.question = question;
        this.examinationSession = examinationSession;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }

    public String getReflectionText() { return reflectionText; }
    public void setReflectionText(String reflectionText) { this.reflectionText = reflectionText; }

    public Integer getFeelingScore() { return feelingScore; }
    public void setFeelingScore(Integer feelingScore) { this.feelingScore = feelingScore; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    public ExaminationSession getExaminationSession() { return examinationSession; }
    public void setExaminationSession(ExaminationSession examinationSession) { this.examinationSession = examinationSession; }

    public static class Builder {
        private Long id;
        private String answerText;
        private boolean correct;
        private String reflectionText;
        private Integer feelingScore;
        private Question question;
        private ExaminationSession examinationSession;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder answerText(String answerText) { this.answerText = answerText; return this; }
        public Builder correct(boolean correct) { this.correct = correct; return this; }
        public Builder reflectionText(String reflectionText) { this.reflectionText = reflectionText; return this; }
        public Builder feelingScore(Integer feelingScore) { this.feelingScore = feelingScore; return this; }
        public Builder question(Question question) { this.question = question; return this; }
        public Builder examinationSession(ExaminationSession examinationSession) { this.examinationSession = examinationSession; return this; }

        public Answer build() {
            return new Answer(id, answerText, correct, reflectionText, feelingScore, question, examinationSession);
        }
    }
}
