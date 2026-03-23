package com.project.cbsbackend.repository;

import com.project.cbsbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Check if email already exists (used in registration)
    boolean existsByEmail(String email);

    // Fetch user by email (used in login)
    Optional<User> findByEmail(String email);
}