package com.example.skillora_platform.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.user.entity.User;
import com.example.skillora_platform.user.entity.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUser(User user);

    Optional<UserProfile> findByUserId(Long userId);
}
