package com.example.skillora_platform.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "skillora.payment.ipn")
public record PaymentIpnProperties(
        boolean whitelistEnabled,
        List<String> vnpayAllowedIps,
        List<String> momoAllowedIps
) {

    public List<String> safeVnPayAllowedIps() {
        return sanitize(vnpayAllowedIps);
    }

    public List<String> safeMomoAllowedIps() {
        return sanitize(momoAllowedIps);
    }

    private List<String> sanitize(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .toList();
    }
}
