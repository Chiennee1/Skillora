package com.example.skillora_platform.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.user.entity.InstructorProfile;
import com.example.skillora_platform.user.entity.User;

public interface InstructorProfileRepository extends JpaRepository<InstructorProfile, Long> {

    Optional<InstructorProfile> findByUser(User user);

    Optional<InstructorProfile> findByUserId(Long userId);
}
