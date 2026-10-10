package com.rei.examenbackend.controller;

import com.rei.examenbackend.dto.settings.UserSettingsRequest;
import com.rei.examenbackend.dto.settings.UserSettingsResponse;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.service.UserSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class UserSettingsController {

    private final UserSettingsService settingsService;

    @GetMapping
    public ResponseEntity<UserSettingsResponse> get(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(settingsService.get(user));
    }

    @PutMapping
    public ResponseEntity<UserSettingsResponse> update(
            @Valid @RequestBody UserSettingsRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(settingsService.update(request, user));
    }
}
