package com.rei.examenbackend.controller;

import com.rei.examenbackend.dto.auth.AuthRequest;
import com.rei.examenbackend.dto.auth.AuthResponse;
import com.rei.examenbackend.dto.auth.RegisterRequest;
import com.rei.examenbackend.dto.auth.PasswordResetRequest;
import com.rei.examenbackend.dto.auth.PasswordResetConfirmRequest;
import com.rei.examenbackend.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/reset/request")
    public ResponseEntity<Void> requestReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset/confirm")
    public ResponseEntity<Void> confirmReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request);
        return ResponseEntity.noContent().build();
    }
}
