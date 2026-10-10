package com.rei.examenbackend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rei.examenbackend.ExamenApplication;
import com.rei.examenbackend.model.Category;
import com.rei.examenbackend.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ExamenApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Import(com.rei.examenbackend.config.SharedTestConfig.class)
class OwnershipAndPrivacyIntegrationTest {

    private static final String PASSWORD = "Password123!";

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void customQuestionsAndCategoriesArePrivateToTheirOwner() throws Exception {
        String owner = registerAndLogin();
        String other = registerAndLogin();

        long categoryId = createCategory(owner, "Private " + UUID.randomUUID());
        long questionId = createQuestion(owner, categoryId, "Private prompt " + UUID.randomUUID());

        JsonNode otherQuestions = getJson(other, "/api/questions?size=500").get("content");
        for (JsonNode q : otherQuestions) {
            assertThat(q.get("id").asLong()).isNotEqualTo(questionId);
            assertThat(q.has("answers")).as("question payload must not carry answers").isFalse();
        }
        JsonNode otherCategories = getJson(other, "/api/categories?size=500").get("content");
        for (JsonNode c : otherCategories) {
            assertThat(c.get("id").asLong()).isNotEqualTo(categoryId);
        }

        mockMvc.perform(get("/api/questions/{id}", questionId).header("Authorization", bearer(other)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/questions/category/{id}", categoryId).header("Authorization", bearer(other)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .header("Authorization", bearer(other))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Hijacked"))))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/categories/{id}", categoryId).header("Authorization", bearer(other)))
                .andExpect(status().isNotFound());
    }

    @Test
    void defaultCategoriesCannotBeChangedByRegularUsers() throws Exception {
        String token = registerAndLogin();
        Category defaults = categoryRepository.findByNameIgnoreCaseAndOwnerIsNull("Reflection").orElseThrow();

        mockMvc.perform(put("/api/categories/{id}", defaults.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Renamed"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/categories/{id}", defaults.getId()).header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void categoryWithQuestionsCannotBeDeletedAndArchivedQuestionsDisappear() throws Exception {
        String token = registerAndLogin();
        long categoryId = createCategory(token, "Busy " + UUID.randomUUID());
        long questionId = createQuestion(token, categoryId, "Prompt " + UUID.randomUUID());

        mockMvc.perform(delete("/api/categories/{id}", categoryId).header("Authorization", bearer(token)))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/questions/custom/{id}", questionId).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/questions/my").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void answersOfAnotherUsersSessionAreNotReadable() throws Exception {
        String owner = registerAndLogin();
        String other = registerAndLogin();
        long sessionId = getJson(owner, "/api/sessions/start", true).get("id").asLong();

        mockMvc.perform(get("/api/answers/session/{id}", sessionId).header("Authorization", bearer(other)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/sessions/{id}", sessionId).header("Authorization", bearer(other)))
                .andExpect(status().isForbidden());
    }

    @Test
    void passwordResetDoesNotRevealWhetherAnAccountExists() throws Exception {
        mockMvc.perform(post("/api/auth/reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "nobody-" + UUID.randomUUID() + "@example.com"))))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void userCanExportAndThenDeleteTheirAccount() throws Exception {
        String email = "export-" + UUID.randomUUID() + "@example.com";
        String token = registerAndLogin(email);

        mockMvc.perform(post("/api/journal")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "A quiet evening."))))
                .andExpect(status().isOk());
        createQuestion(token, createCategory(token, "Mine " + UUID.randomUUID()), "Owned prompt");

        mockMvc.perform(get("/api/users/me/export").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(jsonPath("$.formatVersion").value(1))
                .andExpect(jsonPath("$.account.email").value(email))
                .andExpect(jsonPath("$.account.password").doesNotExist())
                .andExpect(jsonPath("$.journalEntries[0].content").value("A quiet evening."))
                .andExpect(jsonPath("$.customQuestions.length()").value(1));

        mockMvc.perform(post("/api/users/me/delete")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("password", "wrong-password"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/users/me/delete")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("password", PASSWORD))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", PASSWORD))))
                .andExpect(status().is4xxClientError());
    }

    private long createCategory(String token, String name) throws Exception {
        String body = mockMvc.perform(post("/api/categories")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", name))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custom").value(true))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private long createQuestion(String token, long categoryId, String text) throws Exception {
        String body = mockMvc.perform(post("/api/questions/custom")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("text", text, "categoryId", categoryId, "responseType", "SCALE_1_5"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private JsonNode getJson(String token, String path) throws Exception {
        return getJson(token, path, false);
    }

    private JsonNode getJson(String token, String path, boolean usePost) throws Exception {
        var request = usePost ? post(path) : get(path);
        String body = mockMvc.perform(request.header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private String registerAndLogin() throws Exception {
        return registerAndLogin("owner-" + UUID.randomUUID() + "@example.com");
    }

    private String registerAndLogin(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "Test User", "email", email, "password", PASSWORD))))
                .andExpect(status().isOk());
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
