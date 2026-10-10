package com.rei.examenbackend.controller;

import com.rei.examenbackend.dto.answer.AnswerRequest;
import com.rei.examenbackend.dto.answer.AnswerResponse;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.service.AnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @PostMapping
    public ResponseEntity<AnswerResponse> create(@Valid @RequestBody AnswerRequest request,
                                                 @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(answerService.create(request, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnswerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AnswerRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(answerService.update(id, request, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnswerResponse> getById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(answerService.getById(id, user));
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<AnswerResponse>> getByQuestion(@PathVariable Long questionId,
                                                              @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(answerService.getByQuestion(questionId, user));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<AnswerResponse>> getBySession(@PathVariable Long sessionId,
                                                             @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(answerService.getBySession(sessionId, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        answerService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
