package com.rei.examenbackend.repository;

import com.rei.examenbackend.model.ToDoItem;
import com.rei.examenbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToDoItemRepository extends JpaRepository<ToDoItem, Long> {
    List<ToDoItem> findByUser(User user);

    List<ToDoItem> findByUserOrderByDueAtAsc(User user);

    long countByUserAndCompletedTrue(User user);

    void deleteByUser(User user);
}
