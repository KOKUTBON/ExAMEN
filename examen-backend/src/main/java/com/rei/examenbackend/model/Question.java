package com.rei.examenbackend.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "questions",
        indexes = {
                @Index(name = "idx_question_category", columnList = "category_id"),
                @Index(name = "idx_question_owner", columnList = "owner_id")
        })
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String text;

    private Integer orderNumber;

    @Enumerated(EnumType.STRING)
    private QuestionType responseType;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Optional owner for custom questions
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    private boolean custom;

    // Archived questions are hidden from the library but keep their historical answers.
    @Column(nullable = false)
    private boolean archived;

    @OneToMany(mappedBy = "question")
    private List<Answer> answers;

    // Optional difficulty
    private String difficulty;

    public Question() {}

    public Question(Long id, String text, Integer orderNumber, QuestionType responseType,
                    Category category, User owner, boolean custom, List<Answer> answers,
                    String difficulty) {
        this.id = id;
        this.text = text;
        this.orderNumber = orderNumber;
        this.responseType = responseType;
        this.category = category;
        this.owner = owner;
        this.custom = custom;
        this.answers = answers;
        this.difficulty = difficulty;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Integer getOrderNumber() { return orderNumber; }
    public void setOrderNumber(Integer orderNumber) { this.orderNumber = orderNumber; }

    public QuestionType getResponseType() { return responseType; }
    public void setResponseType(QuestionType responseType) { this.responseType = responseType; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public boolean isCustom() { return custom; }
    public void setCustom(boolean custom) { this.custom = custom; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public List<Answer> getAnswers() { return answers; }
    public void setAnswers(List<Answer> answers) { this.answers = answers; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public static class Builder {
        private Long id;
        private String text;
        private Integer orderNumber;
        private QuestionType responseType;
        private Category category;
        private User owner;
        private boolean custom;
        private List<Answer> answers;
        private String difficulty;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder text(String text) { this.text = text; return this; }
        public Builder orderNumber(Integer orderNumber) { this.orderNumber = orderNumber; return this; }
        public Builder responseType(QuestionType responseType) { this.responseType = responseType; return this; }
        public Builder category(Category category) { this.category = category; return this; }
        public Builder owner(User owner) { this.owner = owner; return this; }
        public Builder custom(boolean custom) { this.custom = custom; return this; }
        public Builder answers(List<Answer> answers) { this.answers = answers; return this; }
        public Builder difficulty(String difficulty) { this.difficulty = difficulty; return this; }

        public Question build() {
            return new Question(id, text, orderNumber, responseType, category, owner, custom, answers, difficulty);
        }
    }
}
