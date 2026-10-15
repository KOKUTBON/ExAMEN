package com.rei.examenbackend.controller;

import com.rei.examenbackend.dto.user.DeleteAccountRequest;
import com.rei.examenbackend.dto.user.UserResponse;
import com.rei.examenbackend.exception.ApiException;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.repository.UserRepository;
import com.rei.examenbackend.service.AccountDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final AccountDataService accountDataService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        return ResponseEntity.ok(
                UserResponse.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .build()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        User persisted = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        return ResponseEntity.ok(
                UserResponse.builder()
                        .id(persisted.getId())
                        .fullName(persisted.getFullName())
                        .email(persisted.getEmail())
                        .role(persisted.getRole().name())
                        .build()
        );
    }

    /** Downloads everything stored for the current account as JSON. */
    @GetMapping("/me/export")
    public ResponseEntity<Map<String, Object>> export(@AuthenticationPrincipal User user) {
        String filename = "examen-export-" + java.time.LocalDate.now() + ".json";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(accountDataService.export(user));
    }

    /** Permanently deletes the current account and all of its data. Requires the account password. */
    @PostMapping("/me/delete")
    public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal User user,
                                         @Valid @RequestBody DeleteAccountRequest request) {
        accountDataService.deleteAccount(user, request.getPassword());
        return ResponseEntity.noContent().build();
    }
}
