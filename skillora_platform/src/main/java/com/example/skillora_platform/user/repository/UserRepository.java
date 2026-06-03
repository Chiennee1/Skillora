package com.example.skillora_platform.user.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.skillora_platform.user.entity.User;
import com.example.skillora_platform.user.entity.UserStatus;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByStatus(UserStatus status);

    long countByCreatedAtAfter(LocalDateTime dateTime);
}
