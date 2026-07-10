package com.example.skillora_platform.course;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.skillora_platform.admin.repository.AuditLogRepository;
import com.example.skillora_platform.assignment.repository.AssignmentRepository;
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
import com.example.skillora_platform.notification.repository.NotificationRepository;
import com.example.skillora_platform.quiz.repository.QuizRepository;
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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "skillora.video.bunny.library-id=12345",
                "skillora.video.bunny.api-key=test-secret",
                "skillora.video.bunny.upload-expiration=PT2H",
                "skillora.video.bunny.webhook-signing-secret=webhook-secret",
                "skillora.video.bunny.token-security-key=token-secret",
                "skillora.video.bunny.pull-zone-host=videos.skillora.test",
                "skillora.video.bunny.allowed-mime-types=video/mp4,video/webm,video/quicktime",
                "skillora.video.bunny.max-file-size-bytes=500000"
        }
)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CourseIntegrationTest {

    private static final String PASSWORD = "Password@123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

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
    private CategoryRepository categoryRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseRequirementRepository courseRequirementRepository;

    @Autowired
    private CourseOutcomeRepository courseOutcomeRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LessonVideoRepository lessonVideoRepository;

    @Autowired
    private LessonResourceRepository lessonResourceRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @MockitoBean
    private BunnyStreamClient bunnyStreamClient;

    private User admin;
    private User instructor;
    private User otherInstructor;
    private User student;
    private String adminToken;
    private String instructorToken;
    private String otherInstructorToken;
    private String studentToken;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        admin = createUser("admin@example.com", "Admin User", RoleName.ADMIN);
        instructor = createUser("instructor@example.com", "Instructor User", RoleName.INSTRUCTOR);
        otherInstructor = createUser("other@example.com", "Other Instructor", RoleName.INSTRUCTOR);
        student = createUser("student@example.com", "Student User", RoleName.STUDENT);
        adminToken = token(admin);
        instructorToken = token(instructor);
        otherInstructorToken = token(otherInstructor);
        studentToken = token(student);
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void shouldExposePublicCategoriesAndPublishedCoursesOnly() throws Exception {
        Long categoryId = createCategory("Lập trình", adminToken);
        JsonNode draftCourse = createCourse("Java Spring Boot", categoryId, instructorToken, status().isCreated());

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].slug").value("lap-trinh"));

        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)));

        Long courseId = draftCourse.at("/data/id").asLong();
        mockMvc.perform(patch("/api/v1/courses/{id}/publish", courseId)
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Course submitted for review"))
                .andExpect(jsonPath("$.data.status").value("REVIEWING"));

        mockMvc.perform(get("/api/v1/courses?search=spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)));

        mockMvc.perform(patch("/api/v1/admin/courses/{id}/approve", courseId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        mockMvc.perform(get("/api/v1/courses?search=spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].slug").value("java-spring-boot"));

        mockMvc.perform(get("/api/v1/courses/{idOrSlug}", "java-spring-boot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Java Spring Boot"))
                .andExpect(jsonPath("$.data.categories[0].id").value(categoryId.intValue()));
    }

    @Test
    void shouldAllowOnlyAdminToMutateCategories() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson("Security")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", bearer(instructorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson("Security")))
                .andExpect(status().isForbidden());

        Long categoryId = createCategory("Security", adminToken);
        mockMvc.perform(put("/api/v1/categories/{id}", categoryId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Application Security",
                                    "orderIndex": 2,
                                    "active": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slug").value("application-security"));

        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void shouldEnforceCourseOwnershipAndGenerateUniqueSlugs() throws Exception {
        Long categoryId = createCategory("Backend", adminToken);
        JsonNode firstCourse = createCourse("Java Cơ bản", categoryId, instructorToken, status().isCreated());
        JsonNode secondCourse = createCourse("Java Cơ bản", categoryId, instructorToken, status().isCreated());

        mockMvc.perform(get("/api/v1/courses/me")
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)));

        mockMvc.perform(put("/api/v1/courses/{id}", firstCourse.at("/data/id").asLong())
                        .header("Authorization", bearer(otherInstructorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson("Hacked Course", categoryId)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/courses/{id}/publish", firstCourse.at("/data/id").asLong())
                        .header("Authorization", bearer(otherInstructorToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/courses/{id}", firstCourse.at("/data/id").asLong())
                        .header("Authorization", bearer(instructorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(courseJson("Java Cơ bản Updated", categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slug").value("java-co-ban-updated"));

        mockMvc.perform(get("/api/v1/courses/{idOrSlug}", secondCourse.at("/data/id").asLong())
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slug").value("java-co-ban-2"));
    }

    @Test
    void shouldManageSectionsLessonsAndRefreshCourseTotals() throws Exception {
        Long categoryId = createCategory("Backend", adminToken);
        Long courseId = createCourse("Course Content", categoryId, instructorToken, status().isCreated())
                .at("/data/id").asLong();
        Long sectionId = createSection(courseId, "Module 1", instructorToken);
        Long firstLessonId = createLesson(sectionId, "Intro", "VIDEO", 600, true, instructorToken);
        Long secondLessonId = createLesson(sectionId, "Deep Dive", "TEXT", 1200, false, instructorToken);

        mockMvc.perform(get("/api/v1/courses/{idOrSlug}", courseId)
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalLessons").value(2))
                .andExpect(jsonPath("$.data.totalDurationSeconds").value(1800));

        mockMvc.perform(put("/api/v1/sections/{id}", sectionId)
                        .header("Authorization", bearer(instructorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Updated Module",
                                    "description": "Updated",
                                    "orderIndex": 0,
                                    "published": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Module"));

        mockMvc.perform(put("/api/v1/lessons/{id}", firstLessonId)
                        .header("Authorization", bearer(instructorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(lessonJson("Intro Updated", "VIDEO", 900, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.durationSeconds").value(900));

        mockMvc.perform(delete("/api/v1/lessons/{id}", secondLessonId)
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/courses/{idOrSlug}", courseId)
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalLessons").value(1))
                .andExpect(jsonPath("$.data.totalDurationSeconds").value(900));
    }

    @Test
    void shouldExposePracticeIdsInLessonSummaries() throws Exception {
        Long categoryId = createCategory("Backend", adminToken);
        Long courseId = createCourse("Practice Summary Course", categoryId, instructorToken, status().isCreated())
                .at("/data/id").asLong();
        Long sectionId = createSection(courseId, "Practice Module", instructorToken);
        Long quizLessonId = createLesson(sectionId, "Knowledge Check", "QUIZ", 300, true, instructorToken);
        Long assignmentLessonId = createLesson(sectionId, "Capstone Task", "ASSIGNMENT", 600, false, instructorToken);

        Long quizId = postJson("/api/v1/quizzes", """
                {
                    "lessonId": %d,
                    "title": "Knowledge Check",
                    "passScore": 80,
                    "questions": [
                        {
                            "content": "What does Skillora provide?",
                            "type": "SINGLE",
                            "points": 10,
                            "answerOptions": [
                                { "content": "A learning marketplace", "correct": true },
                                { "content": "A note-taking app", "correct": false }
                            ]
                        }
                    ]
                }
                """.formatted(quizLessonId), instructorToken, status().isCreated())
                .at("/data/id").asLong();
        Long assignmentId = postJson("/api/v1/assignments", """
                {
                    "lessonId": %d,
                    "title": "Capstone Task",
                    "instructions": "Submit your final solution.",
                    "maxScore": 100,
                    "dueDays": 7
                }
                """.formatted(assignmentLessonId), instructorToken, status().isCreated())
                .at("/data/id").asLong();

        mockMvc.perform(get("/api/v1/courses/{id}/sections", courseId)
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].lessons[0].quizId").value(quizId.intValue()))
                .andExpect(jsonPath("$.data[0].lessons[0].assignmentId").doesNotExist())
                .andExpect(jsonPath("$.data[0].lessons[1].quizId").doesNotExist())
                .andExpect(jsonPath("$.data[0].lessons[1].assignmentId").value(assignmentId.intValue()));
    }

    @Test
    void shouldApplyPreviewAndOwnerLessonAccessRules() throws Exception {
        Long categoryId = createCategory("Backend", adminToken);
        Long courseId = createCourse("Access Course", categoryId, instructorToken, status().isCreated())
                .at("/data/id").asLong();
        Long sectionId = createSection(courseId, "Access Module", instructorToken);
        Long previewLessonId = createLesson(sectionId, "Preview", "TEXT", 300, true, instructorToken);
        Long privateLessonId = createLesson(sectionId, "Private", "TEXT", 300, false, instructorToken);
        submitAndApproveCourse(courseId, instructorToken);

        mockMvc.perform(get("/api/v1/lessons/{id}", previewLessonId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Preview"));

        mockMvc.perform(get("/api/v1/lessons/{id}", privateLessonId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/lessons/{id}", privateLessonId)
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/lessons/{id}", privateLessonId)
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Private"));
    }

    @Test
    void shouldManageLessonResources() throws Exception {
        Long categoryId = createCategory("Backend", adminToken);
        Long courseId = createCourse("Resource Course", categoryId, instructorToken, status().isCreated())
                .at("/data/id").asLong();
        Long sectionId = createSection(courseId, "Resource Module", instructorToken);
        Long lessonId = createLesson(sectionId, "Resources", "TEXT", 300, false, instructorToken);

        JsonNode resource = postJson("/api/v1/lessons/%d/resources".formatted(lessonId), """
                {
                    "name": "Starter ZIP",
                    "fileUrl": "https://cdn.skillora.vn/starter.zip",
                    "resourceType": "ZIP",
                    "sizeBytes": 1000,
                    "orderIndex": 1
                }
                """, instructorToken, status().isCreated());
        Long resourceId = resource.at("/data/id").asLong();

        mockMvc.perform(put("/api/v1/lesson-resources/{id}", resourceId)
                        .header("Authorization", bearer(instructorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated ZIP",
                                    "fileUrl": "https://cdn.skillora.vn/updated.zip",
                                    "resourceType": "ZIP",
                                    "sizeBytes": 2000,
                                    "orderIndex": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated ZIP"));

        mockMvc.perform(get("/api/v1/lessons/{id}", lessonId)
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resources", hasSize(1)))
                .andExpect(jsonPath("$.data.resources[0].orderIndex").value(2));

        mockMvc.perform(delete("/api/v1/lesson-resources/{id}", resourceId)
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/lessons/{id}", lessonId)
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resources", hasSize(0)));
    }

    @Test
    void shouldCreateBunnyUploadTicketAndPersistVideoWithoutSecretLeak() throws Exception {
        when(bunnyStreamClient.createVideo(anyString())).thenReturn(new BunnyVideoCreated("video-guid-123"));
        Long categoryId = createCategory("Backend", adminToken);
        Long courseId = createCourse("Video Course", categoryId, instructorToken, status().isCreated())
                .at("/data/id").asLong();
        Long sectionId = createSection(courseId, "Video Module", instructorToken);
        Long lessonId = createLesson(sectionId, "Upload Lesson", "VIDEO", 300, false, instructorToken);

        mockMvc.perform(post("/api/v1/lessons/{id}/video/upload-url", lessonId)
                        .header("Authorization", bearer(otherInstructorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(uploadJson()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/lessons/{id}/video/upload-url", lessonId)
                        .header("Authorization", bearer(instructorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(uploadJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lessonVideoId").isNumber())
                .andExpect(jsonPath("$.data.videoId").value("video-guid-123"))
                .andExpect(jsonPath("$.data.uploadUrl").value("https://video.bunnycdn.com/tusupload"))
                .andExpect(jsonPath("$.data.headers.AuthorizationSignature", not(blankOrNullString())))
                .andExpect(jsonPath("$.data.headers.AuthorizationExpire", not(blankOrNullString())))
                .andExpect(jsonPath("$.data.headers.VideoId").value("video-guid-123"))
                .andExpect(jsonPath("$.data.headers.LibraryId").value("12345"))
                .andExpect(jsonPath("$.data.headers.AccessKey").doesNotExist())
                .andExpect(jsonPath("$.data.metadata.filetype").value("video/mp4"));

        mockMvc.perform(get("/api/v1/lessons/{id}", lessonId)
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.video.assetId").value("video-guid-123"))
                .andExpect(jsonPath("$.data.video.status").value("UPLOADING"))
                .andExpect(jsonPath("$.data.video.mimeType").value("video/mp4"));

        mockMvc.perform(get("/api/v1/courses/{id}/sections", courseId)
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].lessons[0].hasVideo").value(true))
                .andExpect(jsonPath("$.data[0].lessons[0].videoStatus").value("UPLOADING"));
    }

    @Test
    void shouldRejectInvalidBunnyUploadMetadata() throws Exception {
        Long categoryId = createCategory("Backend", adminToken);
        Long courseId = createCourse("Invalid Video Course", categoryId, instructorToken, status().isCreated())
                .at("/data/id").asLong();
        Long sectionId = createSection(courseId, "Video Module", instructorToken);
        Long lessonId = createLesson(sectionId, "Upload Lesson", "VIDEO", 300, false, instructorToken);

        mockMvc.perform(post("/api/v1/lessons/{id}/video/upload-url", lessonId)
                        .header("Authorization", bearer(instructorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fileName": "lesson.exe",
                                    "mimeType": "application/octet-stream",
                                    "fileSizeBytes": 123
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/lessons/{id}/video/upload-url", lessonId)
                        .header("Authorization", bearer(instructorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "fileName": "lesson.mp4",
                                    "mimeType": "video/mp4",
                                    "fileSizeBytes": 500001
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldVerifyBunnyWebhookAndExposeSignedEmbedWhenReady() throws Exception {
        when(bunnyStreamClient.createVideo(anyString())).thenReturn(new BunnyVideoCreated("video-guid-123"));
        Long categoryId = createCategory("Backend", adminToken);
        Long courseId = createCourse("Ready Video Course", categoryId, instructorToken, status().isCreated())
                .at("/data/id").asLong();
        Long sectionId = createSection(courseId, "Video Module", instructorToken);
        Long lessonId = createLesson(sectionId, "Upload Lesson", "VIDEO", 300, true, instructorToken);

        postJson("/api/v1/lessons/%d/video/upload-url".formatted(lessonId), uploadJson(),
                instructorToken, status().isOk());

        postBunnyWebhook("video-guid-123", 3, "12345", status().isOk());
        postBunnyWebhook("video-guid-123", 3, "12345", status().isOk());

        mockMvc.perform(get("/api/v1/lessons/{id}", lessonId)
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.video.status").value("READY"))
                .andExpect(jsonPath("$.data.video.embedUrl", containsString(
                        "https://player.mediadelivery.net/embed/12345/video-guid-123?token=")))
                .andExpect(jsonPath("$.data.video.embedUrl", containsString("&expires=")))
                .andExpect(jsonPath("$.data.video.playbackUrl").doesNotExist())
                .andExpect(jsonPath("$.data.video.hlsUrl").doesNotExist())
                .andExpect(jsonPath("$.data.video.thumbnailUrl").value(
                        "https://videos.skillora.test/video-guid-123/thumbnail.jpg"));

        mockMvc.perform(patch("/api/v1/courses/{id}/publish", courseId)
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REVIEWING"));
        mockMvc.perform(patch("/api/v1/admin/courses/{id}/approve", courseId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        mockMvc.perform(get("/api/v1/lessons/{id}", lessonId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.video.embedUrl", containsString("token=")));
    }

    @Test
    void shouldRejectInvalidBunnyWebhookAndHandleFailedStatus() throws Exception {
        when(bunnyStreamClient.createVideo(anyString())).thenReturn(new BunnyVideoCreated("failed-guid"));
        Long categoryId = createCategory("Backend", adminToken);
        Long courseId = createCourse("Failed Video Course", categoryId, instructorToken, status().isCreated())
                .at("/data/id").asLong();
        Long sectionId = createSection(courseId, "Video Module", instructorToken);
        Long lessonId = createLesson(sectionId, "Upload Lesson", "VIDEO", 300, false, instructorToken);

        postJson("/api/v1/lessons/%d/video/upload-url".formatted(lessonId), uploadJson(),
                instructorToken, status().isOk());

        String invalidSignaturePayload = bunnyWebhookJson("failed-guid", 3, "12345");
        mockMvc.perform(post("/api/v1/videos/bunny/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-BunnyStream-Signature-Version", "v1")
                        .header("X-BunnyStream-Signature-Algorithm", "hmac-sha256")
                        .header("X-BunnyStream-Signature", "invalid")
                        .content(invalidSignaturePayload))
                .andExpect(status().isUnauthorized());

        postBunnyWebhook("failed-guid", 3, "99999", status().isBadRequest());
        postBunnyWebhook("unknown-guid", 3, "12345", status().isOk());
        postBunnyWebhook("failed-guid", 5, "12345", status().isOk());

        mockMvc.perform(get("/api/v1/lessons/{id}", lessonId)
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.video.status").value("FAILED"))
                .andExpect(jsonPath("$.data.video.errorMessage", containsString("status 5")));
    }

    @Test
    void shouldBlockCourseReviewUntilPublishedVideoLessonsAreReady() throws Exception {
        Long categoryId = createCategory("Backend", adminToken);
        Long courseId = createCourse("Blocked Video Course", categoryId, instructorToken, status().isCreated())
                .at("/data/id").asLong();
        Long sectionId = createSection(courseId, "Video Module", instructorToken);
        createLesson(sectionId, "Missing Video", "VIDEO", 300, false, instructorToken);

        mockMvc.perform(patch("/api/v1/courses/{id}/publish", courseId)
                        .header("Authorization", bearer(instructorToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("READY video")));
    }

    private Long createCategory(String name, String accessToken) throws Exception {
        JsonNode response = postJson("/api/v1/categories", categoryJson(name), accessToken, status().isCreated());
        return response.at("/data/id").asLong();
    }

    private JsonNode createCourse(
            String title,
            Long categoryId,
            String accessToken,
            ResultMatcher expectedStatus
    ) throws Exception {
        return postJson("/api/v1/courses", courseJson(title, categoryId), accessToken, expectedStatus);
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
            Long sectionId,
            String title,
            String type,
            int durationSeconds,
            boolean preview,
            String accessToken
    ) throws Exception {
        JsonNode response = postJson("/api/v1/sections/%d/lessons".formatted(sectionId),
                lessonJson(title, type, durationSeconds, preview), accessToken, status().isCreated());
        return response.at("/data/id").asLong();
    }

    private void submitAndApproveCourse(Long courseId, String accessToken) throws Exception {
        mockMvc.perform(patch("/api/v1/courses/{id}/publish", courseId)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REVIEWING"));
        mockMvc.perform(patch("/api/v1/admin/courses/{id}/approve", courseId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
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

    private String categoryJson(String name) {
        return """
                {
                    "name": "%s",
                    "orderIndex": 1
                }
                """.formatted(name);
    }

    private String courseJson(String title, Long categoryId) {
        return """
                {
                    "title": "%s",
                    "subtitle": "Course subtitle",
                    "description": "Course description",
                    "level": "BEGINNER",
                    "language": "vi",
                    "price": 100000,
                    "discountPrice": 50000,
                    "currency": "VND",
                    "categoryIds": [%d],
                    "requirements": ["Laptop"],
                    "outcomes": ["Build APIs"]
                }
                """.formatted(title, categoryId);
    }

    private String lessonJson(String title, String type, int durationSeconds, boolean preview) {
        return """
                {
                    "title": "%s",
                    "type": "%s",
                    "content": "Lesson content",
                    "durationSeconds": %d,
                    "preview": %s,
                    "published": true,
                    "orderIndex": %d
                }
                """.formatted(title, type, durationSeconds, preview, preview ? 0 : 1);
    }

    private String uploadJson() {
        return """
                {
                    "fileName": "lesson.mp4",
                    "mimeType": "video/mp4",
                    "fileSizeBytes": 123456
                }
                """;
    }

    private void postBunnyWebhook(
            String videoGuid,
            int statusCode,
            String libraryId,
            ResultMatcher expectedStatus
    ) throws Exception {
        String rawBody = bunnyWebhookJson(videoGuid, statusCode, libraryId);
        mockMvc.perform(post("/api/v1/videos/bunny/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-BunnyStream-Signature-Version", "v1")
                        .header("X-BunnyStream-Signature-Algorithm", "hmac-sha256")
                        .header("X-BunnyStream-Signature", bunnySignature(rawBody))
                        .content(rawBody))
                .andExpect(expectedStatus);
    }

    private String bunnyWebhookJson(String videoGuid, int statusCode, String libraryId) {
        return """
                {"VideoLibraryId":%s,"VideoGuid":"%s","Status":%d}
                """.formatted(libraryId, videoGuid, statusCode).trim();
    }

    private String bunnySignature(String rawBody) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("webhook-secret".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8)));
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
        auditLogRepository.deleteAll();
        notificationRepository.deleteAll();
        lessonResourceRepository.deleteAll();
        lessonVideoRepository.deleteAll();
        quizRepository.deleteAll();
        assignmentRepository.deleteAll();
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
