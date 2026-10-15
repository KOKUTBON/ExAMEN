package com.rei.examenbackend.controller;

import com.rei.examenbackend.dto.session.ExaminationSessionResponse;
import com.rei.examenbackend.dto.session.ExaminationSessionSubmitRequest;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.service.ExaminationSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class ExaminationSessionController {

    private final ExaminationSessionService sessionService;

    @PostMapping("/start")
    public ResponseEntity<ExaminationSessionResponse> start(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(sessionService.startSession(user));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ExaminationSessionResponse> submitAnswers(
            @PathVariable Long id,
            @Valid @RequestBody ExaminationSessionSubmitRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(sessionService.submitAnswers(id, request, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExaminationSessionResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(sessionService.getById(id, user));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<ExaminationSessionResponse>> getByUser(
            @AuthenticationPrincipal User user,
            Pageable pageable
    ) {
        return ResponseEntity.ok(sessionService.getByUser(user, pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<ExaminationSessionResponse> getActive(@AuthenticationPrincipal User user) {
        return sessionService.getActive(user)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
