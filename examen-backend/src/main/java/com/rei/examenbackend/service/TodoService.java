package com.rei.examenbackend.service;

import com.rei.examenbackend.dto.todo.TodoRequest;
import com.rei.examenbackend.dto.todo.TodoResponse;
import com.rei.examenbackend.model.User;

import java.util.List;

public interface TodoService {
    List<TodoResponse> list(User user);
    TodoResponse create(TodoRequest request, User user);
    TodoResponse update(Long id, TodoRequest request, User user);
    TodoResponse toggle(Long id, User user);
    void delete(Long id, User user);
}
