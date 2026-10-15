package com.rei.examenbackend.dto.insights;

import java.time.LocalDateTime;

public class InsightFeedbackResponse {
    private Long id;
    private String targetType;
    private String targetId;
    private boolean helpful;
    private String comments;
    private LocalDateTime createdAt;

    public InsightFeedbackResponse() {}

    public InsightFeedbackResponse(Long id, String targetType, String targetId,
                                   boolean helpful, String comments, LocalDateTime createdAt) {
        this.id = id;
        this.targetType = targetType;
        this.targetId = targetId;
        this.helpful = helpful;
        this.comments = comments;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public boolean isHelpful() { return helpful; }
    public void setHelpful(boolean helpful) { this.helpful = helpful; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
