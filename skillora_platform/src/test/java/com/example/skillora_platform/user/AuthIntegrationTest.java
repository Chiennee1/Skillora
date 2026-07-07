package com.example.skillora_platform.user;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.skillora_platform.admin.repository.AuditLogRepository;
import com.example.skillora_platform.notification.repository.NotificationRepository;
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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthIntegrationTest {

    private static final String PASSWORD = "Password@123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

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

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    void testBcrypt() {
        System.out.println("--- BCRYPT TEST START ---");
        String hash = "$2b$10$1aQv5v5DF6/2cWolDDpYyuS2s0aHJU.CWy28yBDDaltVpLGG2W1nC";
        System.out.println("Encoded Password@123: " + passwordEncoder.encode("Password@123"));
        System.out.println("Encoded Skillora@12345: " + passwordEncoder.encode("Skillora@12345"));
        
        String[] candidates = {
            "Password@123", "Skillora@12345", "admin", "admin@123", "password", "Password", "123456", 
            "admin123", "Skillora@123", "Password@12345", "skillora", "Skillora", "password123", "12345678"
        };
        for (String candidate : candidates) {
            if (passwordEncoder.matches(candidate, hash)) {
                System.out.println(">>> MATCH FOUND: " + candidate);
            }
        }
        System.out.println("--- BCRYPT TEST END ---");
    }

    @Test
    void shouldRegisterStudent() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("student@example.com", "Student User", "STUDENT")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.data.refreshToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.data.user.email").value("student@example.com"))
                .andExpect(jsonPath("$.data.user.roles", hasItem("STUDENT")));
    }

    @Test
    void shouldRegisterInstructorAndCreateInstructorProfile() throws Exception {
        JsonNode response = postJson("/api/v1/auth/register",
                """
                {
                    "email": "instructor@example.com",
                    "password": "Password@123",
                    "fullName": "Instructor User",
                    "role": "INSTRUCTOR",
                    "instructorTitle": "Java Instructor",
                    "instructorExpertise": "Java, Spring Boot"
                }
                """,
                status().isCreated());

        Long userId = response.at("/data/user/id").asLong();
        mockMvc.perform(get("/api/v1/instructors/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Java Instructor"))
                .andExpect(jsonPath("$.data.expertise").value("Java, Spring Boot"));
    }

    @Test
    void shouldRejectDuplicateEmailAndAdminRegistration() throws Exception {
        postJson("/api/v1/auth/register", registerJson("duplicate@example.com", "First User", "STUDENT"),
                status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("duplicate@example.com", "Second User", "STUDENT")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already registered"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("admin@example.com", "Admin User", "ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ADMIN registration is not allowed"));
    }

    @Test
    void shouldLoginAndRejectInvalidCredentialsOrBannedUser() throws Exception {
        postJson("/api/v1/auth/register", registerJson("login@example.com", "Login User", "STUDENT"),
                status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("login@example.com", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", not(blankOrNullString())));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("login@example.com", "WrongPassword")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        createUser("banned@example.com", UserStatus.BANNED);
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("banned@example.com", PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Account is not active"));
    }

    @Test
    void shouldRequireJwtForMeAndReturnCurrentUser() throws Exception {
        JsonNode response = postJson("/api/v1/auth/register",
                registerJson("me@example.com", "Me User", "STUDENT"), status().isCreated());

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", bearer(response.at("/data/accessToken").asText())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("me@example.com"));
    }

    @Test
    void shouldRotateRefreshTokenAndRejectOldToken() throws Exception {
        JsonNode registerResponse = postJson("/api/v1/auth/register",
                registerJson("refresh@example.com", "Refresh User", "STUDENT"), status().isCreated());
        String firstRefreshToken = registerResponse.at("/data/refreshToken").asText();

        JsonNode refreshResponse = postJson("/api/v1/auth/refresh",
                """
                {
                    "refreshToken": "%s"
                }
                """.formatted(firstRefreshToken),
                status().isOk());
        String secondRefreshToken = refreshResponse.at("/data/refreshToken").asText();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "refreshToken": "%s"
                                }
                                """.formatted(firstRefreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "refreshToken": "%s"
                                }
                                """.formatted(secondRefreshToken)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldLogoutIdempotentlyAndRevokeRefreshToken() throws Exception {
        JsonNode registerResponse = postJson("/api/v1/auth/register",
                registerJson("logout@example.com", "Logout User", "STUDENT"), status().isCreated());
        String refreshToken = registerResponse.at("/data/refreshToken").asText();
        String body = """
                {
                    "refreshToken": "%s"
                }
                """.formatted(refreshToken);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldIssueDevResetTokenAndResetPassword() throws Exception {
        JsonNode registerResponse = postJson("/api/v1/auth/register", registerJson("reset@example.com", "Reset User", "STUDENT"),
                status().isCreated());
        String oldRefreshToken = registerResponse.at("/data/refreshToken").asText();

        JsonNode forgotResponse = postJson("/api/v1/auth/forgot-password",
                """
                {
                    "email": "reset@example.com"
                }
                """,
                status().isOk());
        String resetToken = forgotResponse.at("/data/resetToken").asText();

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "token": "%s",
                                    "password": "NewPassword@123"
                                }
                                """.formatted(resetToken)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "refreshToken": "%s"
                                }
                                """.formatted(oldRefreshToken)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("reset@example.com", PASSWORD)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("reset@example.com", "NewPassword@123")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetAndUpdateMyProfile() throws Exception {
        JsonNode registerResponse = postJson("/api/v1/auth/register",
                registerJson("profile@example.com", "Profile User", "STUDENT"), status().isCreated());
        String accessToken = registerResponse.at("/data/accessToken").asText();

        mockMvc.perform(get("/api/v1/profiles/me")
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("profile@example.com"));

        mockMvc.perform(put("/api/v1/profiles/me")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fullName": "Updated Profile",
                                    "phone": "0900000000",
                                    "headline": "Backend Learner",
                                    "bio": "Learning Spring Boot",
                                    "website": "https://example.com",
                                    "location": "Ha Noi"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Updated Profile"))
                .andExpect(jsonPath("$.data.phone").value("0900000000"))
                .andExpect(jsonPath("$.data.headline").value("Backend Learner"));
    }

    private JsonNode postJson(String path, String json, ResultMatcher expectedStatus) throws Exception {
        String response = mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(expectedStatus)
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private String registerJson(String email, String fullName, String role) {
        return """
                {
                    "email": "%s",
                    "password": "%s",
                    "fullName": "%s",
                    "role": "%s"
                }
                """.formatted(email, PASSWORD, fullName, role);
    }

    private String loginJson(String email, String password) {
        return """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private void createUser(String email, UserStatus status) {
        Role role = roleRepository.findByName(RoleName.STUDENT).orElseThrow();
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(PASSWORD))
                .fullName("Test User")
                .status(status)
                .emailVerified(true)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        userRepository.save(user);
    }
}
