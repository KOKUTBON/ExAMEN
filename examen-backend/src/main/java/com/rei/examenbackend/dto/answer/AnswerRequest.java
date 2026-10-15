package com.rei.examenbackend.dto.answer;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AnswerRequest {

    @NotBlank
    private String answerText;
    private boolean correct;

    @NotBlank
    @Size(max = 2000)
    private String reflectionText;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer feelingScore;
    @NotNull
    private Long questionId;

    @NotNull
    private Long examinationSessionId;

    public AnswerRequest() {}

    public AnswerRequest(String answerText, boolean correct, String reflectionText, Integer feelingScore,
                         Long questionId, Long examinationSessionId) {
        this.answerText = answerText;
        this.correct = correct;
        this.reflectionText = reflectionText;
        this.feelingScore = feelingScore;
        this.questionId = questionId;
        this.examinationSessionId = examinationSessionId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public String getReflectionText() {
        return reflectionText;
    }

    public void setReflectionText(String reflectionText) {
        this.reflectionText = reflectionText;
    }

    public Integer getFeelingScore() {
        return feelingScore;
    }

    public void setFeelingScore(Integer feelingScore) {
        this.feelingScore = feelingScore;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getExaminationSessionId() {
        return examinationSessionId;
    }

    public void setExaminationSessionId(Long examinationSessionId) {
        this.examinationSessionId = examinationSessionId;
    }
}
