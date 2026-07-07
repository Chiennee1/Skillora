package com.example.skillora_platform.config;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 15)
@RequiredArgsConstructor
@Slf4j
public class PaymentIpnIpFilter extends OncePerRequestFilter {

    private final PaymentIpnProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Gateway gateway = gatewayFor(request);
        if (!properties.whitelistEnabled() || gateway == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = clientIp(request);
        List<String> allowedIps = gateway == Gateway.VNPAY
                ? properties.safeVnPayAllowedIps()
                : properties.safeMomoAllowedIps();
        if (isAllowed(clientIp, allowedIps)) {
            filterChain.doFilter(request, response);
            return;
        }

        log.warn("Blocked {} IPN request from non-whitelisted IP {} to {}", gateway, clientIp,
                request.getRequestURI());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),
                ApiResponse.error("Payment IPN source IP is not allowed"));
    }

    private Gateway gatewayFor(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if (("GET".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method))
                && path.equals(Constants.PAYMENT_API_PREFIX + "/vnpay/ipn")) {
            return Gateway.VNPAY;
        }
        if ("POST".equalsIgnoreCase(method)
                && path.equals(Constants.PAYMENT_API_PREFIX + "/momo/ipn")) {
            return Gateway.MOMO;
        }
        return null;
    }

    private boolean isAllowed(String clientIp, List<String> allowedIps) {
        if (clientIp == null || clientIp.isBlank() || allowedIps.isEmpty()) {
            return false;
        }
        return allowedIps.stream().anyMatch(rule -> matchesRule(clientIp, rule));
    }

    private boolean matchesRule(String clientIp, String rule) {
        if (rule.equals(clientIp)) {
            return true;
        }
        if (!rule.contains("/")) {
            return false;
        }
        try {
            String[] parts = rule.split("/", 2);
            int prefix = Integer.parseInt(parts[1]);
            if (prefix < 0 || prefix > 32) {
                return false;
            }
            long ip = ipv4ToLong(clientIp);
            long network = ipv4ToLong(parts[0]);
            long mask = prefix == 0 ? 0L : (0xffffffffL << (32 - prefix)) & 0xffffffffL;
            return (ip & mask) == (network & mask);
        } catch (IllegalArgumentException ex) {
            log.warn("Ignoring invalid payment IP whitelist rule: {}", rule);
            return false;
        }
    }

    private long ipv4ToLong(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Only IPv4 CIDR rules are supported");
        }
        long value = 0;
        for (String part : parts) {
            int octet = Integer.parseInt(part);
            if (octet < 0 || octet > 255) {
                throw new IllegalArgumentException("Invalid IPv4 octet");
            }
            value = (value << 8) + octet;
        }
        return value;
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private enum Gateway {
        VNPAY,
        MOMO
    }
}
