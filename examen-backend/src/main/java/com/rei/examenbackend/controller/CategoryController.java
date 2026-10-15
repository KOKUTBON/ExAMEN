package com.rei.examenbackend.controller;

import com.rei.examenbackend.dto.category.CategoryRequest;
import com.rei.examenbackend.dto.category.CategoryResponse;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request,
                                                   @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(categoryService.create(request, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(categoryService.update(id, request, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(categoryService.getById(id, user));
    }

    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getAll(Pageable pageable, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(categoryService.getAll(pageable, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        categoryService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
