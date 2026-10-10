package com.rei.examenbackend.controller;

import com.rei.examenbackend.dto.growth.*;
import com.rei.examenbackend.exception.ApiException;
import com.rei.examenbackend.model.GratitudeEntry;
import com.rei.examenbackend.model.HabitScore;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.repository.GratitudeEntryRepository;
import com.rei.examenbackend.repository.HabitScoreRepository;
import com.rei.examenbackend.repository.ExaminationSessionRepository;
import com.rei.examenbackend.repository.ToDoItemRepository;
import com.rei.examenbackend.repository.UserRepository;
import com.rei.examenbackend.service.GrowthReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/growth")
public class GrowthController {

    private final GratitudeEntryRepository gratitudeRepo;
    private final HabitScoreRepository habitRepo;
    private final ExaminationSessionRepository sessionRepo;
    private final ToDoItemRepository todoRepo;
    private final UserRepository userRepo;
    private final GrowthReportService growthReportService;

    public GrowthController(GratitudeEntryRepository gratitudeRepo,
                            HabitScoreRepository habitRepo,
                            ExaminationSessionRepository sessionRepo,
                            ToDoItemRepository todoRepo,
                            UserRepository userRepo,
                            GrowthReportService growthReportService) {
        this.gratitudeRepo = gratitudeRepo;
        this.habitRepo = habitRepo;
        this.sessionRepo = sessionRepo;
        this.todoRepo = todoRepo;
        this.userRepo = userRepo;
        this.growthReportService = growthReportService;
    }

    @PostMapping("/gratitude")
    public ResponseEntity<GratitudeResponse> addGratitude(@AuthenticationPrincipal User user,
                                                          @Valid @RequestBody GratitudeRequest request) {
        User persisted = userRepo.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        GratitudeEntry entry = GratitudeEntry.builder()
                .user(persisted)
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();
        gratitudeRepo.save(entry);
        return ResponseEntity.ok(new GratitudeResponse(entry.getId(), entry.getContent(), entry.getCreatedAt()));
    }

    @GetMapping("/gratitude")
    public ResponseEntity<List<GratitudeResponse>> listGratitude(@AuthenticationPrincipal User user) {
        User persisted = userRepo.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        var entries = gratitudeRepo.findByUserOrderByCreatedAtDesc(persisted)
                .stream()
                .map(e -> new GratitudeResponse(e.getId(), e.getContent(), e.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(entries);
    }

    @PostMapping("/habits")
    public ResponseEntity<HabitScoreResponse> addHabitScore(@AuthenticationPrincipal User user,
                                                            @Valid @RequestBody HabitScoreRequest request) {
        User persisted = userRepo.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        HabitScore score = HabitScore.builder()
                .user(persisted)
                .habit(request.getHabit())
                .score(request.getScore())
                .scoreDate(LocalDate.now())
                .build();
        habitRepo.save(score);
        return ResponseEntity.ok(new HabitScoreResponse(score.getId(), score.getHabit(), score.getScore(), score.getScoreDate()));
    }

    @GetMapping("/habits")
    public ResponseEntity<List<HabitScoreResponse>> listHabits(@AuthenticationPrincipal User user,
                                                               @RequestParam(defaultValue = "30") int days) {
        User persisted = userRepo.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        int safeDays = Math.min(Math.max(days, 1), 365);
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(safeDays - 1);

        var scores = habitRepo.findByUserAndScoreDateBetweenOrderByScoreDateDesc(persisted, start, today)
                .stream()
                .map(score -> new HabitScoreResponse(score.getId(), score.getHabit(), score.getScore(), score.getScoreDate()))
                .toList();

        return ResponseEntity.ok(scores);
    }

    @GetMapping("/weekly-summary")
    public ResponseEntity<WeeklySummaryResponse> weeklySummary(@AuthenticationPrincipal User user) {
        User persisted = userRepo.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);

        long sessions = sessionRepo.findAllByUserAndCompletedAtBetweenOrderByCompletedAtDesc(
                persisted, weekAgo.atStartOfDay(), today.atTime(23,59,59)
        ).stream()
                .filter(s -> {
                    try {
                        return s.getCompletedAt() != null;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();

        long todos = todoRepo.findByUser(persisted).stream()
                .filter(t -> t.isCompleted() && !t.getUpdatedAt().toLocalDate().isBefore(weekAgo))
                .count();

        var habitScores = habitRepo.findByUserAndScoreDateBetweenOrderByScoreDateDesc(persisted, weekAgo, today);
        double avgHabit = habitScores.stream().mapToInt(HabitScore::getScore).average().orElse(0.0);

        long gratitude = gratitudeRepo.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
                persisted, weekAgo.atStartOfDay(), today.atTime(23,59,59)
        ).size();

        WeeklySummaryResponse res = new WeeklySummaryResponse(sessions, todos, habitScores.size(), avgHabit, gratitude);
        return ResponseEntity.ok(res);
    }

    @GetMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPdf(@AuthenticationPrincipal User user) {
        byte[] pdfBytes = growthReportService.buildReport(user);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=examen-summary.pdf")
                .body(pdfBytes);
    }

    @GetMapping("/meditation-suggestions")
    public ResponseEntity<List<String>> suggestions() {
        var suggestions = List.of(
                "Take 5 minutes for mindful breathing.",
                "Do a quick body scan to release tension.",
                "Step outside for a 10-minute walk in silence.",
                "Journal one thing you're grateful for before bed."
        );
        return ResponseEntity.ok(suggestions);
    }
}
