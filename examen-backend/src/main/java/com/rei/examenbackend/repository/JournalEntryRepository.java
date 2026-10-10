package com.rei.examenbackend.repository;

import com.rei.examenbackend.model.JournalEntry;
import com.rei.examenbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    List<JournalEntry> findTop20ByUserOrderByCreatedAtDesc(User user);

    List<JournalEntry> findByUserOrderByCreatedAtDesc(User user);

    void deleteByUser(User user);
}
