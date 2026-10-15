package com.rei.examenbackend.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityMetrics {
    private Double feedbackSatisfactionRate; // Ratio of positive ratings (0.0 - 1.0)
    private Long totalFeedbackCount;          // Total user ratings
    private String sentimentDriftIndicator;   // "STABLE", "POSITIVE_DRIFT", "NEGATIVE_DRIFT"
    private Integer evaluatedPeriodDays;       // e.g. 30
}
