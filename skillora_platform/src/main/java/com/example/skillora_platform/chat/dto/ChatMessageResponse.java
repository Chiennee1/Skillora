package com.example.skillora_platform.chat.dto;

import java.time.LocalDateTime;

import com.example.skillora_platform.chat.entity.ChatRole;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageResponse {

    private Long id;
    private Long conversationId;
    private ChatRole role;
    private String content;
    private String model;
    private Integer tokensUsed;
    private LocalDateTime createdAt;
}
