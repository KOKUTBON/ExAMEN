package com.rei.examenbackend.dto.insights;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class InsightFeedbackRequest {

    @NotBlank(message = "Target type is required")
    private String targetType;

    private String targetId;

    @NotNull(message = "Helpful rating is required")
    private Boolean helpful;

    private String comments;

    public InsightFeedbackRequest() {}

    public InsightFeedbackRequest(String targetType, String targetId, Boolean helpful, String comments) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.helpful = helpful;
        this.comments = comments;
    }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public Boolean getHelpful() { return helpful; }
    public void setHelpful(Boolean helpful) { this.helpful = helpful; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
