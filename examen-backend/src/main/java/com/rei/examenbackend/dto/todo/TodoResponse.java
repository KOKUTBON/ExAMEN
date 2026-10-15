package com.rei.examenbackend.dto.todo;

import java.time.LocalDateTime;

public class TodoResponse {
    private Long id;
    private String title;
    private boolean completed;
    private LocalDateTime dueAt;

    public TodoResponse() {}

    public TodoResponse(Long id, String title, boolean completed, LocalDateTime dueAt) {
        this.id = id;
        this.title = title;
        this.completed = completed;
        this.dueAt = dueAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public boolean isCompleted() { return completed; }
    public LocalDateTime getDueAt() { return dueAt; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }
}
