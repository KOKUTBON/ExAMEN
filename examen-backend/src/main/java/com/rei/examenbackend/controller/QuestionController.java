package com.rei.examenbackend.controller;

import com.rei.examenbackend.dto.question.QuestionRequest;
import com.rei.examenbackend.dto.question.QuestionResponse;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuestionResponse> create(@Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(questionService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuestionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest request
    ) {
        return ResponseEntity.ok(questionService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(questionService.getById(id, user));
    }

    @GetMapping
    public ResponseEntity<Page<QuestionResponse>> getAll(Pageable pageable, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(questionService.getAll(pageable, user));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<QuestionResponse>> getByCategory(@PathVariable Long categoryId,
                                                                @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(questionService.getByCategory(categoryId, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/custom")
    public ResponseEntity<QuestionResponse> createCustom(
            @Valid @RequestBody QuestionRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(questionService.createCustom(request, user));
    }

    @PutMapping("/custom/{id}")
    public ResponseEntity<QuestionResponse> updateCustom(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(questionService.updateMine(id, request, user));
    }

    @GetMapping("/my")
    public ResponseEntity<List<QuestionResponse>> getMine(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(questionService.getMine(user));
    }

    @DeleteMapping("/custom/{id}")
    public ResponseEntity<Void> deleteMine(@PathVariable Long id, @AuthenticationPrincipal User user) {
        questionService.deleteMine(id, user);
        return ResponseEntity.noContent().build();
    }
}
