package com.rei.examenbackend.config;

import com.rei.examenbackend.model.Category;
import com.rei.examenbackend.model.Question;
import com.rei.examenbackend.model.QuestionType;
import com.rei.examenbackend.repository.CategoryRepository;
import com.rei.examenbackend.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;

    private static final List<CategorySeed> CATEGORY_SEEDS = List.of(
            new CategorySeed(
                    "Reflection",
                    "Core examen prompts",
                    List.of(
                            "Where did I notice gratitude most clearly today?",
                            "When did I act out of love rather than fear?",
                            "Which habit or vice surfaced today, and how did I respond?",
                            "Where did I feel closest to God/meaning, and where did I feel distant?",
                            "What is one concrete step I will take tomorrow to grow?"
                    )
            ),
            new CategorySeed(
                    "Prayer",
                    "Prayer and presence",
                    List.of(
                            "Did I begin the day with prayer or intention?",
                            "When did I pause to listen or be still?",
                            "Where did I ignore a prompt toward prayer or silence?",
                            "How present was I with God or meaning during ordinary moments?"
                    )
            ),
            new CategorySeed(
                    "Relationships",
                    "Charity and reconciliation",
                    List.of(
                            "Where did I show patience or kindness in my relationships?",
                            "Where did I speak harshly, judge, or withdraw?",
                            "Did I seek reconciliation where it was needed?",
                            "How did I support someone who needed encouragement?"
                    )
            ),
            new CategorySeed(
                    "Integrity",
                    "Truth, responsibility, and trust",
                    List.of(
                            "Where did I act with integrity even when it was hard?",
                            "Where did I cut corners or avoid responsibility?",
                            "Did I keep my commitments today?",
                            "How honest was I in my words and decisions?"
                    )
            ),
            new CategorySeed(
                    "Service",
                    "Compassion and generosity",
                    List.of(
                            "Did I notice someone in need and respond?",
                            "Where did I choose comfort over service?",
                            "How did I use my gifts for others today?",
                            "Did I give time or attention generously?"
                    )
            ),
            new CategorySeed(
                    "Stewardship",
                    "Rhythm, time, and care",
                    List.of(
                            "How well did I steward my time and energy?",
                            "Did I take healthy rest and care for my body?",
                            "Where did I overindulge or waste time?",
                            "Did I practice gratitude for what I have?"
                    )
            )
    );

    @Bean
    @Transactional
    public ApplicationRunner seedDefaults() {
        return args -> {
            for (CategorySeed seed : CATEGORY_SEEDS) {
                Category category = categoryRepository.findByNameIgnoreCaseAndOwnerIsNull(seed.name())
                        .orElseGet(() -> categoryRepository.save(Category.builder()
                                .name(seed.name())
                                .description(seed.description())
                                .build()));

                List<Question> existingQuestions = questionRepository.findByCategoryId(category.getId());
                Set<String> existingTexts = existingQuestions.stream()
                        .map(Question::getText)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                int order = existingQuestions.stream()
                        .map(Question::getOrderNumber)
                        .filter(Objects::nonNull)
                        .max(Integer::compareTo)
                        .orElse(0) + 1;

                for (String text : seed.questions()) {
                    if (existingTexts.contains(text)) {
                        continue;
                    }
                    Question question = Question.builder()
                            .text(text)
                            .orderNumber(order++)
                            .responseType(QuestionType.SCALE_1_5)
                            .category(category)
                            .custom(false)
                            .build();
                    questionRepository.save(question);
                }
            }
        };
    }

    private record CategorySeed(String name, String description, List<String> questions) {}
}
