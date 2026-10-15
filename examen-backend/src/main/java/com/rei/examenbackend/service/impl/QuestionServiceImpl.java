package com.rei.examenbackend.service.impl;

import com.rei.examenbackend.dto.category.CategoryResponse;
import com.rei.examenbackend.dto.question.QuestionRequest;
import com.rei.examenbackend.dto.question.QuestionResponse;
import com.rei.examenbackend.exception.ApiException;
import com.rei.examenbackend.model.Category;
import com.rei.examenbackend.model.Question;
import com.rei.examenbackend.model.QuestionType;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.repository.CategoryRepository;
import com.rei.examenbackend.repository.QuestionRepository;
import com.rei.examenbackend.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default questions (no owner) are shared by everyone; custom questions are visible only to their owner.
 * Deleting a question archives it so historical answers remain intact.
 */
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public QuestionResponse create(QuestionRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .filter(c -> c.getOwner() == null)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Default category not found"));

        Question question = Question.builder()
                .text(request.getText().trim())
                .orderNumber(request.getOrderNumber())
                .difficulty(request.getDifficulty())
                .responseType(responseType(request))
                .category(category)
                .build();

        return toResponse(questionRepository.save(question));
    }

    @Override
    public QuestionResponse createCustom(QuestionRequest request, User owner) {
        Question question = Question.builder()
                .text(request.getText().trim())
                .orderNumber(request.getOrderNumber())
                .difficulty(request.getDifficulty())
                .responseType(responseType(request))
                .category(visibleCategory(request.getCategoryId(), owner))
                .owner(owner)
                .custom(true)
                .build();

        return toResponse(questionRepository.save(question));
    }

    @Override
    public QuestionResponse update(Long id, QuestionRequest request) {
        Question question = findActive(id);
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found"));
        apply(question, request, category);
        return toResponse(questionRepository.save(question));
    }

    @Override
    public void delete(Long id) {
        Question question = findActive(id);
        question.setArchived(true);
        questionRepository.save(question);
    }

    @Override
    public QuestionResponse getById(Long id, User user) {
        Question question = findActive(id);
        if (question.getOwner() != null && !question.getOwner().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Question not found");
        }
        return toResponse(question);
    }

    @Override
    public Page<QuestionResponse> getAll(Pageable pageable, User user) {
        return questionRepository.findVisibleTo(user.getId(), pageable).map(this::toResponse);
    }

    @Override
    public List<QuestionResponse> getByCategory(Long categoryId, User user) {
        return questionRepository.findVisibleInCategory(categoryId, user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<QuestionResponse> getMine(User owner) {
        return questionRepository.findByOwnerIdAndArchivedFalse(owner.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void deleteMine(Long id, User owner) {
        Question question = findOwned(id, owner, "You cannot delete this question");
        question.setArchived(true);
        questionRepository.save(question);
    }

    @Override
    public QuestionResponse updateMine(Long id, QuestionRequest request, User owner) {
        Question question = findOwned(id, owner, "You cannot edit this question");
        apply(question, request, visibleCategory(request.getCategoryId(), owner));
        return toResponse(questionRepository.save(question));
    }

    private Question findActive(Long id) {
        return questionRepository.findById(id)
                .filter(q -> !q.isArchived())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found"));
    }

    private Question findOwned(Long id, User owner, String forbiddenMessage) {
        Question question = findActive(id);
        if (question.getOwner() == null || !question.getOwner().getId().equals(owner.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, forbiddenMessage);
        }
        return question;
    }

    private Category visibleCategory(Long categoryId, User user) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found"));
        if (category.getOwner() != null && !category.isOwnedBy(user)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Category not found");
        }
        return category;
    }

    private void apply(Question question, QuestionRequest request, Category category) {
        question.setText(request.getText().trim());
        question.setOrderNumber(request.getOrderNumber());
        question.setDifficulty(request.getDifficulty());
        question.setResponseType(responseType(request));
        question.setCategory(category);
    }

    private QuestionType responseType(QuestionRequest request) {
        return request.getResponseType() == null ? QuestionType.SCALE_1_5 : request.getResponseType();
    }

    private QuestionResponse toResponse(Question q) {
        Category category = q.getCategory();
        return QuestionResponse.builder()
                .id(q.getId())
                .text(q.getText())
                .orderNumber(q.getOrderNumber())
                .difficulty(q.getDifficulty())
                .responseType(q.getResponseType() == null ? QuestionType.SCALE_1_5 : q.getResponseType())
                .custom(q.isCustom())
                .defaultQuestion(!q.isCustom())
                .category(CategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .description(category.getDescription())
                        .custom(category.getOwner() != null)
                        .build())
                .build();
    }
}
