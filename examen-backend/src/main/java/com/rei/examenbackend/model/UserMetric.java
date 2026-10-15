package com.rei.examenbackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_metrics",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_metric_date", columnNames = {"user_id", "metric_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    private Integer sessionsCompleted;

    private Double averageMood;

    private Double averageScore;

    private LocalDateTime createdAt;
}
