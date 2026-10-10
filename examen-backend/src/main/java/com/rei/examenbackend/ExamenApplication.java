package com.rei.examenbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class ExamenApplication {
    public static void main(String[] args) {
        // Timestamps are persisted and exchanged as UTC regardless of the host's zone.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(ExamenApplication.class, args);
    }
}
