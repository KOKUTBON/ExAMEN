package com.rei.examenbackend.dto.journal;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JournalEntryResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
}
