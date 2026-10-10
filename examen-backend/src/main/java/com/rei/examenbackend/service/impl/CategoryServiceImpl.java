package com.rei.examenbackend.service.impl;

import com.rei.examenbackend.dto.category.CategoryRequest;
import com.rei.examenbackend.dto.category.CategoryResponse;
import com.rei.examenbackend.exception.ApiException;
import com.rei.examenbackend.model.Category;
import com.rei.examenbackend.model.Role;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.repository.CategoryRepository;
import com.rei.examenbackend.repository.QuestionRepository;
import com.rei.examenbackend.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Categories are either part of the shared default library (no owner, managed by admins)
 * or private to the user who created them.
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, QuestionRepository questionRepository) {
        this.categoryRepository = categoryRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    public CategoryResponse create(CategoryRequest request, User user) {
        String name = normalizedName(request);
        if (categoryRepository.nameTakenFor(name, user.getId(), null)) {
            throw new ApiException(HttpStatus.CONFLICT, "Category name already exists");
        }

        Category category = Category.builder()
                .name(name)
                .description(trimToNull(request.getDescription()))
                .owner(user)
                .build();

        return toResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse update(Long id, CategoryRequest request, User user) {
        Category category = findEditable(id, user);
        String name = normalizedName(request);
        if (categoryRepository.nameTakenFor(name, user.getId(), category.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Category name already exists");
        }

        category.setName(name);
        category.setDescription(trimToNull(request.getDescription()));

        return toResponse(categoryRepository.save(category));
    }

    @Override
    public void delete(Long id, User user) {
        Category category = findEditable(id, user);
        if (questionRepository.existsByCategoryIdAndArchivedFalse(category.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Move or delete the questions in this category first");
        }
        if (!questionRepository.findByCategoryId(category.getId()).isEmpty()) {
            // Archived questions still hold historical answers; keep the category row for them.
            throw new ApiException(HttpStatus.CONFLICT, "This category holds archived questions with past answers");
        }
        categoryRepository.delete(category);
    }

    @Override
    public CategoryResponse getById(Long id, User user) {
        return toResponse(findVisible(id, user));
    }

    @Override
    public Page<CategoryResponse> getAll(Pageable pageable, User user) {
        return categoryRepository.findVisibleTo(user.getId(), pageable).map(this::toResponse);
    }

    private Category findVisible(Long id, User user) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found"));
        if (category.getOwner() != null && !category.isOwnedBy(user)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Category not found");
        }
        return category;
    }

    private Category findEditable(Long id, User user) {
        Category category = findVisible(id, user);
        boolean admin = user.getRole() == Role.ADMIN;
        if (!category.isOwnedBy(user) && !(admin && category.getOwner() == null)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Default categories cannot be changed");
        }
        return category;
    }

    private String normalizedName(CategoryRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Category name is required");
        }
        return request.getName().trim();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .custom(category.getOwner() != null)
                .build();
    }
}
