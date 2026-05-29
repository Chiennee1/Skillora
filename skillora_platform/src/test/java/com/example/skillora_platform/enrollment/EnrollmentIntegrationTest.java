package com.example.skillora_platform.enrollment;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.skillora_platform.course.dto.BunnyVideoCreated;
import com.example.skillora_platform.course.repository.CategoryRepository;
import com.example.skillora_platform.course.repository.CourseOutcomeRepository;
import com.example.skillora_platform.course.repository.CourseRepository;
import com.example.skillora_platform.course.repository.CourseRequirementRepository;
import com.example.skillora_platform.course.repository.LessonRepository;
import com.example.skillora_platform.course.repository.LessonResourceRepository;
import com.example.skillora_platform.course.repository.LessonVideoRepository;
import com.example.skillora_platform.course.repository.SectionRepository;
import com.example.skillora_platform.course.service.BunnyStreamClient;
import com.example.skillora_platform.enrollment.repository.CourseCertificateRepository;
import com.example.skillora_platform.enrollment.repository.EnrollmentRepository;
import com.example.skillora_platform.enrollment.repository.LessonProgressRepository;
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
import com.example.skillora_platform.user.service.JwtService;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "skillora.video.bunny.library-id=12345",
                "skillora.video.bunny.api-key=test-secret",
                "skillora.video.bunny.upload-expiration=PT2H"
        }
)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class EnrollmentIntegrationTest {

    private static final String PASSWORD = "Password@123";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private InstructorProfileRepository instructorProfileRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private CourseRequirementRepository courseRequirementRepository;
    @Autowired private CourseOutcomeRepository courseOutcomeRepository;
    @Autowired private SectionRepository sectionRepository;
    @Autowired private LessonRepository lessonRepository;
    @Autowired private LessonVideoRepository lessonVideoRepository;
    @Autowired private LessonResourceRepository lessonResourceRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;
    @Autowired private LessonProgressRepository lessonProgressRepository;
    @Autowired private CourseCertificateRepository courseCertificateRepository;
    @MockitoBean private BunnyStreamClient bunnyStreamClient;

    private User admin;
    private User instructor;
    private User student;
    private User otherStudent;
    private String adminToken;
    private String instructorToken;
    private String studentToken;
    private String otherStudentToken;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        admin = createUser("admin@example.com", "Admin User", RoleName.ADMIN);
        instructor = createUser("instructor@example.com", "Instructor User", RoleName.INSTRUCTOR);
        student = createUser("student@example.com", "Student User", RoleName.STUDENT);
        otherStudent = createUser("other.student@example.com", "Other Student", RoleName.STUDENT);
        adminToken = token(admin);
        instructorToken = token(instructor);
        studentToken = token(student);
        otherStudentToken = token(otherStudent);
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void shouldEnrollStudentInFreeCourseAndPreventDuplicate() throws Exception {
        Long courseId = createPublishedFreeCourse("Java Basics");

        // Student enrolls successfully
        mockMvc.perform(post("/api/v1/courses/{id}/enroll", courseId)
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.courseId").value(courseId))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.progressPercent").value(0));

        // Duplicate enrollment fails
        mockMvc.perform(post("/api/v1/courses/{id}/enroll", courseId)
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isConflict());

        // Other student can also enroll
        mockMvc.perform(post("/api/v1/courses/{id}/enroll", courseId)
                        .header("Authorization", bearer(otherStudentToken)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectEnrollmentForPaidCourseAndInstructorOwnCourse() throws Exception {
        Long paidCourseId = createPublishedPaidCourse("Paid Course");
        Long freeCourseId = createPublishedFreeCourse("Instructor Course");

        // Paid course requires payment flow
        mockMvc.perform(post("/api/v1/courses/{id}/enroll", paidCourseId)
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isPaymentRequired());

        // Instructor cannot enroll in own course
        mockMvc.perform(post("/api/v1/courses/{id}/enroll", freeCourseId)
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListStudentEnrollments() throws Exception {
        Long courseId = createPublishedFreeCourse("My Course");
        enroll(courseId, studentToken);

        mockMvc.perform(get("/api/v1/enrollments/me")
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].courseTitle").value("My Course"))
                .andExpect(jsonPath("$.data.content[0].instructorName").value("Instructor User"));

        // Filter by status
        mockMvc.perform(get("/api/v1/enrollments/me?status=ACTIVE")
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)));

        mockMvc.perform(get("/api/v1/enrollments/me?status=COMPLETED")
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    void shouldTrackLessonProgressAndAutoComplete() throws Exception {
        Long categoryId = createCategory("Backend", adminToken);
        Long courseId = createCourse("Progress Course", categoryId, 0, instructorToken);
        Long sectionId = createSection(courseId, "Module 1", instructorToken);
        Long lesson1Id = createLesson(sectionId, "Lesson 1", "TEXT", 600, false, instructorToken);
        Long lesson2Id = createLesson(sectionId, "Lesson 2", "TEXT", 400, false, instructorToken);
        publishCourse(courseId, instructorToken);
        Long enrollmentId = enroll(courseId, studentToken);

        // Get initial progress — all lessons at 0
        mockMvc.perform(get("/api/v1/enrollments/{id}/progress", enrollmentId)
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].completed").value(false))
                .andExpect(jsonPath("$.data[1].completed").value(false));

        // Update progress on lesson 1 — mark completed
        mockMvc.perform(patch("/api/v1/enrollments/{id}/lessons/{lid}/progress", enrollmentId, lesson1Id)
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "watchedSeconds": 600, "completed": true }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(true))
                .andExpect(jsonPath("$.data.watchedSeconds").value(600));

        // Update progress on lesson 2 — auto-complete by watching >=90%
        mockMvc.perform(patch("/api/v1/enrollments/{id}/lessons/{lid}/progress", enrollmentId, lesson2Id)
                        .header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "watchedSeconds": 360 }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(true));

        // Enrollment should be COMPLETED now
        mockMvc.perform(get("/api/v1/enrollments/me?status=COMPLETED")
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.content[0].progressPercent").value(100.00));

        // Certificate should be auto-generated
        mockMvc.perform(get("/api/v1/enrollments/{id}/certificate", enrollmentId)
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.certificateCode").isNotEmpty())
                .andExpect(jsonPath("$.data.courseTitle").value("Progress Course"))
                .andExpect(jsonPath("$.data.studentName").value("Student User"));
    }

    @Test
    void shouldDenyProgressAccessToOtherStudents() throws Exception {
        Long courseId = createPublishedFreeCourse("Access Course");
        Long enrollmentId = enroll(courseId, studentToken);

        // Other student cannot access progress
        mockMvc.perform(get("/api/v1/enrollments/{id}/progress", enrollmentId)
                        .header("Authorization", bearer(otherStudentToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowEnrolledStudentToViewNonPreviewLessons() throws Exception {
        Long categoryId = createCategory("Backend", adminToken);
        Long courseId = createCourse("Enrolled Access Course", categoryId, 0, instructorToken);
        Long sectionId = createSection(courseId, "Module 1", instructorToken);
        Long privateLessonId = createLesson(sectionId, "Private Lesson", "TEXT", 300, false, instructorToken);
        publishCourse(courseId, instructorToken);

        // Non-enrolled student gets 403
        mockMvc.perform(get("/api/v1/lessons/{id}", privateLessonId)
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isForbidden());

        // Enroll and verify access
        enroll(courseId, studentToken);
        mockMvc.perform(get("/api/v1/lessons/{id}", privateLessonId)
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Private Lesson"));
    }

    @Test
    void shouldReturnLearningDashboard() throws Exception {
        Long courseId = createPublishedFreeCourse("Dashboard Course");
        enroll(courseId, studentToken);

        mockMvc.perform(get("/api/v1/learning/dashboard")
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalEnrolled").value(1))
                .andExpect(jsonPath("$.data.inProgress").value(1))
                .andExpect(jsonPath("$.data.completed").value(0))
                .andExpect(jsonPath("$.data.recentEnrollments", hasSize(1)));
    }

    // ==================== Helpers ====================

    private Long createPublishedFreeCourse(String title) throws Exception {
        Long categoryId = createCategory("General", adminToken);
        Long courseId = createCourse(title, categoryId, 0, instructorToken);
        Long sectionId = createSection(courseId, "Section 1", instructorToken);
        createLesson(sectionId, "Preview Lesson", "TEXT", 300, true, instructorToken);
        publishCourse(courseId, instructorToken);
        return courseId;
    }

    private Long createPublishedPaidCourse(String title) throws Exception {
        Long categoryId = createCategory("Paid", adminToken);
        Long courseId = createCourse(title, categoryId, 100000, instructorToken);
        Long sectionId = createSection(courseId, "Section 1", instructorToken);
        createLesson(sectionId, "Lesson 1", "TEXT", 300, true, instructorToken);
        publishCourse(courseId, instructorToken);
        return courseId;
    }

    private Long enroll(Long courseId, String accessToken) throws Exception {
        JsonNode response = postJson("/api/v1/courses/%d/enroll".formatted(courseId),
                "{}", accessToken, status().isCreated());
        return response.at("/data/id").asLong();
    }

    private void publishCourse(Long courseId, String accessToken) throws Exception {
        mockMvc.perform(patch("/api/v1/courses/{id}/publish", courseId)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk());
    }

    private Long createCategory(String name, String accessToken) throws Exception {
        JsonNode response = postJson("/api/v1/categories", """
                { "name": "%s", "orderIndex": 1 }
                """.formatted(name), accessToken, status().isCreated());
        return response.at("/data/id").asLong();
    }

    private Long createCourse(String title, Long categoryId, int price, String accessToken) throws Exception {
        JsonNode response = postJson("/api/v1/courses", """
                {
                    "title": "%s",
                    "subtitle": "Subtitle",
                    "description": "Description",
                    "level": "BEGINNER",
                    "language": "vi",
                    "price": %d,
                    "currency": "VND",
                    "categoryIds": [%d],
                    "requirements": ["Laptop"],
                    "outcomes": ["Learn"]
                }
                """.formatted(title, price, categoryId), accessToken, status().isCreated());
        return response.at("/data/id").asLong();
    }

    private Long createSection(Long courseId, String title, String accessToken) throws Exception {
        JsonNode response = postJson("/api/v1/courses/%d/sections".formatted(courseId), """
                {
                    "title": "%s",
                    "description": "Section description",
                    "orderIndex": 0,
                    "published": true
                }
                """.formatted(title), accessToken, status().isCreated());
        return response.at("/data/id").asLong();
    }

    private Long createLesson(
            Long sectionId, String title, String type, int durationSeconds, boolean preview, String accessToken
    ) throws Exception {
        JsonNode response = postJson("/api/v1/sections/%d/lessons".formatted(sectionId), """
                {
                    "title": "%s",
                    "type": "%s",
                    "content": "Content",
                    "durationSeconds": %d,
                    "preview": %s,
                    "published": true,
                    "orderIndex": %d
                }
                """.formatted(title, type, durationSeconds, preview, preview ? 0 : 1), accessToken, status().isCreated());
        return response.at("/data/id").asLong();
    }

    private JsonNode postJson(String path, String json, String accessToken, ResultMatcher expectedStatus)
            throws Exception {
        String response = mockMvc.perform(post(path)
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(expectedStatus)
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
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

    private void cleanDatabase() {
        courseCertificateRepository.deleteAll();
        lessonProgressRepository.deleteAll();
        enrollmentRepository.deleteAll();
        lessonResourceRepository.deleteAll();
        lessonVideoRepository.deleteAll();
        lessonRepository.deleteAll();
        sectionRepository.deleteAll();
        courseRequirementRepository.deleteAll();
        courseOutcomeRepository.deleteAll();
        courseRepository.deleteAll();
        categoryRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        instructorProfileRepository.deleteAll();
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
    }
}
