package com.rei.examenbackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rei.examenbackend.ExamenApplication;
import com.rei.examenbackend.dto.auth.AuthRequest;
import com.rei.examenbackend.dto.auth.RegisterRequest;
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
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ExamenApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Import(ProfileControllerIntegrationTest.MailTestConfig.class)
class ProfileControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Configuration
    static class MailTestConfig {
        @Bean
        JavaMailSender javaMailSender() {
            return new JavaMailSenderImpl();
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper().findAndRegisterModules();
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void summaryReturnsCompletedTodoCount() throws Exception {
        String token = registerAndLogin();
        long firstTodoId = createTodo(token, "Prepare evening reflection");
        createTodo(token, "Review daily notes");

        mockMvc.perform(patch("/api/todos/{id}/toggle", firstTodoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/profile/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todosCompleted").value(1));
    }

    @Test
    void progressClampsDaysParameterToSafeRange() throws Exception {
        String token = registerAndLogin();

        mockMvc.perform(get("/api/profile/progress")
                        .param("days", "0")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points.length()").value(1));

        mockMvc.perform(get("/api/profile/progress")
                        .param("days", "999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points.length()").value(365));
    }

    @Test
    void analyticsDoesNotInventScoresWithoutData() throws Exception {
        String token = registerAndLogin();

        mockMvc.perform(get("/api/profile/analytics")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.advancedScore").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.anomalies").isArray())
                .andExpect(jsonPath("$.qualityMetrics").exists())
                .andExpect(jsonPath("$.qualityMetrics.feedbackSatisfactionRate").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.qualityMetrics.totalFeedbackCount").value(0))
                .andExpect(jsonPath("$.qualityMetrics.sentimentDriftIndicator").value("STABLE"));
    }

    private long createTodo(String token, String title) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "title", title,
                "dueAt", LocalDateTime.now().plusDays(1)
        ));
        String response = mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private String registerAndLogin() throws Exception {
        String email = "profile-" + UUID.randomUUID() + "@example.com";
        String password = "Password123!";

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFullName("Profile User");
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        String loginJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(loginJson).get("token").asText();
    }
}
