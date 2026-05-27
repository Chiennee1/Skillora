package com.example.skillora_platform.user.service;

import java.time.LocalDateTime;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.config.JwtProperties;
import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.user.entity.RefreshToken;
import com.example.skillora_platform.user.entity.User;
import com.example.skillora_platform.user.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureTokenService secureTokenService;
    private final JwtProperties jwtProperties;

    @Transactional
    public RefreshTokenIssue issue(User user, HttpServletRequest request) {
        String rawToken = secureTokenService.generateToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(secureTokenService.hashToken(rawToken))
                .expiresAt(LocalDateTime.now().plus(jwtProperties.refreshTokenTtl()))
                .createdByIp(clientIp(request))
                .userAgent(userAgent(request))
                .build();
        return new RefreshTokenIssue(rawToken, refreshTokenRepository.save(refreshToken));
    }

    @Transactional
    public RefreshTokenIssue rotate(String rawToken, HttpServletRequest request) {
        RefreshToken refreshToken = findValidToken(rawToken);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshToken.setRevokedByIp(clientIp(request));
        refreshTokenRepository.save(refreshToken);
        return issue(refreshToken.getUser(), request);
    }

    @Transactional
    public void revoke(String rawToken, HttpServletRequest request) {
        String tokenHash = secureTokenService.hashToken(rawToken);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .filter(token -> token.getRevokedAt() == null)
                .ifPresent(token -> {
                    token.setRevokedAt(LocalDateTime.now());
                    token.setRevokedByIp(clientIp(request));
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public void revokeActiveTokens(User user) {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.findByUserAndRevokedAtIsNull(user)
                .forEach(token -> {
                    token.setRevokedAt(now);
                    refreshTokenRepository.save(token);
                });
    }

    private RefreshToken findValidToken(String rawToken) {
        String tokenHash = secureTokenService.hashToken(rawToken);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> invalidRefreshToken());
        if (refreshToken.getRevokedAt() != null || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw invalidRefreshToken();
        }
        return refreshToken;
    }

    private BusinessException invalidRefreshToken() {
        return new BusinessException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader(FORWARDED_FOR_HEADER);
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String userAgent(HttpServletRequest request) {
        String userAgent = request.getHeader(USER_AGENT_HEADER);
        if (userAgent == null || userAgent.length() <= 500) {
            return userAgent;
        }
        return userAgent.substring(0, 500);
    }

    public record RefreshTokenIssue(String rawToken, RefreshToken refreshToken) {
    }
}
