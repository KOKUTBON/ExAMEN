package com.rei.examenbackend.dto.profile;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ProfileProgressResponse {
    private List<ProgressPoint> points;

    @Data
    @Builder
    public static class ProgressPoint {
        private LocalDate date;
        private boolean completed;
        private Integer mood;
    }
}
