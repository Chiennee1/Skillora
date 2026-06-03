package com.example.skillora_platform.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatResponse {

    private Long conversationId;
    private String conversationTitle;
    private ChatMessageResponse userMessage;
    private ChatMessageResponse assistantMessage;
}
