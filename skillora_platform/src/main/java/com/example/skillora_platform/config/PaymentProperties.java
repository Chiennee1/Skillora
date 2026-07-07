package com.example.skillora_platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "skillora.payment")
public record PaymentProperties(
        boolean enabled,
        String publicBaseUrl,
        String resultUrl
) {

    private static final String DEFAULT_PUBLIC_BASE_URL = "http://localhost:8080";
    private static final String DEFAULT_RESULT_URL = "http://localhost:3000/payment/result";

    public String resolvedPublicBaseUrl() {
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            return DEFAULT_PUBLIC_BASE_URL;
        }
        return trimTrailingSlash(publicBaseUrl);
    }

    public String resolvedResultUrl() {
        if (resultUrl == null || resultUrl.isBlank()) {
            return DEFAULT_RESULT_URL;
        }
        return resultUrl;
    }

    private String trimTrailingSlash(String value) {
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
