package com.rei.examenbackend.dto.answer;

public class AnswerResponse {
    private Long id;
    private String answerText;
    private boolean correct;
    private String reflectionText;
    private Integer feelingScore;
    private Long questionId;
    private Long examinationSessionId;

    public AnswerResponse() {}

    public AnswerResponse(Long id, String answerText, boolean correct, String reflectionText, Integer feelingScore,
                          Long questionId, Long examinationSessionId) {
        this.id = id;
        this.answerText = answerText;
        this.correct = correct;
        this.reflectionText = reflectionText;
        this.feelingScore = feelingScore;
        this.questionId = questionId;
        this.examinationSessionId = examinationSessionId;
    }

    public static Builder builder() {
        return new Builder();
    }

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

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public Long getExaminationSessionId() { return examinationSessionId; }
    public void setExaminationSessionId(Long examinationSessionId) { this.examinationSessionId = examinationSessionId; }

    public static class Builder {
        private Long id;
        private String answerText;
        private boolean correct;
        private String reflectionText;
        private Integer feelingScore;
        private Long questionId;
        private Long examinationSessionId;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder answerText(String answerText) { this.answerText = answerText; return this; }
        public Builder correct(boolean correct) { this.correct = correct; return this; }
        public Builder reflectionText(String reflectionText) { this.reflectionText = reflectionText; return this; }
        public Builder feelingScore(Integer feelingScore) { this.feelingScore = feelingScore; return this; }
        public Builder questionId(Long questionId) { this.questionId = questionId; return this; }
        public Builder examinationSessionId(Long examinationSessionId) { this.examinationSessionId = examinationSessionId; return this; }

        public AnswerResponse build() {
            return new AnswerResponse(id, answerText, correct, reflectionText, feelingScore, questionId, examinationSessionId);
        }
    }
}
