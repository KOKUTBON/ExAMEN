package com.rei.examenbackend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rei.examenbackend.ExamenApplication;
import com.rei.examenbackend.dto.auth.AuthRequest;
import com.rei.examenbackend.dto.auth.PasswordResetConfirmRequest;
import com.rei.examenbackend.dto.auth.PasswordResetRequest;
import com.rei.examenbackend.dto.auth.RegisterRequest;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.model.UserNotification;
import com.rei.examenbackend.repository.PasswordResetTokenRepository;
import com.rei.examenbackend.repository.UserNotificationRepository;
import com.rei.examenbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ExamenApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Import(com.rei.examenbackend.config.SharedTestConfig.class)
class FullstackUserJourneyIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

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

    @Test
    void userCanUseCoreFeaturesEndToEnd() throws Exception {
        String email = "journey-" + UUID.randomUUID() + "@example.com";
        String token = registerAndLogin(email, "Password123!");
        User user = userRepository.findByEmail(email).orElseThrow();

        String categoryName = "Category-" + UUID.randomUUID();
        String categoryJson = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(Map.of("name", categoryName, "description", "Personal reflections"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(categoryName))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long categoryId = objectMapper.readTree(categoryJson).get("id").asLong();

        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").isNotEmpty());

        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(Map.of("name", categoryName + "-Updated", "description", "Updated"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(categoryName + "-Updated"));

        String removableCategoryJson = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(Map.of("name", "Temp-" + UUID.randomUUID(), "description", "Delete me"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long removableCategoryId = objectMapper.readTree(removableCategoryJson).get("id").asLong();

        mockMvc.perform(delete("/api/categories/{id}", removableCategoryId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        String customQuestionJson = mockMvc.perform(post("/api/questions/custom")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(Map.of(
                                "text", "Where did I show patience today?",
                                "categoryId", categoryId,
                                "orderNumber", 1,
                                "responseType", "SCALE_1_5"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custom").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long customQuestionId = objectMapper.readTree(customQuestionJson).get("id").asLong();

        String mineJson = mockMvc.perform(get("/api/questions/my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(readIdsArray(mineJson)).contains(customQuestionId);

        mockMvc.perform(put("/api/questions/custom/{id}", customQuestionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(Map.of(
                                "text", "Where did I show patience today? (updated)",
                                "categoryId", categoryId,
                                "orderNumber", 2,
                                "responseType", "SCALE_1_5"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Where did I show patience today? (updated)"));

        String sessionJson = mockMvc.perform(post("/api/sessions/start")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long sessionId = objectMapper.readTree(sessionJson).get("id").asLong();

        Map<String, Object> answerPayload = Map.of(
                "answerText", "4",
                "correct", false,
                "reflectionText", "I stayed calm during a conflict.",
                "feelingScore", 4,
                "questionId", customQuestionId,
                "examinationSessionId", sessionId
        );

        mockMvc.perform(post("/api/sessions/{id}/submit", sessionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(Map.of(
                                "answers", List.of(answerPayload),
                                "notes", "Solid day overall.",
                                "moodScore", 4
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId))
                .andExpect(jsonPath("$.score").isNumber())
                .andExpect(jsonPath("$.answers.length()").value(1));

        mockMvc.perform(get("/api/sessions/{id}", sessionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId));

        mockMvc.perform(get("/api/sessions/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").isNotEmpty());

        mockMvc.perform(get("/api/sessions/active")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Answers of a completed session are read-only.
        mockMvc.perform(post("/api/answers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(answerPayload)))
                .andExpect(status().isConflict());

        String sessionAnswersJson = mockMvc.perform(get("/api/answers/session/{sessionId}", sessionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long answerId = objectMapper.readTree(sessionAnswersJson).get(0).get("id").asLong();

        mockMvc.perform(get("/api/answers/{id}", answerId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").value(customQuestionId));

        mockMvc.perform(put("/api/answers/{id}", answerId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(answerPayload)))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/answers/{id}", answerId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/profile/analytics")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.advancedScore.compositeScore").isNumber())
                .andExpect(jsonPath("$.advancedScore.confidenceLevel").value(0.95))
                .andExpect(jsonPath("$.advancedScore.sampleSize").value(1));

        mockMvc.perform(get("/api/profile/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/profile/progress")
                        .param("days", "14")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points.length()").value(14));
        mockMvc.perform(get("/api/profile/summary/weekly")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/profile/summary/monthly")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/profile/analytics")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        String todoJson = mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(Map.of(
                                "title", "Review examen notes",
                                "dueAt", LocalDateTime.now().plusDays(1).toString()
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long todoId = objectMapper.readTree(todoJson).get("id").asLong();

        mockMvc.perform(patch("/api/todos/{id}", todoId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(Map.of(
                                "title", "Review examen notes (updated)",
                                "dueAt", LocalDateTime.now().plusDays(2).toString()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Review examen notes (updated)"));

        mockMvc.perform(patch("/api/todos/{id}/toggle", todoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        mockMvc.perform(get("/api/todos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").isNotEmpty());

        mockMvc.perform(post("/api/journal")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(Map.of("content", "Journal entry for integration test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Journal entry for integration test"));

        mockMvc.perform(get("/api/journal")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").isNotEmpty());

        mockMvc.perform(get("/api/settings")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/settings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(Map.of(
                                "timeZone", "UTC",
                                "reminderTime", "09:30",
                                "theme", "system",
                                "emailReminder", true,
                                "inAppReminder", true
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeZone").value("UTC"))
                .andExpect(jsonPath("$.emailReminder").value(true))
                .andExpect(jsonPath("$.inAppReminder").value(true));

        mockMvc.perform(post("/api/growth/gratitude")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(Map.of("content", "Grateful for this day"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Grateful for this day"));

        mockMvc.perform(get("/api/growth/gratitude")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").isNotEmpty());

        mockMvc.perform(post("/api/growth/habits")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(Map.of("habit", "Prayer", "score", 4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.habit").value("Prayer"));

        mockMvc.perform(get("/api/growth/habits")
                        .param("days", "30")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").isNotEmpty());

        mockMvc.perform(get("/api/growth/weekly-summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/growth/meditation-suggestions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));

        mockMvc.perform(get("/api/growth/export/pdf")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        mockMvc.perform(get("/api/insights/summary")
                        .param("days", "30")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").isNotEmpty());

        mockMvc.perform(post("/api/insights/questions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(Map.of("focus", "gratitude", "count", 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions.length()").value(3));

        mockMvc.perform(post("/api/insights/session")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(Map.of("sessionId", sessionId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId));

        UserNotification notification = UserNotification.builder()
                .user(user)
                .title("Reminder")
                .message("Time for your examen")
                .notificationType("REMINDER")
                .createdAt(LocalDateTime.now())
                .build();
        notification = userNotificationRepository.save(notification);

        mockMvc.perform(get("/api/notifications")
                        .param("unreadOnly", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").isNotEmpty());

        mockMvc.perform(patch("/api/notifications/{id}/read", notification.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        String unreadAfterRead = mockMvc.perform(get("/api/notifications")
                        .param("unreadOnly", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(readIdsArray(unreadAfterRead)).doesNotContain(notification.getId());

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        mockMvc.perform(delete("/api/todos/{id}", todoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void passwordResetFlowHonorsStrengthPolicy() throws Exception {
        String email = "reset-" + UUID.randomUUID() + "@example.com";
        String initialPassword = "Password123!";
        registerAndLogin(email, initialPassword);

        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail(email);
        mockMvc.perform(post("/api/auth/reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(request)))
                .andExpect(status().isAccepted());

        String token = passwordResetTokenRepository.findAll().stream()
                .filter(t -> t.getUser() != null && email.equals(t.getUser().getEmail()))
                .max(java.util.Comparator.comparing(t -> t.getCreatedAt()))
                .orElseThrow()
                .getToken();

        PasswordResetConfirmRequest weakReset = new PasswordResetConfirmRequest();
        weakReset.setToken(token);
        weakReset.setNewPassword("weak");

        mockMvc.perform(post("/api/auth/reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(weakReset)))
                .andExpect(status().isBadRequest());

        PasswordResetConfirmRequest strongReset = new PasswordResetConfirmRequest();
        strongReset.setToken(token);
        strongReset.setNewPassword("NewStrongPass1!");

        mockMvc.perform(post("/api/auth/reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(strongReset)))
                .andExpect(status().isNoContent());

        AuthRequest loginWithNewPassword = new AuthRequest();
        loginWithNewPassword.setEmail(email);
        loginWithNewPassword.setPassword("NewStrongPass1!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(loginWithNewPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    private String registerAndLogin(String email, String password) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFullName("Journey User");
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

    private List<Long> readIdsArray(String json) throws Exception {
        JsonNode node = objectMapper.readTree(json);
        if (!node.isArray()) {
            return List.of();
        }
        java.util.ArrayList<Long> ids = new java.util.ArrayList<>();
        for (JsonNode item : node) {
            if (item.hasNonNull("id")) {
                ids.add(item.get("id").asLong());
            }
        }
        return ids;
    }
}
