package com.rei.examenbackend.service;

import com.rei.examenbackend.dto.journal.JournalEntryRequest;
import com.rei.examenbackend.dto.journal.JournalEntryResponse;
import com.rei.examenbackend.model.User;

import java.util.List;

public interface JournalService {
    JournalEntryResponse create(JournalEntryRequest request, User user);
    List<JournalEntryResponse> getRecent(User user);
}
