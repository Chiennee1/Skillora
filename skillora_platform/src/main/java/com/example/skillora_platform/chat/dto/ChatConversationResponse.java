package com.example.skillora_platform.chat.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatConversationResponse {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
