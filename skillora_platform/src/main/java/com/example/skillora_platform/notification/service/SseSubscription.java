package com.example.skillora_platform.notification.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public record SseSubscription(
        Long userId,
        SseEmitter emitter
) {
}
