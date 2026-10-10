package com.rei.examenbackend.service;

import com.rei.examenbackend.config.JwtService;
import com.rei.examenbackend.config.PasswordResetProperties;
import com.rei.examenbackend.dto.auth.AuthRequest;
import com.rei.examenbackend.dto.auth.PasswordResetConfirmRequest;
import com.rei.examenbackend.dto.auth.RegisterRequest;
import com.rei.examenbackend.exception.ApiException;
import com.rei.examenbackend.model.PasswordResetToken;
import com.rei.examenbackend.model.Role;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.repository.UserRepository;
import com.rei.examenbackend.repository.PasswordResetTokenRepository;
import com.rei.examenbackend.service.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AuthenticationServiceImplTest {

    private UserRepository userRepository;
    private PasswordResetTokenRepository resetTokenRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthenticationManager authenticationManager;
    private PasswordResetMailService passwordResetMailService;
    private PasswordResetProperties passwordResetProperties;
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        resetTokenRepository = mock(PasswordResetTokenRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        authenticationManager = mock(AuthenticationManager.class);
        passwordResetMailService = mock(PasswordResetMailService.class);
        passwordResetProperties = new PasswordResetProperties();
        authenticationService = new AuthenticationServiceImpl(
                userRepository,
                resetTokenRepository,
                passwordEncoder,
                jwtService,
                authenticationManager,
                passwordResetMailService,
                passwordResetProperties
        );
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setFullName("Test User");
        request.setPassword("StrongPass1!");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void loginReturnsTokenAndProfile() {
        AuthRequest request = new AuthRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = User.builder()
                .id(5L)
                .fullName("Test User")
                .email("test@example.com")
                .password("hashed")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("test@example.com")).thenReturn("token");

        var response = authenticationService.login(request);

        assertThat(response.getUserId()).isEqualTo(5L);
        assertThat(response.getToken()).isEqualTo("token");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void confirmPasswordResetRejectsWeakPassword() {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
        request.setToken("token");
        request.setNewPassword("weak");

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("old-hash")
                .role(Role.USER)
                .build();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .id(1L)
                .token("token")
                .user(user)
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();
        when(resetTokenRepository.findByToken("token")).thenReturn(Optional.of(resetToken));

        assertThatThrownBy(() -> authenticationService.confirmPasswordReset(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Password must be at least 8 chars")
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(userRepository, never()).save(any());
        verify(resetTokenRepository, never()).save(any());
    }
}
