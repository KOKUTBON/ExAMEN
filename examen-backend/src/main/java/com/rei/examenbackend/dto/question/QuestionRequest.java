package com.rei.examenbackend.dto.question;

import com.rei.examenbackend.model.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class QuestionRequest {
    @NotBlank
    @Size(max = 500)
    private String text;
    private Integer orderNumber;
    @NotNull
    private Long categoryId;
    private String difficulty;
    private QuestionType responseType;
}
