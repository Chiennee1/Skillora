package com.example.skillora_platform.admin;

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

import com.example.skillora_platform.admin.entity.AuditLog;
import com.example.skillora_platform.admin.repository.AuditLogRepository;
import com.example.skillora_platform.admin.repository.CourseStatsRepository;
import com.example.skillora_platform.assignment.repository.AssignmentRepository;
import com.example.skillora_platform.assignment.repository.AssignmentSubmissionRepository;
import com.example.skillora_platform.chat.repository.ChatConversationRepository;
import com.example.skillora_platform.chat.repository.ChatMessageRepository;
import com.example.skillora_platform.commerce.entity.Coupon;
import com.example.skillora_platform.commerce.entity.DiscountType;
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
import com.example.skillora_platform.course.repository.CourseRepository;
import com.example.skillora_platform.course.repository.LessonRepository;
import com.example.skillora_platform.course.repository.SectionRepository;
import com.example.skillora_platform.enrollment.entity.Enrollment;
import com.example.skillora_platform.enrollment.entity.EnrollmentStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AdminIntegrationTest {

    private static final String PASSWORD = "Password@123";
    private static final String ADMIN_PREFIX = "/api/v1/admin";

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CourseRepository courseRepository;
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
    @Autowired private CourseStatsRepository courseStatsRepository;

    private User admin;
    private User instructor;
    private User student;
    private String adminToken;
    private String studentToken;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        admin = createUser("admin@skillora.test", "Admin User", RoleName.ADMIN);
        instructor = createUser("instructor@skillora.test", "Instructor User", RoleName.INSTRUCTOR);
        student = createUser("student@skillora.test", "Student User", RoleName.STUDENT);
        adminToken = token(admin);
        studentToken = token(student);
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    // ──────────────────────────── Dashboard ────────────────────────────

    @Test
    void shouldReturnDashboardStats() throws Exception {
        createCourse("Dashboard Course", CourseStatus.PUBLISHED, BigDecimal.valueOf(100000));
        createCourse("Draft Course", CourseStatus.DRAFT, BigDecimal.ZERO);
        createEnrollment(student, courseRepository.findAll().get(0));

        mockMvc.perform(get(ADMIN_PREFIX + "/dashboard")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.users.total").exists())
                .andExpect(jsonPath("$.data.courses.total").value(2))
                .andExpect(jsonPath("$.data.courses.published").value(1))
                .andExpect(jsonPath("$.data.courses.draft").value(1))
                .andExpect(jsonPath("$.data.enrollments.total").value(1));
    }

    @Test
    void shouldReturnRevenueSummary() throws Exception {
        mockMvc.perform(get(ADMIN_PREFIX + "/revenue")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalRevenue").exists())
                .andExpect(jsonPath("$.data.totalPaidOrders").exists());
    }

    @Test
    void shouldDenyDashboardForNonAdmin() throws Exception {
        mockMvc.perform(get(ADMIN_PREFIX + "/dashboard")
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isForbidden());
    }

    // ──────────────────────────── User Management ────────────────────────────

    @Test
    void shouldListUsers() throws Exception {
        mockMvc.perform(get(ADMIN_PREFIX + "/users")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(3));
    }

    @Test
    void shouldListUsersFilteredByStatus() throws Exception {
        mockMvc.perform(get(ADMIN_PREFIX + "/users")
                        .param("status", "ACTIVE")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void shouldListUsersSearchByName() throws Exception {
        mockMvc.perform(get(ADMIN_PREFIX + "/users")
                        .param("search", "Student")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void shouldGetUserById() throws Exception {
        mockMvc.perform(get(ADMIN_PREFIX + "/users/{id}", student.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(student.getEmail()))
                .andExpect(jsonPath("$.data.roles").isArray());
    }

    @Test
    void shouldBanUser() throws Exception {
        mockMvc.perform(patch(ADMIN_PREFIX + "/users/{id}/status", student.getId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "BANNED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("BANNED"));

        // Verify audit log was created
        assertThat(auditLogRepository.findAll())
                .anyMatch(log -> log.getAction().equals("BAN_USER")
                        && log.getEntityType().equals("USER")
                        && log.getEntityId().equals(student.getId()));
    }

    @Test
    void shouldNotBanSelf() throws Exception {
        mockMvc.perform(patch(ADMIN_PREFIX + "/users/{id}/status", admin.getId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "BANNED"}
                                """))
                .andExpect(status().isBadRequest());
    }

    // ──────────────────────────── Course Approval ────────────────────────────

    @Test
    void shouldListCoursesForAdmin() throws Exception {
        createCourse("Admin View Course", CourseStatus.REVIEWING, BigDecimal.ZERO);

        mockMvc.perform(get(ADMIN_PREFIX + "/courses")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void shouldApproveCourse() throws Exception {
        Course course = createCourse("Approval Course", CourseStatus.REVIEWING, BigDecimal.ZERO);

        mockMvc.perform(patch(ADMIN_PREFIX + "/courses/{id}/approve", course.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.publishedAt").exists());

        // Verify audit log
        assertThat(auditLogRepository.findAll())
                .anyMatch(log -> log.getAction().equals("APPROVE_COURSE")
                        && log.getEntityId().equals(course.getId()));
    }

    @Test
    void shouldRejectCourseWithReason() throws Exception {
        Course course = createCourse("Reject Course", CourseStatus.REVIEWING, BigDecimal.ZERO);

        mockMvc.perform(patch(ADMIN_PREFIX + "/courses/{id}/reject", course.getId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason": "Content does not meet quality standards"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.rejectReason").value("Content does not meet quality standards"));
    }

    @Test
    void shouldRejectCourseWithoutReason() throws Exception {
        Course course = createCourse("Reject Without Reason Course", CourseStatus.REVIEWING, BigDecimal.ZERO);

        mockMvc.perform(patch(ADMIN_PREFIX + "/courses/{id}/reject", course.getId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason": " "}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotApproveAlreadyPublishedCourse() throws Exception {
        Course course = createCourse("Published Course", CourseStatus.PUBLISHED, BigDecimal.ZERO);

        mockMvc.perform(patch(ADMIN_PREFIX + "/courses/{id}/approve", course.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldNotApproveDraftCourse() throws Exception {
        Course course = createCourse("Draft Approval Course", CourseStatus.DRAFT, BigDecimal.ZERO);

        mockMvc.perform(patch(ADMIN_PREFIX + "/courses/{id}/approve", course.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isConflict());
    }

    // ──────────────────────────── Coupon CRUD ────────────────────────────

    @Test
    void shouldCreateCoupon() throws Exception {
        mockMvc.perform(post(ADMIN_PREFIX + "/coupons")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "ADMIN_TEST_50",
                                    "name": "Admin test coupon",
                                    "discountType": "PERCENT",
                                    "discountValue": 50
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("ADMIN_TEST_50"))
                .andExpect(jsonPath("$.data.discountType").value("PERCENT"))
                .andExpect(jsonPath("$.data.active").value(true));

        assertThat(couponRepository.findByCodeIgnoreCase("ADMIN_TEST_50")).isPresent();
    }

    @Test
    void shouldListCoupons() throws Exception {
        couponRepository.save(Coupon.builder()
                .code("LIST_TEST").name("List test").discountType(DiscountType.FIXED)
                .discountValue(BigDecimal.valueOf(10000)).usedCount(0).active(true).build());

        mockMvc.perform(get(ADMIN_PREFIX + "/coupons")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void shouldUpdateCoupon() throws Exception {
        Coupon coupon = couponRepository.save(Coupon.builder()
                .code("UPDATE_TEST").name("Update test").discountType(DiscountType.PERCENT)
                .discountValue(BigDecimal.valueOf(20)).usedCount(0).active(true).build());

        mockMvc.perform(put(ADMIN_PREFIX + "/coupons/{id}", coupon.getId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Updated name", "discountValue": 30}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated name"));
    }

    @Test
    void shouldDeactivateCoupon() throws Exception {
        Coupon coupon = couponRepository.save(Coupon.builder()
                .code("DEACTIVATE_TEST").name("Deactivate test").discountType(DiscountType.FIXED)
                .discountValue(BigDecimal.valueOf(5000)).usedCount(0).active(true).build());

        mockMvc.perform(delete(ADMIN_PREFIX + "/coupons/{id}", coupon.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());

        assertThat(couponRepository.findById(coupon.getId()).get().isActive()).isFalse();
    }

    @Test
    void shouldNotCreateDuplicateCoupon() throws Exception {
        couponRepository.save(Coupon.builder()
                .code("DUPLICATE").name("Dup").discountType(DiscountType.FIXED)
                .discountValue(BigDecimal.valueOf(1000)).usedCount(0).active(true).build());

        mockMvc.perform(post(ADMIN_PREFIX + "/coupons")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "DUPLICATE",
                                    "discountType": "FIXED",
                                    "discountValue": 2000
                                }
                                """))
                .andExpect(status().isConflict());
    }

    // ──────────────────────────── Audit Logs ────────────────────────────

    @Test
    void shouldListAuditLogs() throws Exception {
        // Ban a user to generate an audit log
        mockMvc.perform(patch(ADMIN_PREFIX + "/users/{id}/status", student.getId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "BANNED"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get(ADMIN_PREFIX + "/audit-logs")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].action").value("BAN_USER"))
                .andExpect(jsonPath("$.data.content[0].entityType").value("USER"));
    }

    @Test
    void shouldFilterAuditLogsByEntityType() throws Exception {
        // Create coupon to generate COUPON audit log
        mockMvc.perform(post(ADMIN_PREFIX + "/coupons")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "code": "AUDIT_FILTER_TEST",
                                    "discountType": "PERCENT",
                                    "discountValue": 10
                                }
                                """))
                .andExpect(status().isCreated());

        // Ban user to generate USER audit log
        mockMvc.perform(patch(ADMIN_PREFIX + "/users/{id}/status", student.getId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "BANNED"}
                                """))
                .andExpect(status().isOk());

        // Filter by COUPON entity type
        mockMvc.perform(get(ADMIN_PREFIX + "/audit-logs")
                        .param("entityType", "COUPON")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].entityType").value("COUPON"));
    }

    // ──────────────────────────── Helpers ────────────────────────────

    private Course createCourse(String title, CourseStatus status, BigDecimal price) {
        return courseRepository.save(Course.builder()
                .instructor(instructor)
                .title(title)
                .slug(slug(title))
                .subtitle("Admin test course")
                .description("Course description")
                .level(CourseLevel.BEGINNER)
                .language("vi")
                .price(price)
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

    private Enrollment createEnrollment(User user, Course course) {
        return enrollmentRepository.save(Enrollment.builder()
                .user(user)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .amountPaid(BigDecimal.ZERO)
                .progressPercent(BigDecimal.ZERO)
                .enrolledAt(LocalDateTime.now())
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
        courseStatsRepository.deleteAll();
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
        lessonRepository.deleteAll();
        sectionRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
    }
}
