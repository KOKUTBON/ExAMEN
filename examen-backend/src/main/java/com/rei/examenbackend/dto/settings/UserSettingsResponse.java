package com.rei.examenbackend.dto.settings;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSettingsResponse {
    private String timeZone;
    private String reminderTime;
    private String theme;
    private Boolean emailReminder;
    private Boolean inAppReminder;
}
