package com.rei.examenbackend.service.insights;

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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class StubInsightsClient implements InsightsClient {

    private static final Map<String, List<String>> SUGGESTION_BANK = Map.of(
            "gratitude", List.of(
                    "Where did I notice gratitude today?",
                    "Who or what am I thankful for right now?",
                    "How did I express gratitude in action?",
                    "What small gift did I overlook today?"
            ),
            "prayer", List.of(
                    "When did I feel most present in prayer?",
                    "Where did I rush past silence or stillness?",
                    "How did I invite God or meaning into my choices today?",
                    "What word or phrase stayed with me in prayer?"
            ),
            "relationships", List.of(
                    "Where did I choose patience in a relationship?",
                    "Who did I need to forgive or ask forgiveness from?",
                    "How did I show care in a small but real way?",
                    "Where did I withdraw instead of engaging with love?"
            ),
            "service", List.of(
                    "Where did I choose generosity over comfort today?",
                    "How did I serve with attention, not just action?",
                    "Who did I overlook who needed help?",
                    "What act of service brought me joy?"
            ),
            "integrity", List.of(
                    "Where did I speak truthfully and clearly today?",
                    "Where did I avoid responsibility?",
                    "How did I act with integrity under pressure?",
                    "What commitment did I honor today?"
            ),
            "stewardship", List.of(
                    "How did I steward my time today?",
                    "Where did I waste energy or attention?",
                    "How did I care for my body and mind?",
                    "What would a balanced rhythm look like tomorrow?"
            )
    );

    @Override
    public InsightsSummaryResponse buildSummary(InsightsSummaryContext context) {
        String summary;
        List<String> highlights = new ArrayList<>();
        List<EvidenceSnippetResponse> evidence = new ArrayList<>();
        double confidenceScore;

        if (context.sessionsCompleted() == 0) {
            summary = "No completed sessions in this period. Start a new examination to build momentum.";
            confidenceScore = 0.15;
            evidence.add(new EvidenceSnippetResponse(
                    "Historical Baseline",
                    context.endDate(),
                    String.format(Locale.US, "0 sessions logged across %d days", context.periodDays()),
                    "Baseline reference: additional examination sessions will increase profile insight depth"
            ));
        } else {
            String avg = context.averageFeeling() == null
                    ? "no recorded feeling"
                    : String.format(Locale.US, "%.1f/5", context.averageFeeling());
            summary = String.format(
                    "In the last %d days you completed %d sessions with an average feeling of %s.",
                    context.periodDays(),
                    context.sessionsCompleted(),
                    avg
            );

            double base = Math.min(0.92, 0.45 + (context.sessionsCompleted() * 0.08));
            if (context.averageFeeling() != null) {
                base += 0.04;
            }
            boolean hasReflections = context.recentSessions() != null && context.recentSessions().stream()
                    .anyMatch(s -> StringUtils.hasText(s.notes()) || (s.reflectionSnippets() != null && !s.reflectionSnippets().isEmpty()));
            if (hasReflections) {
                base += 0.04;
            }
            confidenceScore = Math.round(Math.min(0.95, base) * 100.0) / 100.0;

            if (context.recentSessions() != null) {
                for (InsightsSummaryContext.SessionContextSnippet s : context.recentSessions()) {
                    if (evidence.size() >= 3) {
                        break;
                    }
                    if (StringUtils.hasText(s.notes())) {
                        evidence.add(new EvidenceSnippetResponse(
                                "Session Note",
                                s.date(),
                                s.notes().trim(),
                                "Reflects direct self-examination and personal focus"
                        ));
                    } else if (s.reflectionSnippets() != null && !s.reflectionSnippets().isEmpty()) {
                        evidence.add(new EvidenceSnippetResponse(
                                "Reflection Prompt",
                                s.date(),
                                s.reflectionSnippets().get(0).trim(),
                                "Illustrates mindful contemplation during daily examination prompt"
                        ));
                    } else if (s.moodScore() != null) {
                        evidence.add(new EvidenceSnippetResponse(
                                "Session Mood",
                                s.date(),
                                String.format(Locale.US, "Feeling rating: %d/5", s.moodScore()),
                                "Demonstrates emotional self-awareness in examination practice"
                        ));
                    }
                }
            }

            if (context.gratitudeSnippets() != null && !context.gratitudeSnippets().isEmpty()) {
                evidence.add(new EvidenceSnippetResponse(
                        "Gratitude Journal",
                        context.endDate(),
                        context.gratitudeSnippets().get(0).trim(),
                        "Highlights spontaneous moments of appreciation and thanksgiving"
                ));
            }

            if (evidence.isEmpty()) {
                evidence.add(new EvidenceSnippetResponse(
                        "Activity Log",
                        context.endDate(),
                        String.format(Locale.US, "%d completed sessions over %d days", context.sessionsCompleted(), context.periodDays()),
                        "Consistent participation trend supporting profile synthesis"
                ));
            }
        }

        highlights.add("Period days: " + context.periodDays());
        highlights.add("Sessions completed: " + context.sessionsCompleted());
        highlights.add("Average feeling: " + (context.averageFeeling() == null
                ? "-"
                : String.format(Locale.US, "%.1f/5", context.averageFeeling())));

        return new InsightsSummaryResponse(
                summary,
                highlights,
                context.periodDays(),
                context.sessionsCompleted(),
                context.averageFeeling(),
                confidenceScore,
                evidence,
                LocalDateTime.now()
        );
    }

    @Override
    public SessionInsightResponse buildSessionInsight(SessionInsightContext context) {
        String summary;
        List<String> insights = new ArrayList<>();
        List<String> nextSteps = new ArrayList<>();

        if (context.averageFeeling() == null) {
            summary = "Session completed with no recorded feeling score.";
        } else {
            summary = String.format(Locale.US, "Session average feeling: %.1f/5.", context.averageFeeling());
        }

        if (!context.strongestCategories().isEmpty()) {
            insights.add("Strongest areas: " + String.join(", ", context.strongestCategories()) + ".");
        }
        if (!context.needsAttentionCategories().isEmpty()) {
            insights.add("Growth opportunities: " + String.join(", ", context.needsAttentionCategories()) + ".");
        }
        if (insights.isEmpty()) {
            insights.add("Complete more reflections to reveal your strongest and weakest areas.");
        }

        nextSteps.add("Choose one concrete action for tomorrow.");
        nextSteps.add("Revisit one question that felt difficult today.");
        nextSteps.add("Close with a short gratitude or prayer.");

        return new SessionInsightResponse(
                context.session().getId(),
                summary,
                insights,
                nextSteps,
                context.averageFeeling(),
                LocalDateTime.now()
        );
    }

    @Override
    public QuestionSuggestionResponse buildQuestionSuggestions(QuestionSuggestionContext context) {
        String focus = normalizeFocus(context.focus());
        int count = context.count();
        List<String> existing = context.existingQuestions();
        List<String> existingNormalized = existing == null
                ? List.of()
                : existing.stream()
                .map(item -> item == null ? null : item.toLowerCase(Locale.US))
                .filter(item -> item != null && !item.isBlank())
                .toList();

        List<String> base = new ArrayList<>(
                SUGGESTION_BANK.getOrDefault(focus, SUGGESTION_BANK.get("gratitude"))
        );
        List<String> suggestions = new ArrayList<>();

        for (String candidate : base) {
            if (suggestions.size() >= count) {
                break;
            }
            String normalizedCandidate = candidate.toLowerCase(Locale.US);
            if (existingNormalized.contains(normalizedCandidate)) {
                continue;
            }
            suggestions.add(candidate);
        }

        while (suggestions.size() < count) {
            String fallback = "Where did I practice " + focus + " today?";
            String normalizedFallback = fallback.toLowerCase(Locale.US);
            if (!suggestions.contains(fallback) && !existingNormalized.contains(normalizedFallback)) {
                suggestions.add(fallback);
            } else {
                suggestions.add("What is one step I can take to grow in " + focus + "?");
            }
        }

        List<FollowUpPromptResponse> followUps = buildFollowUpPrompts(context);
        return new QuestionSuggestionResponse(focus, suggestions, followUps, LocalDateTime.now());
    }

    @Override
    public List<FollowUpPromptResponse> buildFollowUpPrompts(QuestionSuggestionContext context) {
        List<FollowUpPromptResponse> followUps = new ArrayList<>();
        int limit = context.count() > 0 ? context.count() : 5;

        // 1. Follow up on recent reflection answers if present
        if (context.recentAnswers() != null && !context.recentAnswers().isEmpty()) {
            for (String answer : context.recentAnswers()) {
                if (followUps.size() >= limit) break;
                String snippet = answer.trim();
                String truncated = snippet.length() > 60 ? snippet.substring(0, 57) + "..." : snippet;
                followUps.add(new FollowUpPromptResponse(
                        "How has your experience with \"" + truncated + "\" shaped your patience and choices today?",
                        "Follows up directly on your recent reflection: \"" + truncated + "\"",
                        "RECENT_REFLECTION",
                        "Reflection Deepening"
                ));
            }
        }

        // 2. Follow up on active goals (pending todos / habits)
        if (context.activeGoals() != null && !context.activeGoals().isEmpty()) {
            for (String goal : context.activeGoals()) {
                if (followUps.size() >= limit) break;
                followUps.add(new FollowUpPromptResponse(
                        "What is currently standing between you and completing your goal: \"" + goal + "\"?",
                        "Grounded in your active commitment to \"" + goal + "\"",
                        "ACTIVE_GOAL",
                        "Goal Momentum"
                ));
            }
        }

        // 3. Follow up on recent struggles
        if (context.recentStruggles() != null && !context.recentStruggles().isEmpty()) {
            for (String struggle : context.recentStruggles()) {
                if (followUps.size() >= limit) break;
                followUps.add(new FollowUpPromptResponse(
                        "Where did you feel tension or difficulty regarding \"" + struggle + "\", and where can you invite grace?",
                        "Addresses an ongoing struggle theme detected in recent sessions: \"" + struggle + "\"",
                        "UNRESOLVED_STRUGGLE",
                        "Spiritual Healing"
                ));
            }
        }

        // 4. Default fallback if insufficient context
        if (followUps.isEmpty()) {
            String focus = normalizeFocus(context.focus());
            followUps.add(new FollowUpPromptResponse(
                    "Where in your day did you notice the presence of " + focus + ", and how did you respond?",
                    "Explores your intentional focus on " + focus + " across today's activities.",
                    "HABIT_MOMENTUM",
                    focus
            ));
            followUps.add(new FollowUpPromptResponse(
                    "What choice or conversation today gave you the deepest sense of peace or purpose?",
                    "Encourages discernment of consolation and meaningful action.",
                    "RECENT_REFLECTION",
                    "Discernment"
            ));
        }

        return followUps;
    }

    @Override
    public List<SemanticTagResponse> extractSemanticTags(SemanticTagContext context) {
        List<SemanticTagResponse> tags = new ArrayList<>();
        Long sessionId = context.session() != null ? context.session().getId() : null;

        if (context.answers() != null) {
            for (Answer answer : context.answers()) {
                String text = answer.getReflectionText();
                if (text != null && !text.isBlank()) {
                    String lower = text.toLowerCase(Locale.US);
                    checkAndAddKeywords(lower, answer.getId(), sessionId, tags);
                }

                if (answer.getFeelingScore() != null) {
                    if (answer.getFeelingScore() >= 4) {
                        tags.add(new SemanticTagResponse(null, sessionId, answer.getId(),
                                SemanticTagCategory.GRATITUDE, "Peace & Contentment", 0.85, LocalDateTime.now()));
                    } else if (answer.getFeelingScore() <= 2) {
                        tags.add(new SemanticTagResponse(null, sessionId, answer.getId(),
                                SemanticTagCategory.STRUGGLE, "Restlessness & Weariness", 0.85, LocalDateTime.now()));
                    }
                }
            }
        }

        if (context.session() != null && context.session().getNotes() != null && !context.session().getNotes().isBlank()) {
            String notesLower = context.session().getNotes().toLowerCase(Locale.US);
            checkAndAddKeywords(notesLower, null, sessionId, tags);
        }

        if (tags.isEmpty()) {
            tags.add(new SemanticTagResponse(null, sessionId, null,
                    SemanticTagCategory.VIRTUE, "Mindful Presence", 0.8, LocalDateTime.now()));
            tags.add(new SemanticTagResponse(null, sessionId, null,
                    SemanticTagCategory.GRATITUDE, "Quiet Reflection", 0.8, LocalDateTime.now()));
        }

        return tags;
    }

    private void checkAndAddKeywords(String text, Long answerId, Long sessionId, List<SemanticTagResponse> tags) {
        Map<SemanticTagCategory, List<String>> keywordMap = Map.of(
                SemanticTagCategory.VIRTUE, List.of("patient", "patience", "kind", "kindness", "honest", "honesty",
                        "peace", "calm", "prayer", "prayed", "humble", "humility", "generous", "giving", "love", "loving",
                        "courage", "disciplined", "diligent", "attentive", "forgive", "forgave"),
                SemanticTagCategory.STRUGGLE, List.of("angry", "anger", "impatient", "rush", "rushed", "distracted",
                        "distraction", "lazy", "sloth", "pride", "anxious", "anxiety", "fear", "fail", "failed",
                        "selfish", "guilt", "resentment", "doubt", "procrastinate"),
                SemanticTagCategory.GRATITUDE, List.of("thank", "thankful", "thanks", "grateful", "gratitude",
                        "blessed", "blessing", "gift", "family", "friend", "friends", "nature", "peace", "meal",
                        "sunshine", "support", "forgiven", "joy"),
                SemanticTagCategory.STRESSOR, List.of("stress", "stressed", "work", "deadline", "busy", "tired",
                        "exhausted", "fatigue", "overwhelm", "overwhelmed", "conflict", "argument", "pressure",
                        "hurry", "sick", "pain")
        );

        for (Map.Entry<SemanticTagCategory, List<String>> entry : keywordMap.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (text.contains(keyword)) {
                    String tagName = Character.toUpperCase(keyword.charAt(0)) + keyword.substring(1);
                    boolean exists = tags.stream().anyMatch(t -> t.getCategory() == entry.getKey() && t.getTag().equalsIgnoreCase(tagName));
                    if (!exists) {
                        tags.add(new SemanticTagResponse(null, sessionId, answerId, entry.getKey(), tagName, 0.9, LocalDateTime.now()));
                    }
                }
            }
        }
    }

    @Override
    public WeeklyNarrativeResponse buildWeeklyNarrative(WeeklyNarrativeContext context) {
        int sessionCount = context.sessions() != null ? context.sessions().size() : 0;
        double avgFeeling = 0.0;
        if (context.sessions() != null && !context.sessions().isEmpty()) {
            avgFeeling = context.sessions().stream()
                    .map(com.rei.examenbackend.model.ExaminationSession::getMoodScore)
                    .filter(java.util.Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(3.5);
        }

        List<String> virtues = new ArrayList<>();
        List<String> struggles = new ArrayList<>();
        List<String> gratitude = new ArrayList<>();
        List<String> stressors = new ArrayList<>();

        if (context.recentTags() != null) {
            for (SemanticTag tag : context.recentTags()) {
                if (tag.getCategory() == SemanticTagCategory.VIRTUE && !virtues.contains(tag.getTag())) {
                    virtues.add(tag.getTag());
                } else if (tag.getCategory() == SemanticTagCategory.STRUGGLE && !struggles.contains(tag.getTag())) {
                    struggles.add(tag.getTag());
                } else if (tag.getCategory() == SemanticTagCategory.GRATITUDE && !gratitude.contains(tag.getTag())) {
                    gratitude.add(tag.getTag());
                } else if (tag.getCategory() == SemanticTagCategory.STRESSOR && !stressors.contains(tag.getTag())) {
                    stressors.add(tag.getTag());
                }
            }
        }

        if (context.recentGratitude() != null) {
            for (GratitudeEntry g : context.recentGratitude()) {
                if (g.getContent() != null && !gratitude.contains(g.getContent())) {
                    gratitude.add(g.getContent());
                }
            }
        }

        if (virtues.isEmpty()) virtues.addAll(List.of("Consistency in Daily Reflection", "Attentive Self-Awareness"));
        if (struggles.isEmpty()) struggles.addAll(List.of("Occasional Hurry in Busy Hours", "Wandering Focus"));
        if (gratitude.isEmpty()) gratitude.addAll(List.of("Daily Gifts and Small Mercies", "Support of Loved Ones"));
        if (stressors.isEmpty()) stressors.addAll(List.of("Pacing Under Daily Demands", "Rest and Recovery"));

        List<String> advice = new ArrayList<>(List.of(
                "Protect 5 minutes of stillness at midday to reset your focus before moving to complex tasks.",
                "Begin your next examination with one deliberate breath of gratitude for what went right.",
                "Notice when hurry arises tomorrow, and pause briefly before responding."
        ));

        // Incorporate user feedback preferences
        if (context.userFeedbackPreferences() != null) {
            boolean hasUnhelpful = context.userFeedbackPreferences().stream()
                    .anyMatch(f -> !f.isHelpful());
            if (hasUnhelpful) {
                advice.clear();
                advice.add("Choose one simple micro-pause during high-stress hours tomorrow.");
                advice.add("Revisit the gratitude list when feeling overwhelmed.");
            }
        }

        String title = sessionCount >= 4
                ? "A Week of Grounded Commitment and Deepening Awareness"
                : "A Gentle Week of Beginnings and Renewal";

        String narrative = String.format(Locale.US,
                "Over this past week, you completed %d examination sessions with an average feeling of %.1f/5. " +
                "Your reflections show sincere effort to discern virtues such as %s amidst the press of life.\n\n" +
                "You navigated challenges including %s, while continuing to notice blessings and gratitude. " +
                "By acknowledging both strengths and tension points with honesty, you cultivate a steady, patient rhythm of growth.",
                sessionCount,
                avgFeeling,
                String.join(", ", virtues.stream().limit(2).toList()),
                String.join(", ", struggles.stream().limit(2).toList())
        );

        return new WeeklyNarrativeResponse(
                title,
                narrative,
                context.periodStart(),
                context.periodEnd(),
                sessionCount,
                avgFeeling > 0 ? avgFeeling : null,
                virtues,
                struggles,
                gratitude,
                stressors,
                advice,
                LocalDateTime.now()
        );
    }

    private String normalizeFocus(String focus) {
        if (focus == null || focus.isBlank()) {
            return "gratitude";
        }
        return focus.trim().toLowerCase(Locale.US);
    }
}

