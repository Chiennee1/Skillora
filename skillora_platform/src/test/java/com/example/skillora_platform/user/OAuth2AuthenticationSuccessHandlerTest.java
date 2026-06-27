package com.example.skillora_platform.user;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;

import com.example.skillora_platform.admin.repository.AuditLogRepository;
import com.example.skillora_platform.notification.repository.NotificationRepository;
import com.example.skillora_platform.user.repository.InstructorProfileRepository;
import com.example.skillora_platform.user.repository.PasswordResetTokenRepository;
import com.example.skillora_platform.user.repository.RefreshTokenRepository;
import com.example.skillora_platform.user.repository.UserProfileRepository;
import com.example.skillora_platform.user.repository.UserRepository;
import com.example.skillora_platform.user.service.OAuth2AuthenticationSuccessHandler;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OAuth2AuthenticationSuccessHandlerTest {

    @Autowired
    private OAuth2AuthenticationSuccessHandler successHandler;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private InstructorProfileRepository instructorProfileRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        notificationRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        instructorProfileRepository.deleteAll();
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateGoogleUserAndRedirectWithJwtTokens() throws Exception {
        OAuth2User principal = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT")),
                Map.of(
                        "email", "google@example.com",
                        "email_verified", true,
                        "name", "Google User",
                        "picture", "https://example.com/avatar.png"
                ),
                "email"
        );
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                principal,
                principal.getAuthorities(),
                "google"
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl())
                .startsWith("http://localhost:5173/oauth2/redirect?accessToken=")
                .contains("refreshToken=")
                .contains("tokenType=Bearer")
                .contains("expiresIn=900");
        assertThat(userRepository.findByEmailIgnoreCase("google@example.com"))
                .hasValueSatisfying(user -> {
                    assertThat(user.getFullName()).isEqualTo("Google User");
                    assertThat(user.isEmailVerified()).isTrue();
                });
    }
}
