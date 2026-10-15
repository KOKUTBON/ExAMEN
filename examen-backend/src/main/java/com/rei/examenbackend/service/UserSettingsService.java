package com.rei.examenbackend.service;

import com.rei.examenbackend.dto.settings.UserSettingsRequest;
import com.rei.examenbackend.dto.settings.UserSettingsResponse;
import com.rei.examenbackend.model.User;

public interface UserSettingsService {
    UserSettingsResponse get(User user);
    UserSettingsResponse update(UserSettingsRequest request, User user);
}
