package com.rei.examenbackend.service.impl;

import com.rei.examenbackend.dto.auth.AuthRequest;
import com.rei.examenbackend.dto.auth.AuthResponse;
import com.rei.examenbackend.dto.auth.RegisterRequest;
import com.rei.examenbackend.dto.auth.PasswordResetRequest;
import com.rei.examenbackend.dto.auth.PasswordResetConfirmRequest;
import com.rei.examenbackend.exception.ApiException;
import com.rei.examenbackend.config.PasswordResetProperties;
import com.rei.examenbackend.model.Role;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.model.PasswordResetToken;
import com.rei.examenbackend.repository.UserRepository;
import com.rei.examenbackend.repository.PasswordResetTokenRepository;
import com.rei.examenbackend.service.AuthenticationService;
import com.rei.examenbackend.service.PasswordResetMailService;
import com.rei.examenbackend.config.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final PasswordResetMailService passwordResetMailService;
    private final PasswordResetProperties passwordResetProperties;
    private static final Pattern STRONG_PASSWORD = Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

    public AuthenticationServiceImpl(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authManager,
            PasswordResetMailService passwordResetMailService,
            PasswordResetProperties passwordResetProperties
    ) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authManager = authManager;
        this.passwordResetMailService = passwordResetMailService;
        this.passwordResetProperties = passwordResetProperties;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {

        if (request.getFullName() == null || !request.getFullName().trim().contains(" ")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please provide first and last name");
        }

        if (!STRONG_PASSWORD.matcher(request.getPassword()).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password must be at least 8 chars with upper, lower, number, and symbol");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already in use");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .token(token)
                .build();
    }

    @Override
    public AuthResponse login(AuthRequest request) {

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .token(token)
                .build();
    }

    @Override
    public void requestPasswordReset(PasswordResetRequest request) {
        // Respond identically for unknown addresses so the endpoint cannot be used to discover accounts.
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minusMinutes(passwordResetProperties.getWindowMinutes());
        long recentRequests = passwordResetTokenRepository.countByUserAndCreatedAtAfter(user, windowStart);
        if (recentRequests >= passwordResetProperties.getMaxRequests()) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Too many reset requests. Try again later.");
        }

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .createdAt(now)
                .expiresAt(now.plusMinutes(passwordResetProperties.getTokenTtlMinutes()))
                .used(false)
                .build();
        passwordResetTokenRepository.save(resetToken);
        passwordResetMailService.sendResetEmail(user, token);
    }

    @Override
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        if (!STRONG_PASSWORD.matcher(request.getNewPassword()).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password must be at least 8 chars with upper, lower, number, and symbol");
        }

        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reset token not found"));

        if (token.isUsed() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reset token is expired or already used");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsed(true);
        passwordResetTokenRepository.save(token);
    }
}
