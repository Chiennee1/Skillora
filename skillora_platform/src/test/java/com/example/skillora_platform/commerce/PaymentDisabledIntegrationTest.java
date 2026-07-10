package com.example.skillora_platform.commerce;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.example.skillora_platform.commerce.repository.PaymentTransactionRepository;
import com.example.skillora_platform.user.entity.Role;
import com.example.skillora_platform.user.entity.RoleName;
import com.example.skillora_platform.user.entity.User;
import com.example.skillora_platform.user.entity.UserStatus;
import com.example.skillora_platform.user.repository.RoleRepository;
import com.example.skillora_platform.user.repository.UserRepository;
import com.example.skillora_platform.user.service.JwtService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "skillora.payment.enabled=false",
        "skillora.payment.vnpay.tmn-code=TESTTMN",
        "skillora.payment.vnpay.hash-secret=test-vnpay-secret",
        "skillora.payment.momo.partner-code=MOMO",
        "skillora.payment.momo.access-key=test-access",
        "skillora.payment.momo.secret-key=test-momo-secret"
})
class PaymentDisabledIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PaymentTransactionRepository paymentTransactionRepository;

    private String studentToken;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        studentToken = token(createUser("payment.disabled@example.com", "Payment Disabled User"));
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void shouldRejectGatewayCreateWhenPaymentIsDisabledWithoutCreatingTransactions() throws Exception {
        mockMvc.perform(post("/api/v1/payments/vnpay/create")
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "orderId": 999 }
                                """))
                .andExpect(status().isServiceUnavailable());

        mockMvc.perform(post("/api/v1/payments/momo/create")
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "orderId": 999 }
                                """))
                .andExpect(status().isServiceUnavailable());

        assertThat(paymentTransactionRepository.count()).isZero();
    }

    private User createUser(String email, String fullName) {
        Role role = roleRepository.findByName(RoleName.STUDENT).orElseThrow();
        return userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("Password@123"))
                .fullName(fullName)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .roles(new HashSet<>(Set.of(role)))
                .build());
    }

    private String token(User user) {
        return jwtService.generateAccessToken(user);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private void cleanDatabase() {
        paymentTransactionRepository.deleteAll();
        userRepository.deleteAll();
    }
}
