package com.rei.examenbackend.dto.insights;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class QuestionSuggestionRequest {
    private String focus;

    @Min(1)
    @Max(10)
    private Integer count;

    public QuestionSuggestionRequest() {}

    public QuestionSuggestionRequest(String focus, Integer count) {
        this.focus = focus;
        this.count = count;
    }

    public String getFocus() { return focus; }
    public void setFocus(String focus) { this.focus = focus; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}
