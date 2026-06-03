package com.example.skillora_platform.chat.service;

import com.example.skillora_platform.chat.entity.ChatRole;

public record GeminiMessage(
        ChatRole role,
        String content
) {
}
