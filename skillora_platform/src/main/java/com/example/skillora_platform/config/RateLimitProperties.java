package com.example.skillora_platform.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "skillora.rate-limit")
public record RateLimitProperties(
        boolean enabled,
        Duration window,
        int authLimit,
        int resetPasswordLimit,
        int chatLimit,
        int paymentLimit
) {

    public Duration resolvedWindow() {
        if (window == null || window.isNegative() || window.isZero()) {
            return Duration.ofMinutes(1);
        }
        return window;
    }

    public int safeAuthLimit() {
        return authLimit <= 0 ? 30 : authLimit;
    }

    public int safeResetPasswordLimit() {
        return resetPasswordLimit <= 0 ? 5 : resetPasswordLimit;
    }

    public int safeChatLimit() {
        return chatLimit <= 0 ? 30 : chatLimit;
    }

    public int safePaymentLimit() {
        return paymentLimit <= 0 ? 120 : paymentLimit;
    }
}
