package com.rei.examenbackend.dto.growth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GratitudeRequest {
    @NotBlank
    @Size(max = 1000)
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
