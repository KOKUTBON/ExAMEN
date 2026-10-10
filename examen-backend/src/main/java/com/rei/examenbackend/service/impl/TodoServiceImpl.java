package com.rei.examenbackend.service.impl;

import com.rei.examenbackend.dto.todo.TodoRequest;
import com.rei.examenbackend.dto.todo.TodoResponse;
import com.rei.examenbackend.exception.ApiException;
import com.rei.examenbackend.model.ToDoItem;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.repository.ToDoItemRepository;
import com.rei.examenbackend.repository.UserRepository;
import com.rei.examenbackend.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final ToDoItemRepository todoRepo;
    private final UserRepository userRepo;

    @Override
    public List<TodoResponse> list(User user) {
        User persisted = userRepo.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return todoRepo.findByUser(persisted).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public TodoResponse create(TodoRequest request, User user) {
        User persisted = userRepo.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        ToDoItem item = new ToDoItem();
        item.setUser(persisted);
        item.setTitle(request.getTitle());
        item.setCompleted(false);
        item.setDueAt(request.getDueAt());
        item.setUpdatedAt(LocalDateTime.now());
        todoRepo.save(item);
        return toResponse(item);
    }

    @Override
    public TodoResponse update(Long id, TodoRequest request, User user) {
        ToDoItem item = getOwned(id, user);
        if (request.getTitle() != null) {
            item.setTitle(request.getTitle());
        }
        if (request.getDueAt() != null) {
            item.setDueAt(request.getDueAt());
        }
        item.setUpdatedAt(LocalDateTime.now());
        todoRepo.save(item);
        return toResponse(item);
    }

    @Override
    public TodoResponse toggle(Long id, User user) {
        ToDoItem item = getOwned(id, user);
        item.setCompleted(!item.isCompleted());
        item.setUpdatedAt(LocalDateTime.now());
        todoRepo.save(item);
        return toResponse(item);
    }

    @Override
    public void delete(Long id, User user) {
        ToDoItem item = getOwned(id, user);
        todoRepo.delete(item);
    }

    private ToDoItem getOwned(Long id, User user) {
        User persisted = userRepo.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        ToDoItem item = todoRepo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Todo not found"));
        if (!item.getUser().getId().equals(persisted.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not your todo");
        }
        return item;
    }

    private TodoResponse toResponse(ToDoItem item) {
        return new TodoResponse(
                item.getId(),
                item.getTitle(),
                item.isCompleted(),
                item.getDueAt()
        );
    }
}
