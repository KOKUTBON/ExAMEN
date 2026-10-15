package com.rei.examenbackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private String timeZone;

    private LocalTime reminderTime;

    private String theme; // light/dark/system

    private Boolean emailReminder;

    private Boolean inAppReminder;

    private LocalDate lastReminderSentDate;
}
