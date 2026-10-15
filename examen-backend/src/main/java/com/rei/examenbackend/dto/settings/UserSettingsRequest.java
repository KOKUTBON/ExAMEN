package com.rei.examenbackend.dto.settings;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserSettingsRequest {
    @NotBlank
    private String timeZone;
    private String reminderTime; // HH:mm
    private String theme;
    private Boolean emailReminder;
    private Boolean inAppReminder;
}
