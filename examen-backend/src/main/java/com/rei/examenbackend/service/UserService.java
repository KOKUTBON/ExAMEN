package com.rei.examenbackend.service;

import com.rei.examenbackend.model.User;

import java.util.List;

public interface UserService {
    User createUser(User user);
    User getUserById(Long id);
    User getUserByEmail(String email);
    List<User> getAllUsers();

    void deleteUser(Long id);
}
