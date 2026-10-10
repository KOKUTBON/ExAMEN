package com.rei.examenbackend.service;

import com.rei.examenbackend.dto.auth.AuthRequest;
import com.rei.examenbackend.dto.auth.AuthResponse;
import com.rei.examenbackend.dto.auth.RegisterRequest;
import com.rei.examenbackend.dto.auth.PasswordResetRequest;
import com.rei.examenbackend.dto.auth.PasswordResetConfirmRequest;

public interface AuthenticationService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(AuthRequest request);

    void requestPasswordReset(PasswordResetRequest request);

    void confirmPasswordReset(PasswordResetConfirmRequest request);
}
