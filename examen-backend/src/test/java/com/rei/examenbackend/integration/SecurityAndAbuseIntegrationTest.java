package com.rei.examenbackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rei.examenbackend.ExamenApplication;
import com.rei.examenbackend.dto.answer.AnswerRequest;
import com.rei.examenbackend.dto.auth.AuthRequest;
import com.rei.examenbackend.dto.auth.PasswordResetConfirmRequest;
import com.rei.examenbackend.dto.auth.PasswordResetRequest;
import com.rei.examenbackend.dto.auth.RegisterRequest;
import com.rei.examenbackend.dto.session.ExaminationSessionSubmitRequest;
import com.rei.examenbackend.model.Category;
import com.rei.examenbackend.model.Question;
import com.rei.examenbackend.model.QuestionType;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.model.UserNotification;
import com.rei.examenbackend.repository.CategoryRepository;
import com.rei.examenbackend.repository.PasswordResetTokenRepository;
import com.rei.examenbackend.repository.QuestionRepository;
import com.rei.examenbackend.repository.UserNotificationRepository;
import com.rei.examenbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ExamenApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Import(com.rei.examenbackend.config.SharedTestConfig.class)
public class SecurityAndAbuseIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserNotificationRepository userNotificationRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Nested
    @DisplayName("API2: Authentication & Token Security")
    class AuthenticationSecurityTests {

        @Test
        @DisplayName("Unauthenticated requests to protected endpoints return 401")
        void unauthenticatedRequestsAreRejected() throws Exception {
            mockMvc.perform(get("/api/profile/summary")).andExpect(status().isUnauthorized());
            mockMvc.perform(get("/api/profile/analytics")).andExpect(status().isUnauthorized());
            mockMvc.perform(get("/api/sessions/active")).andExpect(status().isUnauthorized());
            mockMvc.perform(get("/api/sessions/me")).andExpect(status().isUnauthorized());
            mockMvc.perform(post("/api/sessions/start")).andExpect(status().isUnauthorized());
            mockMvc.perform(get("/api/todos")).andExpect(status().isUnauthorized());
            mockMvc.perform(get("/api/journal")).andExpect(status().isUnauthorized());
            mockMvc.perform(get("/api/growth/weekly-summary")).andExpect(status().isUnauthorized());
            mockMvc.perform(get("/api/settings")).andExpect(status().isUnauthorized());
            mockMvc.perform(get("/api/notifications")).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Tampered or forged JWT tokens are rejected")
        void tamperedTokenIsRejected() throws Exception {
            mockMvc.perform(get("/api/profile/summary")
                            .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.tampered.token"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/profile/summary")
                            .header("Authorization", "Basic invalidcreds"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("API1: Broken Object Level Authorization (BOLA)")
    class BolaAuthorizationTests {

        @Test
        @DisplayName("User B cannot access or modify User A's Todos")
        void userCannotModifyAnotherUsersTodo() throws Exception {
            String tokenA = registerAndLogin("userA-" + UUID.randomUUID() + "@example.com", "Password123!");
            String tokenB = registerAndLogin("userB-" + UUID.randomUUID() + "@example.com", "Password123!");

            String todoJson = mockMvc.perform(post("/api/todos")
                            .header("Authorization", "Bearer " + tokenA)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(Map.of(
                                    "title", "User A Private Todo",
                                    "dueAt", LocalDateTime.now().plusDays(1).toString()
                            ))))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            long todoId = objectMapper.readTree(todoJson).get("id").asLong();

            // User B attempts to update User A's todo -> 403 Forbidden
            mockMvc.perform(patch("/api/todos/{id}", todoId)
                            .header("Authorization", "Bearer " + tokenB)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(Map.of("title", "Hacked Title"))))
                    .andExpect(status().isForbidden());

            // User B attempts to toggle User A's todo -> 403 Forbidden
            mockMvc.perform(patch("/api/todos/{id}/toggle", todoId)
                            .header("Authorization", "Bearer " + tokenB))
                    .andExpect(status().isForbidden());

            // User B attempts to delete User A's todo -> 403 Forbidden
            mockMvc.perform(delete("/api/todos/{id}", todoId)
                            .header("Authorization", "Bearer " + tokenB))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("User B cannot access or submit answers to User A's Examination Session")
        void userCannotTamperWithAnotherUsersSession() throws Exception {
            String emailA = "examA-" + UUID.randomUUID() + "@example.com";
            String emailB = "examB-" + UUID.randomUUID() + "@example.com";
            String tokenA = registerAndLogin(emailA, "Password123!");
            String tokenB = registerAndLogin(emailB, "Password123!");

            Category category = categoryRepository.save(Category.builder()
                    .name("SecCat-" + UUID.randomUUID())
                    .description("Test")
                    .build());
            Question question = questionRepository.save(Question.builder()
                    .text("Prompt " + UUID.randomUUID())
                    .category(category)
                    .responseType(QuestionType.SCALE_1_5)
                    .custom(false)
                    .build());

            String sessionJson = mockMvc.perform(post("/api/sessions/start")
                            .header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            long sessionId = objectMapper.readTree(sessionJson).get("id").asLong();

            // User B attempts to read User A's session -> 403 Forbidden
            mockMvc.perform(get("/api/sessions/{id}", sessionId)
                            .header("Authorization", "Bearer " + tokenB))
                    .andExpect(status().isForbidden());

            // User B attempts to submit answers to User A's session -> 403 Forbidden
            AnswerRequest answer = new AnswerRequest();
            answer.setQuestionId(question.getId());
            answer.setExaminationSessionId(sessionId);
            answer.setAnswerText("4");
            answer.setReflectionText("Unauthorized submission");
            answer.setFeelingScore(4);

            ExaminationSessionSubmitRequest submitRequest = new ExaminationSessionSubmitRequest();
            submitRequest.setNotes("Tampered");
            submitRequest.setMoodScore(4);
            submitRequest.setAnswers(List.of(answer));

            mockMvc.perform(post("/api/sessions/{id}/submit", sessionId)
                            .header("Authorization", "Bearer " + tokenB)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(submitRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("User B cannot modify or delete User A's custom question")
        void userCannotTamperWithAnotherUsersCustomQuestion() throws Exception {
            String tokenA = registerAndLogin("qOwnerA-" + UUID.randomUUID() + "@example.com", "Password123!");
            String tokenB = registerAndLogin("qAttackerB-" + UUID.randomUUID() + "@example.com", "Password123!");

            Category category = categoryRepository.save(Category.builder()
                    .name("CustomCat-" + UUID.randomUUID())
                    .description("Test")
                    .build());

            String questionJson = mockMvc.perform(post("/api/questions/custom")
                            .header("Authorization", "Bearer " + tokenA)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(Map.of(
                                    "text", "User A Private Question?",
                                    "categoryId", category.getId(),
                                    "orderNumber", 1,
                                    "responseType", "SCALE_1_5"
                            ))))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            long questionId = objectMapper.readTree(questionJson).get("id").asLong();

            // User B attempts to update User A's custom question -> 403 Forbidden
            mockMvc.perform(put("/api/questions/custom/{id}", questionId)
                            .header("Authorization", "Bearer " + tokenB)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(Map.of(
                                    "text", "Tampered question text?",
                                    "categoryId", category.getId(),
                                    "orderNumber", 1,
                                    "responseType", "SCALE_1_5"
                            ))))
                    .andExpect(status().isForbidden());

            // User B attempts to delete User A's custom question -> 403 Forbidden
            mockMvc.perform(delete("/api/questions/custom/{id}", questionId)
                            .header("Authorization", "Bearer " + tokenB))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("User B cannot mark User A's notification as read")
        void userCannotMarkAnotherUsersNotificationRead() throws Exception {
            String emailA = "notifA-" + UUID.randomUUID() + "@example.com";
            String emailB = "notifB-" + UUID.randomUUID() + "@example.com";
            String tokenA = registerAndLogin(emailA, "Password123!");
            String tokenB = registerAndLogin(emailB, "Password123!");

            User userA = userRepository.findByEmail(emailA).orElseThrow();
            UserNotification notification = userNotificationRepository.save(UserNotification.builder()
                    .user(userA)
                    .title("Private Notice")
                    .message("User A only")
                    .notificationType("REMINDER")
                    .createdAt(LocalDateTime.now())
                    .build());

            // User B attempts to mark User A's notification as read -> 404 Not Found
            mockMvc.perform(patch("/api/notifications/{id}/read", notification.getId())
                            .header("Authorization", "Bearer " + tokenB))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("API4: Abuse, Rate Limiting & Cooldown Enforcements")
    class AbuseAndRateLimitingTests {

        @Test
        @DisplayName("Exceeding password reset requests triggers 429 Too Many Requests")
        void passwordResetRateLimitEnforcement() throws Exception {
            String email = "resetRateLimit-" + UUID.randomUUID() + "@example.com";
            registerAndLogin(email, "Password123!");

            PasswordResetRequest resetReq = new PasswordResetRequest();
            resetReq.setEmail(email);

            // Default maxRequests is 3
            for (int i = 0; i < 3; i++) {
                mockMvc.perform(post("/api/auth/reset/request")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJson(resetReq)))
                        .andExpect(status().isAccepted());
            }

            // 4th request triggers rate limit -> 429
            mockMvc.perform(post("/api/auth/reset/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(resetReq)))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(jsonPath("$.message").value("Too many reset requests. Try again later."));
        }

        @Test
        @DisplayName("Submitting answers to an already completed session triggers 409 Conflict")
        void submittingCompletedSessionTriggersConflict() throws Exception {
            String token = registerAndLogin("conflict-" + UUID.randomUUID() + "@example.com", "Password123!");

            Category category = categoryRepository.save(Category.builder()
                    .name("ConfCat-" + UUID.randomUUID())
                    .description("Test")
                    .build());
            Question question = questionRepository.save(Question.builder()
                    .text("Question " + UUID.randomUUID())
                    .category(category)
                    .responseType(QuestionType.SCALE_1_5)
                    .custom(false)
                    .build());

            String sessionJson = mockMvc.perform(post("/api/sessions/start")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            long sessionId = objectMapper.readTree(sessionJson).get("id").asLong();

            AnswerRequest answer = new AnswerRequest();
            answer.setQuestionId(question.getId());
            answer.setExaminationSessionId(sessionId);
            answer.setAnswerText("5");
            answer.setReflectionText("Done");
            answer.setFeelingScore(5);

            ExaminationSessionSubmitRequest submitRequest = new ExaminationSessionSubmitRequest();
            submitRequest.setNotes("First submit");
            submitRequest.setMoodScore(5);
            submitRequest.setAnswers(List.of(answer));

            // First submit succeeds
            mockMvc.perform(post("/api/sessions/{id}/submit", sessionId)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(submitRequest)))
                    .andExpect(status().isOk());

            // Second submit -> 409 Conflict
            mockMvc.perform(post("/api/sessions/{id}/submit", sessionId)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(submitRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Session already completed"));
        }
    }

    @Nested
    @DisplayName("Input Validation & Boundary Testing")
    class InputValidationBoundaryTests {

        @Test
        @DisplayName("Registration with blank or malformed parameters is rejected")
        void registrationValidation() throws Exception {
            RegisterRequest invalidReq = new RegisterRequest();
            invalidReq.setEmail("invalid-email");
            invalidReq.setPassword("short");
            invalidReq.setFullName("");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(invalidReq)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Category creation with empty name is rejected")
        void categoryCreationValidation() throws Exception {
            String token = registerAndLogin("catVal-" + UUID.randomUUID() + "@example.com", "Password123!");

            mockMvc.perform(post("/api/categories")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(Map.of("name", "", "description", "Desc"))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Custom question creation with non-existent category ID returns 404")
        void nonExistentCategoryReturns404() throws Exception {
            String token = registerAndLogin("qCat404-" + UUID.randomUUID() + "@example.com", "Password123!");

            mockMvc.perform(post("/api/questions/custom")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJson(Map.of(
                                    "text", "Valid text?",
                                    "categoryId", 9999999L,
                                    "orderNumber", 1,
                                    "responseType", "SCALE_1_5"
                            ))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Category not found"));
        }
    }

    private String registerAndLogin(String email, String password) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFullName("Security User");
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(registerRequest)))
                .andExpect(status().isOk());

        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        String loginJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(loginJson).get("token").asText();
    }

    private String asJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
