package com.rei.examenbackend.service;

import com.rei.examenbackend.dto.notification.NotificationResponse;
import com.rei.examenbackend.exception.ApiException;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.model.UserNotification;
import com.rei.examenbackend.repository.UserNotificationRepository;
import com.rei.examenbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserNotificationService {
    private final UserNotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public UserNotificationService(UserNotificationRepository notificationRepository,
                                   UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public List<NotificationResponse> list(User user, boolean unreadOnly) {
        User persisted = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        List<UserNotification> notifications = unreadOnly
                ? notificationRepository.findByUserAndReadAtIsNullOrderByCreatedAtDesc(persisted)
                : notificationRepository.findByUserOrderByCreatedAtDesc(persisted);

        return notifications.stream()
                .map(this::toResponse)
                .toList();
    }

    public void markRead(Long id, User user) {
        User persisted = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        UserNotification notification = notificationRepository.findByIdAndUser(id, persisted)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found"));

        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    public void createReminder(User user, String title, String message) {
        UserNotification notification = UserNotification.builder()
                .user(user)
                .title(title)
                .message(message)
                .notificationType("REMINDER")
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    private NotificationResponse toResponse(UserNotification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getNotificationType(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
