package com.example.skillora_platform.notification.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.skillora_platform.notification.dto.NotificationResponse;
import com.example.skillora_platform.notification.event.NotificationCreatedEvent;
import com.example.skillora_platform.notification.service.NotificationService;
import com.example.skillora_platform.notification.service.NotificationStreamService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationStreamListener {

    private final NotificationService notificationService;
    private final NotificationStreamService streamService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationCreated(NotificationCreatedEvent event) {
        NotificationResponse response = notificationService.getResponseForStream(event.notificationId());
        streamService.send(event.userId(), response);
    }
}
