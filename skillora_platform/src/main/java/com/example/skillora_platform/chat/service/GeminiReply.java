package com.example.skillora_platform.chat.service;

public record GeminiReply(
        String text,
        String model,
        Integer tokensUsed
) {
}
