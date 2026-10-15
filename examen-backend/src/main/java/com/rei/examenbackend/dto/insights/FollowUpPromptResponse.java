package com.rei.examenbackend.dto.insights;

import java.time.LocalDateTime;

public class FollowUpPromptResponse {
    private String question;
    private String rationale;
    private String sourceContext;
    private String targetFocus;
    private LocalDateTime generatedAt;

    public FollowUpPromptResponse() {
        this.generatedAt = LocalDateTime.now();
    }

    public FollowUpPromptResponse(String question, String rationale, String sourceContext, String targetFocus) {
        this.question = question;
        this.rationale = rationale;
        this.sourceContext = sourceContext;
        this.targetFocus = targetFocus;
        this.generatedAt = LocalDateTime.now();
    }

    public FollowUpPromptResponse(String question, String rationale, String sourceContext, String targetFocus, LocalDateTime generatedAt) {
        this.question = question;
        this.rationale = rationale;
        this.sourceContext = sourceContext;
        this.targetFocus = targetFocus;
        this.generatedAt = generatedAt;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public String getSourceContext() {
        return sourceContext;
    }

    public void setSourceContext(String sourceContext) {
        this.sourceContext = sourceContext;
    }

    public String getTargetFocus() {
        return targetFocus;
    }

    public void setTargetFocus(String targetFocus) {
        this.targetFocus = targetFocus;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
