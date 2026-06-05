package com.example.skillora_platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "skillora.payment.vnpay")
public record VnPayProperties(
        String tmnCode,
        String hashSecret,
        String paymentUrl,
        String locale,
        String orderType
) {

    private static final String DEFAULT_PAYMENT_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String DEFAULT_LOCALE = "vn";
    private static final String DEFAULT_ORDER_TYPE = "other";

    public String resolvedPaymentUrl() {
        if (paymentUrl == null || paymentUrl.isBlank()) {
            return DEFAULT_PAYMENT_URL;
        }
        return paymentUrl;
    }

    public String resolvedLocale() {
        if (locale == null || locale.isBlank()) {
            return DEFAULT_LOCALE;
        }
        return locale;
    }

    public String resolvedOrderType() {
        if (orderType == null || orderType.isBlank()) {
            return DEFAULT_ORDER_TYPE;
        }
        return orderType;
    }

    public boolean configured() {
        return tmnCode != null && !tmnCode.isBlank()
                && hashSecret != null && !hashSecret.isBlank();
    }
}
