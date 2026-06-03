package com.example.skillora_platform.chat.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.chat.entity.ChatConversation;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    @EntityGraph(attributePaths = {"course"})
    Page<ChatConversation> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "course"})
    Optional<ChatConversation> findByIdAndUserId(Long id, Long userId);
}
