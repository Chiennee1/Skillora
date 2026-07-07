package com.example.skillora_platform.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.skillora_platform.assignment.repository.AssignmentRepository;
import com.example.skillora_platform.commerce.repository.CouponRepository;
import com.example.skillora_platform.course.entity.Course;
import com.example.skillora_platform.course.entity.CourseStatus;
import com.example.skillora_platform.course.repository.CategoryRepository;
import com.example.skillora_platform.course.repository.CourseRepository;
import com.example.skillora_platform.course.repository.LessonRepository;
import com.example.skillora_platform.course.repository.SectionRepository;
import com.example.skillora_platform.enrollment.repository.EnrollmentRepository;
import com.example.skillora_platform.enrollment.repository.LessonProgressRepository;
import com.example.skillora_platform.quiz.repository.QuizRepository;
import com.example.skillora_platform.user.entity.User;
import com.example.skillora_platform.user.repository.InstructorProfileRepository;
import com.example.skillora_platform.user.repository.UserProfileRepository;
import com.example.skillora_platform.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "skillora.seed.enabled=true",
                "skillora.seed.password=Skillora@12345",
                "spring.datasource.url=jdbc:h2:mem:skillora_seed_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
                "spring.jpa.properties.hibernate.format_sql=false",
                "spring.jpa.show-sql=false",
                "spring.flyway.enabled=false",
                "spring.cache.type=simple",
                "management.health.redis.enabled=false",
                "management.health.mail.enabled=false",
                "debug=false",
                "logging.level.root=INFO",
                "logging.level.com.example.skillora_platform=INFO",
                "logging.level.org.springframework=WARN",
                "logging.level.org.hibernate.SQL=WARN"
        }
)
@ActiveProfiles({"test", "dev"})
class DevDataBootstrapIntegrationTest {

    @Autowired private DevDataBootstrap devDataBootstrap;
    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private InstructorProfileRepository instructorProfileRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private SectionRepository sectionRepository;
    @Autowired private LessonRepository lessonRepository;
    @Autowired private QuizRepository quizRepository;
    @Autowired private AssignmentRepository assignmentRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;
    @Autowired private LessonProgressRepository lessonProgressRepository;
    @Autowired private CouponRepository couponRepository;

    @Test
    void shouldCreateUatSeedDataAndRemainIdempotent() {
        assertSeedData();

        long users = userRepository.count();
        long categories = categoryRepository.count();
        long courses = courseRepository.count();
        long enrollments = enrollmentRepository.count();
        long progressRecords = lessonProgressRepository.count();
        long coupons = couponRepository.count();

        devDataBootstrap.run();

        assertSeedData();
        assertThat(userRepository.count()).isEqualTo(users);
        assertThat(categoryRepository.count()).isEqualTo(categories);
        assertThat(courseRepository.count()).isEqualTo(courses);
        assertThat(enrollmentRepository.count()).isEqualTo(enrollments);
        assertThat(lessonProgressRepository.count()).isEqualTo(progressRecords);
        assertThat(couponRepository.count()).isEqualTo(coupons);
    }

    private void assertSeedData() {
        User admin = userRepository.findByEmailIgnoreCase("admin@skillora.test").orElseThrow();
        User instructor = userRepository.findByEmailIgnoreCase("instructor@skillora.test").orElseThrow();
        User learner = userRepository.findByEmailIgnoreCase("learner@skillora.test").orElseThrow();

        assertThat(admin.getRoles()).extracting(role -> role.getName().name()).containsExactly("ADMIN");
        assertThat(instructor.getRoles()).extracting(role -> role.getName().name()).containsExactly("INSTRUCTOR");
        assertThat(learner.getRoles()).extracting(role -> role.getName().name()).containsExactly("STUDENT");
        assertThat(userProfileRepository.findByUserId(admin.getId())).isPresent();
        assertThat(userProfileRepository.findByUserId(instructor.getId())).isPresent();
        assertThat(userProfileRepository.findByUserId(learner.getId())).isPresent();
        assertThat(instructorProfileRepository.findByUserId(instructor.getId())).isPresent();

        Course published = courseRepository.findBySlugAndDeletedAtIsNull("spring-boot-production-apis").orElseThrow();
        Course reviewing = courseRepository.findBySlugAndDeletedAtIsNull("kubernetes-deployment-playbook").orElseThrow();
        assertThat(published.getStatus()).isEqualTo(CourseStatus.PUBLISHED);
        assertThat(reviewing.getStatus()).isEqualTo(CourseStatus.REVIEWING);
        assertThat(published.getTotalLessons()).isEqualTo(5);
        assertThat(published.getTotalDurationSeconds()).isEqualTo(8700);
        assertThat(sectionRepository.findByCourseIdAndDeletedAtIsNullOrderByOrderIndexAscIdAsc(published.getId()))
                .hasSize(3);
        assertThat(lessonRepository.findBySectionCourseIdAndDeletedAtIsNull(published.getId())).hasSize(5);
        assertThat(lessonRepository.findPublishedLessonsByCourseId(published.getId())).hasSize(4);
        assertThat(quizRepository.findByLessonId(lessonRepository.findPublishedLessonsByCourseId(published.getId()).get(2).getId()))
                .isPresent();
        assertThat(assignmentRepository.findByLessonId(lessonRepository.findPublishedLessonsByCourseId(published.getId()).get(3).getId()))
                .isPresent();
        assertThat(enrollmentRepository.findByUserIdAndCourseId(learner.getId(), published.getId())).isPresent();
        assertThat(lessonProgressRepository.countByEnrollmentId(
                enrollmentRepository.findByUserIdAndCourseId(learner.getId(), published.getId()).orElseThrow().getId()))
                .isEqualTo(4);
        assertThat(couponRepository.findByCodeIgnoreCase("SKILLORA20")).isPresent();
    }
}
