package com.rei.examenbackend.dto.journal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JournalEntryRequest {
    @NotBlank
    @Size(max = 2000)
    private String content;
}
