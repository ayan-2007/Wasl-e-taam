package com.waslataam.repository;

import com.waslataam.domain.User;
import com.waslataam.domain.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    void save(User user);
    Optional<User> findById(UUID id);
    List<User> findByRole(UserRole role);
    List<User> findAll();
    void delete(UUID id);
    void update(User user);
    Optional<User> findByEmail(String email);
}