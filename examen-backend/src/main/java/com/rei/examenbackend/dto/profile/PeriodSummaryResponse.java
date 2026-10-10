package com.rei.examenbackend.dto.profile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PeriodSummaryResponse {
    private String period; // week or month
    private long sessions;
    private long completedDays;
    private Double averageMood;
}
