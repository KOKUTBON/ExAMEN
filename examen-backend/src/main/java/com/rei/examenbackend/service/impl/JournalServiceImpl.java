package com.rei.examenbackend.service.impl;

import com.rei.examenbackend.dto.journal.JournalEntryRequest;
import com.rei.examenbackend.dto.journal.JournalEntryResponse;
import com.rei.examenbackend.model.JournalEntry;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.repository.JournalEntryRepository;
import com.rei.examenbackend.repository.UserRepository;
import com.rei.examenbackend.service.JournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.rei.examenbackend.exception.ApiException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JournalServiceImpl implements JournalService {

    private final JournalEntryRepository journalEntryRepository;
    private final UserRepository userRepository;

    @Override
    public JournalEntryResponse create(JournalEntryRequest request, User user) {
        User persisted = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        JournalEntry entry = JournalEntry.builder()
                .user(persisted)
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();
        journalEntryRepository.save(entry);
        return toResponse(entry);
    }

    @Override
    public List<JournalEntryResponse> getRecent(User user) {
        User persisted = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        return journalEntryRepository.findTop20ByUserOrderByCreatedAtDesc(persisted)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private JournalEntryResponse toResponse(JournalEntry entry) {
        return JournalEntryResponse.builder()
                .id(entry.getId())
                .content(entry.getContent())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
