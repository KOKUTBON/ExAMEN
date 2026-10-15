package com.rei.examenbackend.dto.insights;

import java.time.LocalDateTime;
import java.util.List;

public class SessionInsightResponse {
    private Long sessionId;
    private String summary;
    private List<String> insights;
    private List<String> nextSteps;
    private Double averageFeeling;
    private LocalDateTime generatedAt;

    public SessionInsightResponse() {}

    public SessionInsightResponse(Long sessionId, String summary, List<String> insights,
                                  List<String> nextSteps, Double averageFeeling, LocalDateTime generatedAt) {
        this.sessionId = sessionId;
        this.summary = summary;
        this.insights = insights;
        this.nextSteps = nextSteps;
        this.averageFeeling = averageFeeling;
        this.generatedAt = generatedAt;
    }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<String> getInsights() { return insights; }
    public void setInsights(List<String> insights) { this.insights = insights; }

    public List<String> getNextSteps() { return nextSteps; }
    public void setNextSteps(List<String> nextSteps) { this.nextSteps = nextSteps; }

    public Double getAverageFeeling() { return averageFeeling; }
    public void setAverageFeeling(Double averageFeeling) { this.averageFeeling = averageFeeling; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
