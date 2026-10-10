package com.rei.examenbackend.service;

import com.rei.examenbackend.dto.category.CategoryRequest;
import com.rei.examenbackend.dto.category.CategoryResponse;
import com.rei.examenbackend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    CategoryResponse create(CategoryRequest request, User user);

    CategoryResponse update(Long id, CategoryRequest request, User user);

    void delete(Long id, User user);

    CategoryResponse getById(Long id, User user);

    Page<CategoryResponse> getAll(Pageable pageable, User user);
}
