package com.rei.examenbackend.controller;

import com.rei.examenbackend.dto.journal.JournalEntryRequest;
import com.rei.examenbackend.dto.journal.JournalEntryResponse;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.service.JournalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/journal")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;

    @PostMapping
    public ResponseEntity<JournalEntryResponse> create(
            @Valid @RequestBody JournalEntryRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(journalService.create(request, user));
    }

    @GetMapping
    public ResponseEntity<List<JournalEntryResponse>> recent(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(journalService.getRecent(user));
    }
}
