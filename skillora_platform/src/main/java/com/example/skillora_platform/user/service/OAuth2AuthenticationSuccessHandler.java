package com.example.skillora_platform.user.service;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.skillora_platform.config.AppOAuth2Properties;
import com.example.skillora_platform.user.dto.AuthResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final SocialAuthService socialAuthService;
    private final AppOAuth2Properties appOAuth2Properties;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        AuthResponse authResponse = socialAuthService.authenticateGoogle(principal, request);
        String redirectUrl = UriComponentsBuilder.fromUriString(appOAuth2Properties.authorizedRedirectUri())
                .queryParam("accessToken", authResponse.getAccessToken())
                .queryParam("refreshToken", authResponse.getRefreshToken())
                .queryParam("tokenType", authResponse.getTokenType())
                .queryParam("expiresIn", authResponse.getExpiresIn())
                .build()
                .encode()
                .toUriString();
        response.sendRedirect(redirectUrl);
    }
}
