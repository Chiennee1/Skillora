package com.example.skillora_platform.commerce.service;

public record MomoCreatePaymentPayload(
        String partnerCode,
        String storeName,
        String storeId,
        String requestId,
        Long amount,
        String orderId,
        String orderInfo,
        String redirectUrl,
        String ipnUrl,
        String requestType,
        String extraData,
        String lang,
        String signature
) {
}
