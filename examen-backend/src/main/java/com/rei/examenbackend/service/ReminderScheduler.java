package com.rei.examenbackend.service;

import com.rei.examenbackend.config.ReminderProperties;
import com.rei.examenbackend.model.UserSettings;
import com.rei.examenbackend.repository.UserSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReminderScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ReminderScheduler.class);

    private final UserSettingsRepository settingsRepository;
    private final ReminderMailService reminderMailService;
    private final UserNotificationService notificationService;
    private final ReminderProperties reminderProperties;

    public ReminderScheduler(UserSettingsRepository settingsRepository,
                             ReminderMailService reminderMailService,
                             UserNotificationService notificationService,
                             ReminderProperties reminderProperties) {
        this.settingsRepository = settingsRepository;
        this.reminderMailService = reminderMailService;
        this.notificationService = notificationService;
        this.reminderProperties = reminderProperties;
    }

    @Scheduled(cron = "${app.reminder.cron:0 * * * * *}")
    public void deliverReminders() {
        if (!reminderProperties.isEnabled()) {
            return;
        }

        List<UserSettings> settingsList = settingsRepository.findReminderCandidates();
        for (UserSettings settings : settingsList) {
            LocalTime reminderTime = settings.getReminderTime();
            if (reminderTime == null) {
                continue;
            }

            ZoneId zoneId = resolveZone(settings.getTimeZone());
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            LocalTime nowTime = now.toLocalTime().truncatedTo(ChronoUnit.MINUTES);
            if (!nowTime.equals(reminderTime)) {
                continue;
            }

            LocalDate today = now.toLocalDate();
            if (today.equals(settings.getLastReminderSentDate())) {
                continue;
            }

            boolean delivered = false;
            if (Boolean.TRUE.equals(settings.getEmailReminder())) {
                delivered |= reminderMailService.sendReminder(settings.getUser(), today, reminderTime, zoneId);
            }

            if (Boolean.TRUE.equals(settings.getInAppReminder())) {
                notificationService.createReminder(
                        settings.getUser(),
                        reminderProperties.getInAppTitle(),
                        reminderProperties.getInAppMessage()
                );
                delivered = true;
            }

            if (delivered) {
                settings.setLastReminderSentDate(today);
                settingsRepository.save(settings);
            } else {
                logger.debug("Reminder skipped for user {} due to delivery settings", settings.getUser().getEmail());
            }
        }
    }

    private ZoneId resolveZone(String timeZone) {
        if (timeZone == null || timeZone.isBlank()) {
            return ZoneId.systemDefault();
        }
        try {
            return ZoneId.of(timeZone);
        } catch (Exception e) {
            logger.warn("Invalid time zone {}. Falling back to system default.", timeZone);
            return ZoneId.systemDefault();
        }
    }
}
