package com.rei.examenbackend.controller;

import com.rei.examenbackend.dto.todo.TodoRequest;
import com.rei.examenbackend.dto.todo.TodoResponse;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @GetMapping
    public ResponseEntity<List<TodoResponse>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(todoService.list(user));
    }

    @PostMapping
    public ResponseEntity<TodoResponse> create(@Valid @RequestBody TodoRequest request,
                                               @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(todoService.create(request, user));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TodoResponse> update(@PathVariable Long id,
                                               @RequestBody TodoRequest request,
                                               @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(todoService.update(id, request, user));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<TodoResponse> toggle(@PathVariable Long id,
                                               @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(todoService.toggle(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal User user) {
        todoService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
