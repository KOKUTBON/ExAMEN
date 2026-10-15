package com.rei.examenbackend.dto.insights;

import com.rei.examenbackend.model.SemanticTagCategory;
import java.time.LocalDateTime;

public class SemanticTagResponse {
    private Long id;
    private Long sessionId;
    private Long answerId;
    private SemanticTagCategory category;
    private String tag;
    private Double confidence;
    private LocalDateTime createdAt;

    public SemanticTagResponse() {}

    public SemanticTagResponse(Long id, Long sessionId, Long answerId, SemanticTagCategory category,
                               String tag, Double confidence, LocalDateTime createdAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.answerId = answerId;
        this.category = category;
        this.tag = tag;
        this.confidence = confidence;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getAnswerId() { return answerId; }
    public void setAnswerId(Long answerId) { this.answerId = answerId; }

    public SemanticTagCategory getCategory() { return category; }
    public void setCategory(SemanticTagCategory category) { this.category = category; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
