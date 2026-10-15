package com.rei.examenbackend.service;

import com.rei.examenbackend.config.ReminderProperties;
import com.rei.examenbackend.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class ReminderMailService {
    private static final Logger logger = LoggerFactory.getLogger(ReminderMailService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

    private final JavaMailSender mailSender;
    private final ReminderProperties properties;
    private final String mailHost;

    public ReminderMailService(JavaMailSender mailSender,
                               ReminderProperties properties,
                               @Value("${spring.mail.host:}") String mailHost) {
        this.mailSender = mailSender;
        this.properties = properties;
        this.mailHost = mailHost;
    }

    public boolean sendReminder(User user, LocalDate date, LocalTime time, ZoneId zoneId) {
        if (!properties.isMailEnabled() || !StringUtils.hasText(mailHost)) {
            logger.debug("Reminder email skipped for {} because mail is disabled", user.getEmail());
            return false;
        }

        String dateText = DATE_FORMAT.format(date);
        String timeText = TIME_FORMAT.format(time);
        String messageBody = String.format(
                "Hi %s,%n%n" +
                "This is your daily Examen reminder.%n" +
                "Scheduled time: %s (%s)%n" +
                "Date: %s%n%n" +
                "Start your examination here:%n%s%n%n" +
                "If you no longer want reminders, update your settings in Examen.",
                user.getFullName(),
                timeText,
                zoneId,
                dateText,
                properties.getAppUrl()
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        if (StringUtils.hasText(properties.getFrom())) {
            message.setFrom(properties.getFrom());
        }
        message.setSubject(properties.getSubject());
        message.setText(messageBody);

        mailSender.send(message);
        return true;
    }
}
