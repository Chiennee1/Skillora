package com.example.skillora_platform.notification.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.example.skillora_platform.notification.entity.NotificationType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {

    private Long id;
    private Long userId;
    private NotificationType type;
    private String title;
    private String content;
    private JsonNode data;
    private boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
