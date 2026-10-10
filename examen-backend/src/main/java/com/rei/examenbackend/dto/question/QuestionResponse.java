package com.rei.examenbackend.dto.question;

import com.rei.examenbackend.dto.category.CategoryResponse;
import com.rei.examenbackend.model.QuestionType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestionResponse {
    private Long id;
    private String text;
    private Integer orderNumber;
    private String difficulty;
    private QuestionType responseType;
    private CategoryResponse category;
    private boolean custom;
    private boolean defaultQuestion;
}
