package com.example.skillora_platform.notification.event;

public record PaymentFailedEvent(
        Long orderId,
        Long paymentTransactionId,
        String reason
) {
}
