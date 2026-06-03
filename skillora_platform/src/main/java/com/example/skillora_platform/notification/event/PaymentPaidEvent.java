package com.example.skillora_platform.notification.event;

public record PaymentPaidEvent(
        Long orderId,
        Long paymentTransactionId
) {
}
