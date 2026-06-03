package com.example.skillora_platform.notification.event;

public record NotificationCreatedEvent(
        Long notificationId,
        Long userId
) {
}
