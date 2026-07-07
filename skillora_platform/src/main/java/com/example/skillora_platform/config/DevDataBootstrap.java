package com.example.skillora_platform.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.assignment.entity.Assignment;
import com.example.skillora_platform.assignment.repository.AssignmentRepository;
import com.example.skillora_platform.commerce.entity.Coupon;
import com.example.skillora_platform.commerce.entity.DiscountType;
import com.example.skillora_platform.commerce.repository.CouponRepository;
import com.example.skillora_platform.course.entity.Category;
import com.example.skillora_platform.course.entity.Course;
import com.example.skillora_platform.course.entity.CourseLevel;
import com.example.skillora_platform.course.entity.CourseOutcome;
import com.example.skillora_platform.course.entity.CourseRequirement;
import com.example.skillora_platform.course.entity.CourseStatus;
import com.example.skillora_platform.course.entity.Lesson;
import com.example.skillora_platform.course.entity.LessonResource;
import com.example.skillora_platform.course.entity.LessonType;
import com.example.skillora_platform.course.entity.ResourceType;
import com.example.skillora_platform.course.entity.Section;
import com.example.skillora_platform.course.repository.CategoryRepository;
import com.example.skillora_platform.course.repository.CourseRepository;
import com.example.skillora_platform.course.repository.LessonRepository;
import com.example.skillora_platform.course.repository.SectionRepository;
import com.example.skillora_platform.enrollment.entity.Enrollment;
import com.example.skillora_platform.enrollment.entity.EnrollmentStatus;
import com.example.skillora_platform.enrollment.entity.LessonProgress;
import com.example.skillora_platform.enrollment.repository.EnrollmentRepository;
import com.example.skillora_platform.enrollment.repository.LessonProgressRepository;
import com.example.skillora_platform.quiz.entity.AnswerOption;
import com.example.skillora_platform.quiz.entity.Question;
import com.example.skillora_platform.quiz.entity.QuestionType;
import com.example.skillora_platform.quiz.entity.Quiz;
import com.example.skillora_platform.quiz.repository.QuizRepository;
import com.example.skillora_platform.user.entity.InstructorProfile;
import com.example.skillora_platform.user.entity.Role;
import com.example.skillora_platform.user.entity.RoleName;
import com.example.skillora_platform.user.entity.User;
import com.example.skillora_platform.user.entity.UserProfile;
import com.example.skillora_platform.user.entity.UserStatus;
import com.example.skillora_platform.user.repository.InstructorProfileRepository;
import com.example.skillora_platform.user.repository.RoleRepository;
import com.example.skillora_platform.user.repository.UserProfileRepository;
import com.example.skillora_platform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@Profile("dev")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "skillora.seed", name = "enabled", havingValue = "true")
public class DevDataBootstrap implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@skillora.test";
    private static final String INSTRUCTOR_EMAIL = "instructor@skillora.test";
    private static final String LEARNER_EMAIL = "learner@skillora.test";
    private static final String PUBLISHED_COURSE_SLUG = "spring-boot-production-apis";
    private static final String REVIEWING_COURSE_SLUG = "kubernetes-deployment-playbook";
    private static final String DEV_COUPON_CODE = "SKILLORA20";

    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final InstructorProfileRepository instructorProfileRepository;
    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final AssignmentRepository assignmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CouponRepository couponRepository;

    @Value("${skillora.seed.password:Skillora@12345}")
    private String defaultPassword;

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = role(RoleName.ADMIN, "System administrator with full platform permissions");
        Role instructorRole = role(RoleName.INSTRUCTOR, "Instructor who can create and manage courses");
        Role studentRole = role(RoleName.STUDENT, "Student who can enroll and learn courses");

        User admin = user(ADMIN_EMAIL, "Skillora Admin", adminRole);
        User instructor = user(INSTRUCTOR_EMAIL, "Skillora Instructor", instructorRole);
        User learner = user(LEARNER_EMAIL, "Skillora Learner", studentRole);

        ensureUserProfile(admin, "Platform operations", "Ho Chi Minh City");
        ensureUserProfile(instructor, "Backend and cloud instructor", "Ho Chi Minh City");
        ensureUserProfile(learner, "Learning backend engineering", "Ho Chi Minh City");
        ensureInstructorProfile(instructor);

        Category backend = category("Backend", "backend", 1);
        Category java = category("Java", "java", 2);
        Course publishedCourse = ensurePublishedCourse(instructor, Set.of(backend, java));
        ensureReviewingCourse(instructor, Set.of(backend));
        ensureLearnerEnrollment(learner, publishedCourse);
        ensureDevCoupon();
    }

    private Role role(RoleName name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(name)
                        .description(description)
                        .build()));
    }

    private User user(String email, String fullName, Role role) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .passwordHash(passwordEncoder.encode(defaultPassword))
                        .fullName(fullName)
                        .status(UserStatus.ACTIVE)
                        .emailVerified(true)
                        .roles(new HashSet<>(Set.of(role)))
                        .build()));
    }

    private void ensureUserProfile(User user, String headline, String location) {
        if (userProfileRepository.findByUserId(user.getId()).isPresent()) {
            return;
        }
        userProfileRepository.save(UserProfile.builder()
                .user(user)
                .headline(headline)
                .bio("Development seed account for Skillora UAT flows.")
                .location(location)
                .build());
    }

    private void ensureInstructorProfile(User instructor) {
        if (instructorProfileRepository.findByUserId(instructor.getId()).isPresent()) {
            return;
        }
        instructorProfileRepository.save(InstructorProfile.builder()
                .user(instructor)
                .title("Senior Backend Instructor")
                .expertise("Spring Boot, API security, payment workflows, production readiness")
                .verified(true)
                .build());
    }

    private Category category(String name, String slug, int orderIndex) {
        return categoryRepository.findAll().stream()
                .filter(category -> slug.equals(category.getSlug()))
                .findFirst()
                .map(existing -> {
                    if (!existing.isActive()) {
                        existing.setActive(true);
                        return categoryRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .slug(slug)
                        .orderIndex(orderIndex)
                        .active(true)
                        .build()));
    }

    private Course ensurePublishedCourse(User instructor, Set<Category> categories) {
        return courseRepository.findBySlugAndDeletedAtIsNull(PUBLISHED_COURSE_SLUG)
                .orElseGet(() -> createPublishedCourse(instructor, categories));
    }

    private Course createPublishedCourse(User instructor, Set<Category> categories) {
        Course course = Course.builder()
                .instructor(instructor)
                .title("Spring Boot Production APIs")
                .slug(PUBLISHED_COURSE_SLUG)
                .subtitle("Build secure learning APIs with admin review, payments, and access control.")
                .description("""
                        A development-ready sample course for validating Skillora catalog, enrollment, protected lessons,
                        quiz submission, assignment submission, and admin workflows.
                        """)
                .thumbnailUrl("https://images.unsplash.com/photo-1515879218367-8466d910aaa4")
                .level(CourseLevel.INTERMEDIATE)
                .language("vi")
                .price(new BigDecimal("1200000"))
                .discountPrice(new BigDecimal("890000"))
                .currency("VND")
                .status(CourseStatus.PUBLISHED)
                .totalLessons(5)
                .totalDurationSeconds(8700)
                .totalEnrollments(1)
                .avgRating(new BigDecimal("4.70"))
                .totalReviews(12)
                .publishedAt(LocalDateTime.now().minusDays(7))
                .categories(new HashSet<>(categories))
                .build();
        addRequirements(course, List.of("Java 17 basics", "REST API fundamentals"));
        addOutcomes(course, List.of(
                "Design secure Spring Boot APIs",
                "Protect course content by enrollment status",
                "Handle payment retry and idempotent callbacks"
        ));
        course = courseRepository.save(course);

        Section foundations = section(course, "Production foundations", 1, true);
        Lesson welcome = lesson(foundations, "Welcome and platform walkthrough", "welcome-platform-walkthrough",
                LessonType.TEXT, 900, true, true, 1);
        welcome.setContent("Use this preview lesson to verify public access without enrollment.");
        welcome.getResources().add(resource(welcome, "Course checklist", "https://example.com/skillora-checklist.pdf", 1));
        lessonRepository.save(welcome);

        Lesson accessControl = lesson(foundations, "Course access control rules", "course-access-control-rules",
                LessonType.TEXT, 1800, false, true, 2);
        accessControl.setContent("Learners can access protected lessons only through active or completed enrollments.");
        lessonRepository.save(accessControl);

        Section practice = section(course, "Practice and assessment", 2, true);
        Lesson quizLesson = lesson(practice, "Access control quiz", "access-control-quiz",
                LessonType.QUIZ, 1200, false, true, 1);
        lessonRepository.save(quizLesson);
        ensureQuiz(quizLesson);

        Lesson assignmentLesson = lesson(practice, "Implement a payment retry note", "payment-retry-assignment",
                LessonType.ASSIGNMENT, 1800, false, true, 2);
        lessonRepository.save(assignmentLesson);
        ensureAssignment(assignmentLesson);

        Section draftBonus = section(course, "Draft bonus content", 3, false);
        Lesson hiddenLesson = lesson(draftBonus, "Unpublished instructor notes", "unpublished-instructor-notes",
                LessonType.TEXT, 1500, false, false, 1);
        hiddenLesson.setContent("This lesson should stay hidden from public and enrolled learner flows.");
        lessonRepository.save(hiddenLesson);

        return course;
    }

    private void ensureReviewingCourse(User instructor, Set<Category> categories) {
        if (courseRepository.findBySlugAndDeletedAtIsNull(REVIEWING_COURSE_SLUG).isPresent()) {
            return;
        }
        Course course = Course.builder()
                .instructor(instructor)
                .title("Kubernetes Deployment Playbook")
                .slug(REVIEWING_COURSE_SLUG)
                .subtitle("Admin review queue sample course.")
                .description("Use this course to test approve/reject workflows before publishing.")
                .level(CourseLevel.ADVANCED)
                .language("vi")
                .price(new BigDecimal("1500000"))
                .currency("VND")
                .status(CourseStatus.REVIEWING)
                .totalLessons(1)
                .totalDurationSeconds(1800)
                .totalEnrollments(0)
                .avgRating(BigDecimal.ZERO)
                .totalReviews(0)
                .categories(new HashSet<>(categories))
                .build();
        addRequirements(course, List.of("Docker basics", "Basic Kubernetes concepts"));
        addOutcomes(course, List.of("Review deployment readiness", "Prepare rollout and rollback notes"));
        course = courseRepository.save(course);

        Section section = section(course, "Deployment review", 1, true);
        Lesson lesson = lesson(section, "Release checklist overview", "release-checklist-overview",
                LessonType.TEXT, 1800, false, true, 1);
        lesson.setContent("Admin should approve or reject this sample course from the review queue.");
        lessonRepository.save(lesson);
    }

    private Section section(Course course, String title, int orderIndex, boolean published) {
        return sectionRepository.save(Section.builder()
                .course(course)
                .title(title)
                .description(title + " section")
                .orderIndex(orderIndex)
                .published(published)
                .build());
    }

    private Lesson lesson(
            Section section,
            String title,
            String slug,
            LessonType type,
            int durationSeconds,
            boolean preview,
            boolean published,
            int orderIndex
    ) {
        return Lesson.builder()
                .section(section)
                .title(title)
                .slug(slug)
                .type(type)
                .durationSeconds(durationSeconds)
                .preview(preview)
                .published(published)
                .orderIndex(orderIndex)
                .build();
    }

    private LessonResource resource(Lesson lesson, String name, String url, int orderIndex) {
        return LessonResource.builder()
                .lesson(lesson)
                .name(name)
                .fileUrl(url)
                .resourceType(ResourceType.LINK)
                .orderIndex(orderIndex)
                .build();
    }

    private void ensureQuiz(Lesson lesson) {
        if (quizRepository.existsByLessonId(lesson.getId())) {
            return;
        }
        Quiz quiz = Quiz.builder()
                .lesson(lesson)
                .title("Access control quiz")
                .description("Validate that course content is only exposed to eligible learners.")
                .passScore(70)
                .timeLimitMins(10)
                .maxAttempts(3)
                .shuffleQuestions(false)
                .build();
        Question question = Question.builder()
                .quiz(quiz)
                .content("Which condition is required before a learner can open a protected lesson?")
                .type(QuestionType.SINGLE)
                .points(10)
                .orderIndex(1)
                .explanation("Protected content requires an active or completed enrollment.")
                .build();
        question.getAnswerOptions().add(answer(question, "The course is in REVIEWING status", false, 1));
        question.getAnswerOptions().add(answer(question, "The learner has ACTIVE or COMPLETED enrollment", true, 2));
        question.getAnswerOptions().add(answer(question, "The lesson has any title", false, 3));
        quiz.getQuestions().add(question);
        quizRepository.save(quiz);
    }

    private AnswerOption answer(Question question, String content, boolean correct, int orderIndex) {
        return AnswerOption.builder()
                .question(question)
                .content(content)
                .correct(correct)
                .orderIndex(orderIndex)
                .build();
    }

    private void ensureAssignment(Lesson lesson) {
        if (assignmentRepository.existsByLessonId(lesson.getId())) {
            return;
        }
        assignmentRepository.save(Assignment.builder()
                .lesson(lesson)
                .title("Payment retry implementation note")
                .instructions("Describe how failed payment callbacks should keep orders retryable and idempotent.")
                .maxScore(100)
                .dueDays(7)
                .build());
    }

    private void ensureLearnerEnrollment(User learner, Course course) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(learner.getId(), course.getId())
                .orElseGet(() -> enrollmentRepository.save(Enrollment.builder()
                        .user(learner)
                        .course(course)
                        .status(EnrollmentStatus.ACTIVE)
                        .amountPaid(new BigDecimal("890000"))
                        .progressPercent(new BigDecimal("25.00"))
                        .build()));
        List<Lesson> lessons = lessonRepository.findPublishedLessonsByCourseId(course.getId());
        for (int i = 0; i < lessons.size(); i++) {
            Lesson lesson = lessons.get(i);
            if (lessonProgressRepository.findByEnrollmentIdAndLessonId(enrollment.getId(), lesson.getId()).isPresent()) {
                continue;
            }
            boolean completed = i == 0;
            lessonProgressRepository.save(LessonProgress.builder()
                    .enrollment(enrollment)
                    .lesson(lesson)
                    .watchedSeconds(completed ? lesson.getDurationSeconds() : 0)
                    .completed(completed)
                    .completedAt(completed ? LocalDateTime.now().minusDays(1) : null)
                    .lastAccessedAt(LocalDateTime.now())
                    .build());
        }
    }

    private void ensureDevCoupon() {
        if (couponRepository.findByCodeIgnoreCase(DEV_COUPON_CODE).isPresent()) {
            return;
        }
        couponRepository.save(Coupon.builder()
                .code(DEV_COUPON_CODE)
                .name("Skillora UAT 20 percent off")
                .discountType(DiscountType.PERCENT)
                .discountValue(new BigDecimal("20.00"))
                .maxUses(100)
                .usedCount(0)
                .minOrderAmount(new BigDecimal("100000"))
                .startsAt(LocalDateTime.now().minusDays(1))
                .expiresAt(LocalDateTime.now().plusMonths(3))
                .active(true)
                .build());
    }

    private void addRequirements(Course course, List<String> requirements) {
        for (int i = 0; i < requirements.size(); i++) {
            course.getRequirements().add(CourseRequirement.builder()
                    .course(course)
                    .description(requirements.get(i))
                    .orderIndex(i + 1)
                    .build());
        }
    }

    private void addOutcomes(Course course, List<String> outcomes) {
        for (int i = 0; i < outcomes.size(); i++) {
            course.getOutcomes().add(CourseOutcome.builder()
                    .course(course)
                    .description(outcomes.get(i))
                    .orderIndex(i + 1)
                    .build());
        }
    }
}
