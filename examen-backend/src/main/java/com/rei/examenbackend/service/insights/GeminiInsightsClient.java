package com.rei.examenbackend.service.insights;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rei.examenbackend.config.GeminiProperties;
import com.rei.examenbackend.dto.insights.EvidenceSnippetResponse;
import com.rei.examenbackend.dto.insights.FollowUpPromptResponse;
import com.rei.examenbackend.dto.insights.InsightsSummaryResponse;
import com.rei.examenbackend.dto.insights.QuestionSuggestionResponse;
import com.rei.examenbackend.dto.insights.SemanticTagResponse;
import com.rei.examenbackend.dto.insights.SessionInsightResponse;
import com.rei.examenbackend.dto.insights.WeeklyNarrativeResponse;
import com.rei.examenbackend.model.Answer;
import com.rei.examenbackend.model.GratitudeEntry;
import com.rei.examenbackend.model.InsightFeedback;
import com.rei.examenbackend.model.SemanticTag;
import com.rei.examenbackend.model.SemanticTagCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Primary
@Service
public class GeminiInsightsClient implements InsightsClient {

    private static final Logger logger = LoggerFactory.getLogger(GeminiInsightsClient.class);

    private final GeminiProperties properties;
    private final StubInsightsClient fallbackClient;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Autowired
    public GeminiInsightsClient(GeminiProperties properties, StubInsightsClient fallbackClient) {
        this(properties, fallbackClient, new ObjectMapper().findAndRegisterModules());
    }

