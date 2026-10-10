package com.rei.examenbackend.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedScoreDetails {
    private Double compositeScore;         // 0-100 composite
    private Double consistencyScore;       // 0-100 completion rhythm & streak
    private Double moodStabilityScore;     // 0-100 emotional stability & baseline
    private Double virtueStruggleRatio;    // 0-100 ratio of virtues/gratitude vs struggles/stressors
    private Double goalExecutionScore;     // 0-100 todo completion & habit scores
    private Double confidenceLower;        // Lower bound of 95% confidence interval
    private Double confidenceUpper;        // Upper bound of 95% confidence interval
    private Double confidenceLevel;        // e.g. 0.95
    private Double marginOfError;          // e.g. 4.2
    private Integer sampleSize;            // Number of sessions/data points
    private String scoringRationale;       // Human-readable statistical explanation
}
