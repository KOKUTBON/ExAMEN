package com.rei.examenbackend.controller;

import com.rei.examenbackend.dto.notification.NotificationResponse;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.service.UserNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final UserNotificationService notificationService;

    public NotificationController(UserNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list(@AuthenticationPrincipal User user,
                                                           @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return ResponseEntity.ok(notificationService.list(user, unreadOnly));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id,
                                         @AuthenticationPrincipal User user) {
        notificationService.markRead(id, user);
        return ResponseEntity.noContent().build();
    }
}
