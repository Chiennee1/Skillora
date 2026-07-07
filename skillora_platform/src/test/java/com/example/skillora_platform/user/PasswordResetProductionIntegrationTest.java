package com.example.skillora_platform.user;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.skillora_platform.user.entity.Role;
import com.example.skillora_platform.user.entity.RoleName;
import com.example.skillora_platform.user.entity.User;
import com.example.skillora_platform.user.entity.UserStatus;
import com.example.skillora_platform.user.repository.InstructorProfileRepository;
import com.example.skillora_platform.user.repository.PasswordResetTokenRepository;
import com.example.skillora_platform.user.repository.RefreshTokenRepository;
import com.example.skillora_platform.user.repository.RoleRepository;
import com.example.skillora_platform.user.repository.UserProfileRepository;
import com.example.skillora_platform.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "FRONTEND_URL=https://app.skillora.test",
                "JWT_SECRET=skillora-prod-test-secret-key-at-least-32-bytes-long",
                "DB_HOST=localhost",
                "DB_PASSWORD=test-password",
                "MAIL_HOST=smtp.skillora.test",
                "spring.datasource.url=jdbc:h2:mem:skillora_prod_reset;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
                "spring.flyway.enabled=false",
                "spring.cache.type=simple",
                "spring.data.redis.repositories.enabled=false",
                "management.health.redis.enabled=false",
                "management.health.mail.enabled=false",
                "management.endpoint.health.validate-group-membership=false",
                "management.endpoint.health.group.liveness.include=livenessState",
                "management.endpoint.health.group.readiness.include=readinessState,db",
                "skillora.security.jwt.secret=skillora-prod-test-secret-key-at-least-32-bytes-long",
                "skillora.auth.password-reset-url=https://app.skillora.test/reset-password?token={token}",
                "skillora.payment.enabled=false",
                "skillora.rate-limit.enabled=false"
        }
)
@ActiveProfiles("prod")
@AutoConfigureMockMvc
class PasswordResetProductionIntegrationTest {

    private static final String PASSWORD = "Password@123";

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private InstructorProfileRepository instructorProfileRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private PasswordResetTokenRepository passwordResetTokenRepository;

    @MockitoBean private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        passwordResetTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        instructorProfileRepository.deleteAll();
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldSendResetEmailAndNotExposeTokenOutsideDevOrTest() throws Exception {
        createUser("reset-prod@example.com", "Reset Prod User");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "reset-prod@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resetToken").doesNotExist())
                .andExpect(jsonPath("$.data.expiresAt").doesNotExist());

        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mailCaptor.capture());

        SimpleMailMessage message = mailCaptor.getValue();
        assertThat(message.getTo()).containsExactly("reset-prod@example.com");
        assertThat(message.getText())
                .contains("https://app.skillora.test/reset-password?token=")
                .doesNotContain("{token}");
        assertThat(passwordResetTokenRepository.count()).isEqualTo(1);
    }

    private void createUser(String email, String fullName) {
        Role role = roleRepository.findByName(RoleName.STUDENT).orElseThrow();
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(PASSWORD))
                .fullName(fullName)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        userRepository.save(user);
    }
}
