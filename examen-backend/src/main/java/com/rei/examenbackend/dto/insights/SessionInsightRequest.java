package com.rei.examenbackend.dto.insights;

import jakarta.validation.constraints.NotNull;

public class SessionInsightRequest {
    @NotNull
    private Long sessionId;

    public SessionInsightRequest() {}

    public SessionInsightRequest(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
}
