package com.example.skillora_platform.course;

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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.skillora_platform.admin.repository.AuditLogRepository;
import com.example.skillora_platform.assignment.repository.AssignmentRepository;
import com.example.skillora_platform.assignment.repository.AssignmentSubmissionRepository;
import com.example.skillora_platform.chat.repository.ChatConversationRepository;
import com.example.skillora_platform.chat.repository.ChatMessageRepository;
import com.example.skillora_platform.commerce.repository.CartItemRepository;
import com.example.skillora_platform.commerce.repository.CartRepository;
import com.example.skillora_platform.commerce.repository.CouponRepository;
import com.example.skillora_platform.commerce.repository.OrderItemRepository;
import com.example.skillora_platform.commerce.repository.OrderRepository;
import com.example.skillora_platform.commerce.repository.PaymentTransactionRepository;
import com.example.skillora_platform.commerce.repository.WishlistRepository;
import com.example.skillora_platform.course.entity.Course;
import com.example.skillora_platform.course.entity.CourseLevel;
import com.example.skillora_platform.course.entity.CourseStatus;
import com.example.skillora_platform.course.entity.CourseVersion;
import com.example.skillora_platform.course.entity.CourseVersionStatus;
import com.example.skillora_platform.course.repository.CourseRepository;
import com.example.skillora_platform.course.repository.CourseVersionRepository;
import com.example.skillora_platform.course.repository.LessonRepository;
import com.example.skillora_platform.course.repository.SectionRepository;
import com.example.skillora_platform.enrollment.repository.CourseCertificateRepository;
import com.example.skillora_platform.enrollment.repository.EnrollmentRepository;
import com.example.skillora_platform.enrollment.repository.LessonProgressRepository;
import com.example.skillora_platform.notification.repository.NotificationRepository;
import com.example.skillora_platform.review.repository.ReviewLikeRepository;
import com.example.skillora_platform.review.repository.ReviewRepository;
import com.example.skillora_platform.user.entity.Role;
import com.example.skillora_platform.user.entity.RoleName;
import com.example.skillora_platform.user.entity.User;
import com.example.skillora_platform.user.entity.UserStatus;
import com.example.skillora_platform.user.repository.RoleRepository;
import com.example.skillora_platform.user.repository.UserRepository;
import com.example.skillora_platform.user.service.JwtService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CourseVersionIntegrationTest {

    private static final String PASSWORD = "Password@123";

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private CourseVersionRepository courseVersionRepository;
    @Autowired private SectionRepository sectionRepository;
    @Autowired private LessonRepository lessonRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;
    @Autowired private LessonProgressRepository lessonProgressRepository;
    @Autowired private CourseCertificateRepository courseCertificateRepository;
    @Autowired private AssignmentRepository assignmentRepository;
    @Autowired private AssignmentSubmissionRepository assignmentSubmissionRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ReviewLikeRepository reviewLikeRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private WishlistRepository wishlistRepository;
    @Autowired private CouponRepository couponRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private PaymentTransactionRepository paymentTransactionRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private ChatConversationRepository chatConversationRepository;
    @Autowired private ChatMessageRepository chatMessageRepository;
    @Autowired private AuditLogRepository auditLogRepository;

    private User admin;
    private User instructor;
    private String adminToken;
    private String instructorToken;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        admin = createUser("admin-version@skillora.test", "Admin Version", RoleName.ADMIN);
        instructor = createUser("instructor-version@skillora.test", "Instructor Version", RoleName.INSTRUCTOR);
        adminToken = token(admin);
        instructorToken = token(instructor);
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void shouldReviewAndApprovePublishedCourseVersion() throws Exception {
        Course course = createCourse("Published Version Course", CourseStatus.PUBLISHED);

        String draftResponse = mockMvc.perform(post("/api/v1/courses/{courseId}/versions", course.getId())
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.versionNumber").value(1))
                .andExpect(jsonPath("$.data.snapshotJson").isString())
                .andReturn().getResponse().getContentAsString();

        CourseVersion version = courseVersionRepository.findAll().get(0);
        assertThat(draftResponse).contains("Published Version Course");

        mockMvc.perform(put("/api/v1/courses/{courseId}/versions/{versionId}", course.getId(), version.getId())
                        .header("Authorization", bearer(instructorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Reviewed Version Course",
                                    "subtitle": "Updated through version",
                                    "description": "This change should wait for admin approval.",
                                    "thumbnailUrl": "https://example.com/version.png"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Reviewed Version Course"));

        mockMvc.perform(patch("/api/v1/courses/{courseId}/versions/{versionId}/submit", course.getId(), version.getId())
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REVIEWING"));

        mockMvc.perform(patch("/api/v1/admin/courses/{courseId}/versions/{versionId}/approve",
                        course.getId(), version.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        Course approvedCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(approvedCourse.getTitle()).isEqualTo("Reviewed Version Course");
        assertThat(approvedCourse.getCurrentVersion()).isEqualTo(1);
        assertThat(courseVersionRepository.findById(version.getId()).orElseThrow().getStatus())
                .isEqualTo(CourseVersionStatus.APPROVED);
        assertThat(auditLogRepository.findAll())
                .anyMatch(log -> log.getAction().equals("APPROVE_COURSE_VERSION")
                        && log.getEntityType().equals("COURSE_VERSION")
                        && log.getEntityId().equals(version.getId()));
    }

    @Test
    void shouldRejectPublishedCourseVersionWithReason() throws Exception {
        Course course = createCourse("Rejected Version Course", CourseStatus.PUBLISHED);
        CourseVersion version = createSubmittedVersion(course);

        mockMvc.perform(patch("/api/v1/admin/courses/{courseId}/versions/{versionId}/reject",
                        course.getId(), version.getId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason": "Needs clearer learning outcomes"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.rejectReason").value("Needs clearer learning outcomes"));
    }

    @Test
    void shouldBlockInstructorDirectUpdateOfPublishedCourse() throws Exception {
        Course course = createCourse("Locked Published Course", CourseStatus.PUBLISHED);

        mockMvc.perform(put("/api/v1/courses/{id}", course.getId())
                        .header("Authorization", bearer(instructorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Direct Live Change",
                                    "description": "Should be blocked",
                                    "level": "BEGINNER",
                                    "language": "vi",
                                    "price": 0,
                                    "currency": "VND"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    private CourseVersion createSubmittedVersion(Course course) throws Exception {
        mockMvc.perform(post("/api/v1/courses/{courseId}/versions", course.getId())
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isCreated());
        CourseVersion version = courseVersionRepository.findAll().get(0);
        mockMvc.perform(patch("/api/v1/courses/{courseId}/versions/{versionId}/submit", course.getId(), version.getId())
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isOk());
        return version;
    }

    private Course createCourse(String title, CourseStatus status) {
        return courseRepository.save(Course.builder()
                .instructor(instructor)
                .title(title)
                .slug(slug(title))
                .subtitle("Version test course")
                .description("Course description")
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
                .build());
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
        auditLogRepository.deleteAll();
        notificationRepository.deleteAll();
        chatMessageRepository.deleteAll();
        chatConversationRepository.deleteAll();
        reviewLikeRepository.deleteAll();
        reviewRepository.deleteAll();
        assignmentSubmissionRepository.deleteAll();
        assignmentRepository.deleteAll();
        lessonProgressRepository.deleteAll();
        courseCertificateRepository.deleteAll();
        enrollmentRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        wishlistRepository.deleteAll();
        paymentTransactionRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        couponRepository.deleteAll();
        courseVersionRepository.deleteAll();
        lessonRepository.deleteAll();
        sectionRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
    }
}
