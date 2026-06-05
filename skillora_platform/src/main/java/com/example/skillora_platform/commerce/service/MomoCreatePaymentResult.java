package com.example.skillora_platform.commerce.service;

public record MomoCreatePaymentResult(
        String partnerCode,
        String requestId,
        String orderId,
        Long amount,
        Long responseTime,
        String message,
        Integer resultCode,
        String payUrl,
        String shortLink
) {
}
