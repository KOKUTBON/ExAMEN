package com.rei.examenbackend.service;

import com.rei.examenbackend.config.PasswordResetProperties;
import com.rei.examenbackend.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PasswordResetMailService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetMailService.class);

    private final JavaMailSender mailSender;
    private final PasswordResetProperties properties;
    private final String mailHost;

    public PasswordResetMailService(
            JavaMailSender mailSender,
            PasswordResetProperties properties,
            @Value("${spring.mail.host:}") String mailHost
    ) {
        this.mailSender = mailSender;
        this.properties = properties;
        this.mailHost = mailHost;
    }

    public void sendResetEmail(User user, String token) {
        String resetLink = UriComponentsBuilder.fromUriString(properties.getResetUrl())
                .queryParam("token", token)
                .build()
                .toUriString();

        if (!properties.isMailEnabled() || !StringUtils.hasText(mailHost)) {
            // Without SMTP the operator can hand the link to the user out of band.
            logger.warn("Password reset email is disabled or no mail host is configured. Reset link for {}: {}",
                    user.getEmail(), resetLink);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        if (StringUtils.hasText(properties.getFrom())) {
            message.setFrom(properties.getFrom());
        }
        message.setSubject("Reset your Examen password");
        message.setText(String.format(
                "Hi %s,%n%n" +
                "We received a request to reset your Examen password.%n%n" +
                "Use this link to set a new password:%n%s%n%n" +
                "If you did not request this, you can safely ignore this email.",
                user.getFullName(),
                resetLink
        ));

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            logger.error("Could not send password reset email to {}", user.getEmail(), ex);
        }
    }
}
