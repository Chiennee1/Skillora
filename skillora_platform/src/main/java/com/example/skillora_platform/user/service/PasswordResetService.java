package com.example.skillora_platform.user.service;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.user.dto.PasswordResetTokenResponse;
import com.example.skillora_platform.user.entity.PasswordResetToken;
import com.example.skillora_platform.user.entity.User;
import com.example.skillora_platform.user.entity.UserStatus;
import com.example.skillora_platform.user.repository.PasswordResetTokenRepository;
import com.example.skillora_platform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final long RESET_TOKEN_TTL_HOURS = 1;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final SecureTokenService secureTokenService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final Environment environment;

    @Transactional
    public PasswordResetTokenResponse requestReset(String email) {
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .filter(this::canResetPassword)
                .map(this::createResetTokenResponse)
                .orElseGet(() -> PasswordResetTokenResponse.builder().build());
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken resetToken = findValidToken(rawToken);
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        resetToken.setUsedAt(LocalDateTime.now());
        userRepository.save(user);
        passwordResetTokenRepository.save(resetToken);
        refreshTokenService.revokeActiveTokens(user);
    }

    private PasswordResetTokenResponse createResetTokenResponse(User user) {
        String rawToken = secureTokenService.generateToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(RESET_TOKEN_TTL_HOURS);
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash(secureTokenService.hashToken(rawToken))
                .expiresAt(expiresAt)
                .build();
        passwordResetTokenRepository.save(resetToken);
        return PasswordResetTokenResponse.builder()
                .resetToken(shouldExposeResetToken() ? rawToken : null)
                .expiresAt(shouldExposeResetToken() ? expiresAt : null)
                .build();
    }

    private PasswordResetToken findValidToken(String rawToken) {
        String tokenHash = secureTokenService.hashToken(rawToken);
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> invalidResetToken());
        if (resetToken.getUsedAt() != null || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw invalidResetToken();
        }
        return resetToken;
    }

    private boolean canResetPassword(User user) {
        return user.getStatus() == UserStatus.ACTIVE && user.getDeletedAt() == null;
    }

    private boolean shouldExposeResetToken() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equals("dev") || profile.equals("test"));
    }

    private BusinessException invalidResetToken() {
        return new BusinessException("Invalid password reset token", HttpStatus.UNAUTHORIZED);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
