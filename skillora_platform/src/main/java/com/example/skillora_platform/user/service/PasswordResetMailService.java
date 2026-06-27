package com.example.skillora_platform.user.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.skillora_platform.config.PasswordResetProperties;
import com.example.skillora_platform.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetMailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final PasswordResetProperties passwordResetProperties;

    public void sendResetLink(User user, String rawToken, LocalDateTime expiresAt) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new IllegalStateException("Mail sender is not configured");
        }

        String resetUrl = passwordResetProperties.passwordResetUrl().replace("{token}", rawToken);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Reset your Skillora password");
        message.setText("""
                Hello %s,

                We received a request to reset your Skillora password.

                Reset your password here:
                %s

                This link expires at %s.
                If you did not request this, you can ignore this email.
                """.formatted(user.getFullName(), resetUrl, expiresAt));
        mailSender.send(message);
    }
}
