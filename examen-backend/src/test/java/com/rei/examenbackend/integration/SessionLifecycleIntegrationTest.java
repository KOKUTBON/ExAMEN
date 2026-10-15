package com.rei.examenbackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rei.examenbackend.ExamenApplication;
import com.rei.examenbackend.dto.answer.AnswerRequest;
import com.rei.examenbackend.dto.auth.AuthRequest;
import com.rei.examenbackend.dto.auth.RegisterRequest;
import com.rei.examenbackend.dto.session.ExaminationSessionSubmitRequest;
import com.rei.examenbackend.model.Category;
import com.rei.examenbackend.model.Question;
import com.rei.examenbackend.model.QuestionType;
import com.rei.examenbackend.repository.CategoryRepository;
import com.rei.examenbackend.repository.QuestionRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ExamenApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Import(com.rei.examenbackend.config.SharedTestConfig.class)
class SessionLifecycleIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private QuestionRepository questionRepository;

    private String token;
    private Question questionOne;
    private Question questionTwo;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        token = registerAndLogin("test@example.com", "Password123!");

        Category category = Category.builder()
                .name("Reflection")
                .description("Core examen prompts")
                .build();
        categoryRepository.save(category);

        questionOne = Question.builder()
                .text("Where did I notice gratitude most clearly today?")
                .category(category)
                .responseType(QuestionType.SCALE_1_5)
                .custom(false)
                .build();
        questionTwo = Question.builder()
                .text("When did I act out of love versus fear?")
                .category(category)
                .responseType(QuestionType.SCALE_1_5)
                .custom(false)
                .build();

        questionRepository.saveAll(List.of(questionOne, questionTwo));
    }

    @Test
    void submitSessionCalculatesScoresAndLocksCooldown() throws Exception {
        String sessionJson = mockMvc.perform(post("/api/sessions/start")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long sessionId = objectMapper.readTree(sessionJson).get("id").asLong();

        ExaminationSessionSubmitRequest submitRequest = new ExaminationSessionSubmitRequest();
        submitRequest.setNotes("Felt more focused today.");

        AnswerRequest answerOne = new AnswerRequest();
        answerOne.setQuestionId(questionOne.getId());
        answerOne.setExaminationSessionId(sessionId);
        answerOne.setAnswerText("Felt grateful during prayer.");
        answerOne.setReflectionText("Felt grateful during prayer.");
        answerOne.setFeelingScore(5);

        AnswerRequest answerTwo = new AnswerRequest();
        answerTwo.setQuestionId(questionTwo.getId());
        answerTwo.setExaminationSessionId(sessionId);
        answerTwo.setAnswerText("Noticed impatience after work.");
        answerTwo.setReflectionText("Noticed impatience after work.");
        answerTwo.setFeelingScore(3);

        submitRequest.setAnswers(List.of(answerOne, answerTwo));

        mockMvc.perform(post("/api/sessions/{id}/submit", sessionId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedAt").isNotEmpty())
                .andExpect(jsonPath("$.moodScore").value(4))
                .andExpect(jsonPath("$.score").value(Matchers.closeTo(75.0, 0.1)))
                .andExpect(jsonPath("$.answers.length()").value(2));

        mockMvc.perform(post("/api/sessions/start")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isTooManyRequests());

        mockMvc.perform(get("/api/sessions/active")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    private String registerAndLogin(String email, String password) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFullName("Test User");
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
