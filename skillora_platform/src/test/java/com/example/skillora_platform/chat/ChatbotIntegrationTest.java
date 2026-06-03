package com.example.skillora_platform.chat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.skillora_platform.chat.repository.ChatConversationRepository;
import com.example.skillora_platform.chat.repository.ChatMessageRepository;
import com.example.skillora_platform.chat.service.GeminiClient;
import com.example.skillora_platform.chat.service.GeminiReply;
import com.example.skillora_platform.course.entity.Course;
import com.example.skillora_platform.course.entity.CourseLevel;
import com.example.skillora_platform.course.entity.CourseStatus;
import com.example.skillora_platform.course.repository.CourseRepository;
import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.notification.repository.NotificationRepository;
import com.example.skillora_platform.user.entity.Role;
import com.example.skillora_platform.user.entity.RoleName;
import com.example.skillora_platform.user.entity.User;
import com.example.skillora_platform.user.entity.UserStatus;
import com.example.skillora_platform.user.repository.RoleRepository;
import com.example.skillora_platform.user.repository.UserRepository;
import com.example.skillora_platform.user.service.JwtService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ChatbotIntegrationTest {

    private static final String PASSWORD = "Password@123";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private ChatConversationRepository conversationRepository;
    @Autowired private ChatMessageRepository messageRepository;
    @Autowired private NotificationRepository notificationRepository;

    @MockitoBean private GeminiClient geminiClient;

    private User instructor;
    private User student;
    private User otherStudent;
    private String instructorToken;
    private String studentToken;
    private String otherStudentToken;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        instructor = createUser("chat.instructor@example.com", "Chat Instructor", RoleName.INSTRUCTOR);
        student = createUser("chat.student@example.com", "Chat Student", RoleName.STUDENT);
        otherStudent = createUser("chat.other@example.com", "Other Student", RoleName.STUDENT);
        instructorToken = token(instructor);
        studentToken = token(student);
        otherStudentToken = token(otherStudent);
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void shouldCreateConversationAndStoreUserAndAssistantMessages() throws Exception {
        Course course = createCourse("Spring Chat Course", CourseStatus.PUBLISHED);
        when(geminiClient.generate(anyString(), anyList()))
                .thenReturn(new GeminiReply("Bạn nên bắt đầu từ security filter chain.", "gemini-2.5-flash", 42));

        JsonNode response = postAsk("""
                {
                    "courseId": %d,
                    "message": "JWT trong Spring Security hoạt động thế nào?"
                }
                """.formatted(course.getId()), studentToken, status().isCreated());

        Long conversationId = response.at("/data/conversationId").asLong();
        assertThat(conversationRepository.count()).isEqualTo(1);
        assertThat(messageRepository.findByConversationIdOrderByCreatedAtAsc(
                conversationId, org.springframework.data.domain.PageRequest.of(0, 10)).getContent()).hasSize(2);

        mockMvc.perform(get("/api/v1/chat/conversations")
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(conversationId));
    }

    @Test
    void shouldContinueOwnedConversationAndDenyOtherUserMessages() throws Exception {
        when(geminiClient.generate(anyString(), anyList()))
                .thenReturn(new GeminiReply("Câu trả lời đầu tiên", "gemini-2.5-flash", 20))
                .thenReturn(new GeminiReply("Câu trả lời tiếp theo", "gemini-2.5-flash", 30));

        JsonNode first = postAsk("""
                { "message": "Tôi nên học gì trước?" }
                """, studentToken, status().isCreated());
        Long conversationId = first.at("/data/conversationId").asLong();

        postAsk("""
                {
                    "conversationId": %d,
                    "message": "Giải thích kỹ hơn."
                }
                """.formatted(conversationId), studentToken, status().isCreated());

        mockMvc.perform(get("/api/v1/chat/conversations/{id}/messages", conversationId)
                        .header("Authorization", bearer(otherStudentToken)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/chat/conversations/{id}/messages", conversationId)
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(4)));
    }

    @Test
    void shouldRejectDraftCourseForStudentButAllowOwner() throws Exception {
        Course draft = createCourse("Draft AI Course", CourseStatus.DRAFT);
        when(geminiClient.generate(anyString(), anyList()))
                .thenReturn(new GeminiReply("Owner can inspect draft context.", "gemini-2.5-flash", 15));

        postAsk("""
                {
                    "courseId": %d,
                    "message": "Tóm tắt khóa học này."
                }
                """.formatted(draft.getId()), studentToken, status().isForbidden());

        postAsk("""
                {
                    "courseId": %d,
                    "message": "Tóm tắt khóa học này."
                }
                """.formatted(draft.getId()), instructorToken, status().isCreated());
    }

    @Test
    void shouldRevalidateExistingConversationCourseBeforeCallingGemini() throws Exception {
        Course archivedCourse = createCourse("Archived Conversation Course", CourseStatus.PUBLISHED);
        Course deletedCourse = createCourse("Deleted Conversation Course", CourseStatus.PUBLISHED);
        when(geminiClient.generate(anyString(), anyList()))
                .thenReturn(new GeminiReply("First response", "gemini-2.5-flash", 10))
                .thenReturn(new GeminiReply("Second response", "gemini-2.5-flash", 10));

        Long archivedConversationId = postAsk("""
                {
                    "courseId": %d,
                    "message": "Start archived conversation"
                }
                """.formatted(archivedCourse.getId()), studentToken, status().isCreated())
                .at("/data/conversationId")
                .asLong();
        Long deletedConversationId = postAsk("""
                {
                    "courseId": %d,
                    "message": "Start deleted conversation"
                }
                """.formatted(deletedCourse.getId()), studentToken, status().isCreated())
                .at("/data/conversationId")
                .asLong();
        clearInvocations(geminiClient);

        archivedCourse.setStatus(CourseStatus.ARCHIVED);
        courseRepository.save(archivedCourse);
        deletedCourse.setDeletedAt(LocalDateTime.now());
        courseRepository.save(deletedCourse);

        postAsk("""
                {
                    "conversationId": %d,
                    "message": "Continue archived conversation"
                }
                """.formatted(archivedConversationId), studentToken, status().isForbidden());
        postAsk("""
                {
                    "conversationId": %d,
                    "message": "Continue deleted conversation"
                }
                """.formatted(deletedConversationId), studentToken, status().isNotFound());
        verifyNoInteractions(geminiClient);
    }

    @Test
    void shouldNotPersistConversationWhenGeminiFails() throws Exception {
        when(geminiClient.generate(anyString(), anyList()))
                .thenThrow(new BusinessException("Gemini AI is not configured", HttpStatus.SERVICE_UNAVAILABLE));

        postAsk("""
                { "message": "Xin trợ giúp." }
                """, studentToken, status().isServiceUnavailable());

        assertThat(conversationRepository.count()).isZero();
        assertThat(messageRepository.count()).isZero();
    }

    private JsonNode postAsk(String json, String accessToken, ResultMatcher expectedStatus) throws Exception {
        String response = mockMvc.perform(post("/api/v1/chat/ask")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(expectedStatus)
                .andReturn()
                .getResponse()
                .getContentAsString();
        return response.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(response);
    }

    private Course createCourse(String title, CourseStatus status) {
        Course course = Course.builder()
                .instructor(instructor)
                .title(title)
                .slug(slug(title))
                .subtitle("Chat integration course")
                .description("Course description for chatbot context")
                .level(CourseLevel.BEGINNER)
                .language("vi")
                .price(BigDecimal.ZERO)
                .currency("VND")
                .status(status)
                .totalLessons(0)
                .totalDurationSeconds(0)
                .totalEnrollments(0)
                .avgRating(BigDecimal.ZERO)
                .totalReviews(0)
                .publishedAt(status == CourseStatus.PUBLISHED ? LocalDateTime.now() : null)
                .build();
        return courseRepository.save(course);
    }

    private User createUser(String email, String fullName, RoleName roleName) {
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(PASSWORD))
                .fullName(fullName)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .roles(new HashSet<>(Set.of(role)))
                .build();
        return userRepository.save(user);
    }

    private String token(User user) {
        return jwtService.generateAccessToken(user);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String slug(String title) {
        return title.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-")
                + "-" + System.nanoTime();
    }

    private void cleanDatabase() {
        notificationRepository.deleteAll();
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
    }
}