    GeminiInsightsClient(
            GeminiProperties properties,
            StubInsightsClient fallbackClient,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.fallbackClient = fallbackClient;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        int timeoutMs = Math.max(1000, properties.getTimeoutSeconds() * 1000);
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public InsightsSummaryResponse buildSummary(InsightsSummaryContext context) {
        if (!isConfigured()) {
            return fallbackClient.buildSummary(context);
        }

        try {
            String avgStr = context.averageFeeling() == null
                    ? "none recorded"
                    : String.format(Locale.US, "%.1f/5", context.averageFeeling());

            StringBuilder sessionsBlock = new StringBuilder();
            if (context.recentSessions() != null && !context.recentSessions().isEmpty()) {
                for (InsightsSummaryContext.SessionContextSnippet s : context.recentSessions()) {
                    sessionsBlock.append(String.format(Locale.US, "- Date: %s | Mood: %s | Note: %s",
                            s.date(),
                            s.moodScore() != null ? s.moodScore() + "/5" : "n/a",
                            StringUtils.hasText(s.notes()) ? s.notes().trim() : "none"));
                    if (s.reflectionSnippets() != null && !s.reflectionSnippets().isEmpty()) {
                        sessionsBlock.append(" | Reflection: \"").append(s.reflectionSnippets().get(0).trim()).append("\"");
                    }
                    sessionsBlock.append("\n");
                }
            } else {
                sessionsBlock.append("No detailed session notes recorded.\n");
            }

            StringBuilder gratitudeBlock = new StringBuilder();
            if (context.gratitudeSnippets() != null && !context.gratitudeSnippets().isEmpty()) {
                for (String g : context.gratitudeSnippets()) {
                    gratitudeBlock.append("- \"").append(g.trim()).append("\"\n");
                }
            } else {
                gratitudeBlock.append("No gratitude entries recorded.\n");
            }

            String prompt = String.format(
                    "You are an empathetic spiritual and reflective guide for the Examen examination of conscience app.\n" +
                    "Analyze the user's reflection period with concrete grounding:\n" +
                    "- Period: %d days\n" +
                    "- Completed sessions: %d\n" +
                    "- Average feeling score: %s\n\n" +
                    "Recent Sessions Context:\n%s\n" +
                    "Recent Gratitude Context:\n%s\n" +
                    "Generate a JSON response with this exact structure:\n" +
                    "{\n" +
                    "  \"summary\": \"2-3 sentences summarizing progress, spiritual consistency, and encouragement\",\n" +
                    "  \"highlights\": [\n" +
                    "    \"3-4 bullet points highlighting themes, observed virtues, or growth areas\"\n" +
                    "  ],\n" +
                    "  \"confidenceScore\": 0.85,\n" +
                    "  \"evidenceSnippets\": [\n" +
                    "    {\n" +
                    "      \"source\": \"Session Note / Reflection Prompt / Gratitude Journal / Activity Log\",\n" +
                    "      \"date\": \"YYYY-MM-DD\",\n" +
                    "      \"snippet\": \"Concise quote or concrete observation under 140 characters\",\n" +
                    "      \"rationale\": \"Why this evidence grounds the insight\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}\n" +
                    "Respond with ONLY the raw JSON object.",
                    context.periodDays(),
                    context.sessionsCompleted(),
                    avgStr,
                    sessionsBlock.toString().trim(),
                    gratitudeBlock.toString().trim()
            );

            String rawJson = executeGeminiPrompt(prompt);
            JsonNode root = parseJson(rawJson);

            String summary = root.path("summary").asText("");
            if (!StringUtils.hasText(summary)) {
                return fallbackClient.buildSummary(context);
            }

            List<String> highlights = new ArrayList<>();
            JsonNode highlightsNode = root.path("highlights");
            if (highlightsNode.isArray()) {
                for (JsonNode item : highlightsNode) {
                    if (StringUtils.hasText(item.asText())) {
                        highlights.add(item.asText().trim());
                    }
                }
            }
            if (highlights.isEmpty()) {
                highlights.add(String.format(Locale.US, "Sessions completed: %d in %d days", context.sessionsCompleted(), context.periodDays()));
            }

            double confidenceScore = root.path("confidenceScore").asDouble(0.0);
            if (confidenceScore <= 0.0 || confidenceScore > 1.0) {
                InsightsSummaryResponse fallback = fallbackClient.buildSummary(context);
                confidenceScore = fallback.getConfidenceScore();
            } else {
                confidenceScore = Math.round(confidenceScore * 100.0) / 100.0;
            }

            List<EvidenceSnippetResponse> evidenceList = new ArrayList<>();
            JsonNode evidenceNode = root.path("evidenceSnippets");
            if (evidenceNode.isArray()) {
                for (JsonNode item : evidenceNode) {
                    String snippet = item.path("snippet").asText("");
                    if (StringUtils.hasText(snippet)) {
                        String source = item.path("source").asText("Session Evidence");
                        String dateStr = item.path("date").asText("");
                        LocalDate date = context.endDate();
                        if (StringUtils.hasText(dateStr)) {
                            try {
                                date = LocalDate.parse(dateStr.trim());
                            } catch (Exception ignored) {}
                        }
                        String rationale = item.path("rationale").asText("Corroborating observation");
                        evidenceList.add(new EvidenceSnippetResponse(source, date, snippet.trim(), rationale.trim()));
                    }
                }
            }

            if (evidenceList.isEmpty()) {
                InsightsSummaryResponse fallback = fallbackClient.buildSummary(context);
                evidenceList = fallback.getEvidenceSnippets();
            }

            return new InsightsSummaryResponse(
                    summary,
                    highlights,
                    context.periodDays(),
                    context.sessionsCompleted(),
                    context.averageFeeling(),
                    confidenceScore,
                    evidenceList,
                    LocalDateTime.now()
            );
        } catch (Exception ex) {
            logger.warn("Gemini buildSummary failed [{}], using fallback: {}", ex.getClass().getSimpleName(), ex.getMessage());
            return fallbackClient.buildSummary(context);
        }
    }

    @Override
    public SessionInsightResponse buildSessionInsight(SessionInsightContext context) {
        if (!isConfigured()) {
            return fallbackClient.buildSessionInsight(context);
        }

        try {
            String avgStr = context.averageFeeling() == null
                    ? "none recorded"
                    : String.format(Locale.US, "%.1f/5", context.averageFeeling());
            String notes = context.session().getNotes() == null ? "" : context.session().getNotes();
            String strongest = String.join(", ", context.strongestCategories());
            String needsAttention = String.join(", ", context.needsAttentionCategories());

            String prompt = String.format(
                    "You are an empathetic spiritual and reflective guide for the Examen examination of conscience app.\n" +
                    "Analyze this completed examination session:\n" +
                    "- Average feeling rating: %s\n" +
                    "- Strongest areas: [%s]\n" +
                    "- Areas needing attention: [%s]\n" +
                    "- User notes: \"%s\"\n\n" +
                    "Generate a JSON response with this exact structure:\n" +
                    "{\n" +
                    "  \"summary\": \"1-2 sentence thoughtful reflection on today's session\",\n" +
                    "  \"insights\": [\n" +
                    "    \"2-3 psychological or spiritual observations\"\n" +
                    "  ],\n" +
                    "  \"nextSteps\": [\n" +
                    "    \"2 gentle and concrete next steps for tomorrow\"\n" +
                    "  ]\n" +
                    "}\n" +
                    "Respond with ONLY the raw JSON object.",
                    avgStr,
                    strongest,
                    needsAttention,
                    notes
            );

            String rawJson = executeGeminiPrompt(prompt);
            JsonNode root = parseJson(rawJson);

            String summary = root.path("summary").asText("");
            if (!StringUtils.hasText(summary)) {
                return fallbackClient.buildSessionInsight(context);
            }

            List<String> insights = readStringList(root.path("insights"));
            List<String> nextSteps = readStringList(root.path("nextSteps"));

            return new SessionInsightResponse(
                    context.session().getId(),
                    summary,
                    insights,
                    nextSteps,
                    context.averageFeeling(),
                    LocalDateTime.now()
            );
        } catch (Exception ex) {
            logger.warn("Gemini buildSessionInsight failed [{}], using fallback: {}", ex.getClass().getSimpleName(), ex.getMessage());
            return fallbackClient.buildSessionInsight(context);
        }
    }

    @Override
    public QuestionSuggestionResponse buildQuestionSuggestions(QuestionSuggestionContext context) {
        if (!isConfigured()) {
            return fallbackClient.buildQuestionSuggestions(context);
        }

        try {
            String focus = StringUtils.hasText(context.focus()) ? context.focus().trim() : "general reflection";
            String prompt = String.format(
                    "You are an expert in daily examination of conscience and spiritual practice.\n" +
                    "Generate %d unique, thoughtful reflection prompts for the focus area: \"%s\".\n" +
                    "Avoid duplicating these existing prompts: [%s]\n\n" +
                    "Generate a JSON response with this exact structure:\n" +
                    "{\n" +
                    "  \"focus\": \"%s\",\n" +
                    "  \"suggestions\": [\n" +
                    "    \"Prompt 1\",\n" +
                    "    \"Prompt 2\"\n" +
                    "  ]\n" +
                    "}\n" +
                    "Respond with ONLY the raw JSON object.",
                    context.count(),
                    focus,
                    String.join("; ", context.existingQuestions()),
                    focus
            );

            String rawJson = executeGeminiPrompt(prompt);
            JsonNode root = parseJson(rawJson);

            List<String> suggestions = readStringList(root.path("suggestions"));
            if (suggestions.isEmpty()) {
                return fallbackClient.buildQuestionSuggestions(context);
            }

            String resolvedFocus = root.path("focus").asText(focus);
            List<FollowUpPromptResponse> followUps = buildFollowUpPrompts(context);
            return new QuestionSuggestionResponse(
                    resolvedFocus,
                    suggestions,
                    followUps,
                    LocalDateTime.now()
            );
        } catch (Exception ex) {
            logger.warn("Gemini buildQuestionSuggestions failed [{}], using fallback: {}", ex.getClass().getSimpleName(), ex.getMessage());
            return fallbackClient.buildQuestionSuggestions(context);
        }
    }

    @Override
    public List<FollowUpPromptResponse> buildFollowUpPrompts(QuestionSuggestionContext context) {
        if (!isConfigured()) {
            return fallbackClient.buildFollowUpPrompts(context);
        }

        try {
            String focus = StringUtils.hasText(context.focus()) ? context.focus().trim() : "reflection deepening";
            String reflectionsStr = context.recentAnswers() != null && !context.recentAnswers().isEmpty()
                    ? String.join("\n- ", context.recentAnswers())
                    : "No recent reflection quotes recorded yet.";
            String goalsStr = context.activeGoals() != null && !context.activeGoals().isEmpty()
                    ? String.join(", ", context.activeGoals())
                    : "None";
            String strugglesStr = context.recentStruggles() != null && !context.recentStruggles().isEmpty()
                    ? String.join(", ", context.recentStruggles())
                    : "None";

            String prompt = String.format(
                    "You are an expert spiritual director and contemplative guide.\n" +
                    "Generate %d personalized follow-up reflection questions based on the user's recent behavior, reflections, and active goals.\n\n" +
                    "User Context:\n" +
                    "- Recent reflection answers/notes:\n- %s\n" +
                    "- Active goals/todos: [%s]\n" +
                    "- Recent struggle/stressor themes: [%s]\n" +
                    "- Focus: \"%s\"\n\n" +
                    "Generate a JSON array of objects with this exact structure:\n" +
                    "[\n" +
                    "  {\n" +
                    "    \"question\": \"The follow-up prompt text\",\n" +
                    "    \"rationale\": \"Why this prompt was synthesized based on the user's specific answers or goals\",\n" +
                    "    \"sourceContext\": \"RECENT_REFLECTION, ACTIVE_GOAL, UNRESOLVED_STRUGGLE, or HABIT_MOMENTUM\",\n" +
                    "    \"targetFocus\": \"Key virtue or theme\"\n" +
                    "  }\n" +
                    "]\n" +
                    "Respond with ONLY the raw JSON array.",
                    context.count() > 0 ? context.count() : 4,
                    reflectionsStr,
                    goalsStr,
                    strugglesStr,
                    focus
            );

            String rawJson = executeGeminiPrompt(prompt);
            JsonNode root = parseJson(rawJson);

            List<FollowUpPromptResponse> followUps = new ArrayList<>();
            if (root.isArray()) {
                for (JsonNode item : root) {
                    String q = item.path("question").asText("");
                    String r = item.path("rationale").asText("");
                    String sc = item.path("sourceContext").asText("RECENT_REFLECTION");
                    String tf = item.path("targetFocus").asText(focus);
                    if (StringUtils.hasText(q)) {
                        followUps.add(new FollowUpPromptResponse(q, r, sc, tf));
                    }
                }
            }

            if (followUps.isEmpty()) {
                return fallbackClient.buildFollowUpPrompts(context);
            }

            return followUps;
        } catch (Exception ex) {
            logger.warn("Gemini buildFollowUpPrompts failed [{}], using fallback: {}", ex.getClass().getSimpleName(), ex.getMessage());
            return fallbackClient.buildFollowUpPrompts(context);
        }
    }

    @Override
    public List<SemanticTagResponse> extractSemanticTags(SemanticTagContext context) {
        if (!isConfigured()) {
            return fallbackClient.extractSemanticTags(context);
        }

        try {
            Long sessionId = context.session() != null ? context.session().getId() : null;
            StringBuilder textContent = new StringBuilder();

            if (context.answers() != null) {
                for (Answer a : context.answers()) {
                    String category = a.getQuestion() != null && a.getQuestion().getCategory() != null
                            ? a.getQuestion().getCategory().getName() : "General";
                    String reflection = a.getReflectionText() != null ? a.getReflectionText() : a.getAnswerText();
                    String score = a.getFeelingScore() != null ? String.valueOf(a.getFeelingScore()) : "N/A";
                    textContent.append(String.format("Category: %s | Rating: %s/5 | Reflection: \"%s\"\n",
                            category, score, reflection != null ? reflection : ""));
                }
            }

            if (context.session() != null && StringUtils.hasText(context.session().getNotes())) {
                textContent.append(String.format("Session Notes: \"%s\"\n", context.session().getNotes()));
            }

            String prompt = String.format(
                    "You are an empathetic spiritual and reflective guide for the Examen examination of conscience app.\n" +
                    "Analyze the user's reflections from this completed examination session:\n%s\n\n" +
                    "Extract semantic tags categorized into exactly 4 types:\n" +
                    "- VIRTUE (moral strengths, patience, charity, honesty, courage, humility)\n" +
                    "- STRUGGLE (temptations, impatience, anxiety, distractions, anger)\n" +
                    "- GRATITUDE (blessings, relationships, thanksgiving, quiet moments)\n" +
                    "- STRESSOR (work pressure, fatigue, friction, overload)\n\n" +
                    "Generate a JSON response with this exact structure:\n" +
                    "{\n" +
                    "  \"tags\": [\n" +
                    "    {\n" +
                    "      \"category\": \"VIRTUE\",\n" +
                    "      \"tag\": \"Patience Under Pressure\",\n" +
                    "      \"confidence\": 0.95\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}\n" +
                    "Respond with ONLY the raw JSON object.",
                    textContent.toString()
            );

            String rawJson = executeGeminiPrompt(prompt);
            JsonNode root = parseJson(rawJson);

            List<SemanticTagResponse> result = new ArrayList<>();
            JsonNode tagsNode = root.path("tags");
            if (tagsNode.isArray()) {
                for (JsonNode item : tagsNode) {
                    String catStr = item.path("category").asText("").toUpperCase(Locale.US).trim();
                    String tagText = item.path("tag").asText("").trim();
                    double confidence = item.path("confidence").asDouble(0.9);

                    if (StringUtils.hasText(tagText)) {
                        SemanticTagCategory cat;
                        try {
                            cat = SemanticTagCategory.valueOf(catStr);
                        } catch (Exception e) {
                            cat = SemanticTagCategory.VIRTUE;
                        }
                        result.add(new SemanticTagResponse(
                                null,
                                sessionId,
                                null,
                                cat,
                                tagText,
                                confidence,
                                LocalDateTime.now()
                        ));
                    }
                }
            }

            if (result.isEmpty()) {
                return fallbackClient.extractSemanticTags(context);
            }

            return result;
        } catch (Exception ex) {
            logger.warn("Gemini extractSemanticTags failed [{}], using fallback: {}", ex.getClass().getSimpleName(), ex.getMessage());
            return fallbackClient.extractSemanticTags(context);
        }
    }

    @Override
    public WeeklyNarrativeResponse buildWeeklyNarrative(WeeklyNarrativeContext context) {
        if (!isConfigured()) {
            return fallbackClient.buildWeeklyNarrative(context);
        }

        try {
            int sessionsCount = context.sessions() != null ? context.sessions().size() : 0;
            Double avgFeeling = context.sessions() == null || context.sessions().isEmpty() ? null :
                    context.sessions().stream()
                            .map(com.rei.examenbackend.model.ExaminationSession::getMoodScore)
                            .filter(java.util.Objects::nonNull)
                            .mapToInt(Integer::intValue)
                            .average()
                            .orElse(Double.NaN);
            Double safeAvg = (avgFeeling == null || Double.isNaN(avgFeeling)) ? null : avgFeeling;

            List<String> tagsSummary = context.recentTags() == null ? List.of() :
                    context.recentTags().stream()
                            .map(t -> String.format("[%s: %s]", t.getCategory(), t.getTag()))
                            .distinct()
                            .toList();

            List<String> gratitudeItems = context.recentGratitude() == null ? List.of() :
                    context.recentGratitude().stream()
                            .map(GratitudeEntry::getContent)
                            .filter(StringUtils::hasText)
                            .toList();

            List<String> userFeedback = context.userFeedbackPreferences() == null ? List.of() :
                    context.userFeedbackPreferences().stream()
                            .map(f -> String.format("%s was rated %s%s",
                                    f.getTargetType(),
                                    f.isHelpful() ? "HELPFUL" : "NOT HELPFUL",
                                    StringUtils.hasText(f.getComments()) ? " (comment: " + f.getComments() + ")" : ""))
                            .toList();

            String prompt = String.format(
                    "You are an empathetic spiritual and reflective guide for the Examen examination of conscience app.\n" +
                    "Synthesize the user's weekly reflection report for period %s to %s:\n" +
                    "- Completed sessions: %d\n" +
                    "- Average feeling score: %s\n" +
                    "- Semantic patterns detected: %s\n" +
                    "- Gratitude highlights: %s\n" +
                    "- Past user feedback preferences: %s\n\n" +
                    "Instructions:\n" +
                    "1. Write an empathetic, thoughtful 2-paragraph narrative summarizing their spiritual and psychological flow.\n" +
                    "2. Notice virtues, struggles, gratitude, and stress signals.\n" +
                    "3. Provide 2-3 tailored, concrete next steps. Respect past user feedback: if certain suggestions were marked NOT HELPFUL, avoid similar phrasing and make advice more concrete and concise.\n\n" +
                    "Generate a JSON response with this exact structure:\n" +
                    "{\n" +
                    "  \"title\": \"Inspiring thematic title for this week\",\n" +
                    "  \"narrative\": \"2-3 paragraphs synthesizing the week with depth and encouragement\",\n" +
                    "  \"virtuesObserved\": [\"virtue 1\", \"virtue 2\"],\n" +
                    "  \"strugglesIdentified\": [\"struggle 1\", \"struggle 2\"],\n" +
                    "  \"gratitudeThemes\": [\"gratitude 1\", \"gratitude 2\"],\n" +
                    "  \"stressorSignals\": [\"stressor 1\", \"stressor 2\"],\n" +
                    "  \"actionableAdvice\": [\"advice 1\", \"advice 2\"]\n" +
                    "}\n" +
                    "Respond with ONLY the raw JSON object.",
                    context.periodStart(),
                    context.periodEnd(),
                    sessionsCount,
                    safeAvg != null ? String.format(Locale.US, "%.1f/5", safeAvg) : "none recorded",
                    String.join(", ", tagsSummary),
                    String.join("; ", gratitudeItems),
                    String.join("; ", userFeedback)
            );

            String rawJson = executeGeminiPrompt(prompt);
            JsonNode root = parseJson(rawJson);

            String title = root.path("title").asText("");
            String narrative = root.path("narrative").asText("");

            if (!StringUtils.hasText(title) || !StringUtils.hasText(narrative)) {
                return fallbackClient.buildWeeklyNarrative(context);
            }

            List<String> virtues = readStringList(root.path("virtuesObserved"));
            List<String> struggles = readStringList(root.path("strugglesIdentified"));
            List<String> gratitude = readStringList(root.path("gratitudeThemes"));
            List<String> stressors = readStringList(root.path("stressorSignals"));
            List<String> advice = readStringList(root.path("actionableAdvice"));

            return new WeeklyNarrativeResponse(
                    title,
                    narrative,
                    context.periodStart(),
                    context.periodEnd(),
                    sessionsCount,
                    safeAvg,
                    virtues,
                    struggles,
                    gratitude,
                    stressors,
                    advice,
                    LocalDateTime.now()
            );
        } catch (Exception ex) {
            logger.warn("Gemini buildWeeklyNarrative failed [{}], using fallback: {}", ex.getClass().getSimpleName(), ex.getMessage());
            return fallbackClient.buildWeeklyNarrative(context);
        }
    }

    public boolean isConfigured() {
        return properties.isEnabled() && StringUtils.hasText(properties.getApiKey());
    }

    private String executeGeminiPrompt(String promptText) {
        String url = String.format(
                "%s/models/%s:generateContent",
                properties.getBaseUrl().replaceAll("/+$", ""),
                properties.getModel()
        );

        Map<String, Object> requestPayload = Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(
                                        Map.of("text", promptText)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.3,
                        "responseMimeType", "application/json"
                )
        );

        String responseBody = restClient.post()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("x-goog-api-key", properties.getApiKey())
                .body(requestPayload)
                .retrieve()
                .body(String.class);

        return extractCandidateText(responseBody);
    }

    private String extractCandidateText(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            throw new IllegalStateException("Empty response from Gemini API");
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    return parts.get(0).path("text").asText("");
                }
            }
            throw new IllegalStateException("No candidate text found in Gemini response");
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse Gemini response wrapper: " + ex.getMessage(), ex);
        }
    }

    private JsonNode parseJson(String text) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalStateException("Empty JSON candidate text");
        }
        String clean = text.trim();
        if (clean.startsWith("```json")) {
            clean = clean.substring(7);
        } else if (clean.startsWith("```")) {
            clean = clean.substring(3);
        }
        if (clean.endsWith("```")) {
            clean = clean.substring(0, clean.length() - 3);
        }
        clean = clean.trim();
        try {
            return objectMapper.readTree(clean);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse clean JSON from Gemini: " + ex.getMessage(), ex);
        }
    }

    private List<String> readStringList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : node) {
                if (StringUtils.hasText(item.asText())) {
                    list.add(item.asText().trim());
                }
            }
        }
        return list;
    }
}
