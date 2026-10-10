package com.rei.examenbackend.dto.insights;

import java.time.LocalDateTime;
import java.util.List;

public class SemanticTagSummaryResponse {
    private int periodDays;
    private int totalTags;
    private List<TagCount> virtues;
    private List<TagCount> struggles;
    private List<TagCount> gratitude;
    private List<TagCount> stressors;
    private LocalDateTime generatedAt;

    public SemanticTagSummaryResponse() {}

    public SemanticTagSummaryResponse(int periodDays, int totalTags, List<TagCount> virtues,
                                      List<TagCount> struggles, List<TagCount> gratitude,
                                      List<TagCount> stressors, LocalDateTime generatedAt) {
        this.periodDays = periodDays;
        this.totalTags = totalTags;
        this.virtues = virtues;
        this.struggles = struggles;
        this.gratitude = gratitude;
        this.stressors = stressors;
        this.generatedAt = generatedAt;
    }

    public int getPeriodDays() { return periodDays; }
    public void setPeriodDays(int periodDays) { this.periodDays = periodDays; }

    public int getTotalTags() { return totalTags; }
    public void setTotalTags(int totalTags) { this.totalTags = totalTags; }

    public List<TagCount> getVirtues() { return virtues; }
    public void setVirtues(List<TagCount> virtues) { this.virtues = virtues; }

    public List<TagCount> getStruggles() { return struggles; }
    public void setStruggles(List<TagCount> struggles) { this.struggles = struggles; }

    public List<TagCount> getGratitude() { return gratitude; }
    public void setGratitude(List<TagCount> gratitude) { this.gratitude = gratitude; }

    public List<TagCount> getStressors() { return stressors; }
    public void setStressors(List<TagCount> stressors) { this.stressors = stressors; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public static class TagCount {
        private String tag;
        private long count;

        public TagCount() {}

        public TagCount(String tag, long count) {
            this.tag = tag;
            this.count = count;
        }

        public String getTag() { return tag; }
        public void setTag(String tag) { this.tag = tag; }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }
}
