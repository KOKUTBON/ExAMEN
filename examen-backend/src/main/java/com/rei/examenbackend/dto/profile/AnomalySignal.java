package com.rei.examenbackend.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalySignal {
    private String type;         // e.g. "MOOD_DROP", "STREAK_BREAK", "STRESSOR_SURGE", "BREAKTHROUGH"
    private String severity;     // "LOW", "MEDIUM", "HIGH"
    private String title;        // Short header description
    private String description;  // Contextual explanation and advice
    private LocalDate detectedDate;
    private Double deviation;    // Numerical deviation magnitude
}
