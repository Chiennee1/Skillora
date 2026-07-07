package com.example.skillora_platform.course.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.course.dto.LessonSummaryResponse;
import com.example.skillora_platform.course.dto.SectionCreateRequest;
import com.example.skillora_platform.course.dto.SectionResponse;
import com.example.skillora_platform.course.dto.SectionUpdateRequest;
import com.example.skillora_platform.course.entity.Course;
import com.example.skillora_platform.course.entity.CourseStatus;
import com.example.skillora_platform.course.entity.Lesson;
import com.example.skillora_platform.course.entity.Section;
import com.example.skillora_platform.course.repository.LessonRepository;
import com.example.skillora_platform.course.repository.SectionRepository;
import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.exception.ResourceNotFoundException;
import com.example.skillora_platform.assignment.repository.AssignmentRepository;
import com.example.skillora_platform.quiz.repository.QuizRepository;
import com.example.skillora_platform.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final CourseService courseService;
    private final CoursePermissionService permissionService;
    private final CourseTotalsService courseTotalsService;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final AssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public List<SectionResponse> listByCourse(Long courseId, String actorEmail) {
        Course course = courseService.findActiveCourse(courseId);
        boolean manageAccess = hasManageAccess(course, actorEmail);
        if (!manageAccess && course.getStatus() != CourseStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        return sectionRepository.findByCourseIdAndDeletedAtIsNullOrderByOrderIndexAscIdAsc(courseId).stream()
                .filter(section -> manageAccess || section.isPublished())
                .map(section -> toResponse(section, manageAccess))
                .toList();
    }

    @Transactional
    public SectionResponse create(Long courseId, SectionCreateRequest request, String actorEmail) {
        Course course = courseService.findActiveCourse(courseId);
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        permissionService.requireOwnerOrAdmin(course, actor);
        requireVersionFlowForPublishedCourse(course, actor);
        int orderIndex = defaultInt(request.getOrderIndex());
        ensureUniqueOrder(courseId, orderIndex, null);

        Section section = Section.builder()
                .course(course)
                .title(request.getTitle().trim())
                .description(trimToNull(request.getDescription()))
                .orderIndex(orderIndex)
                .published(request.getPublished() == null || request.getPublished())
                .build();
        return toResponse(sectionRepository.save(section), true);
    }

    @Transactional
    public SectionResponse update(Long id, SectionUpdateRequest request, String actorEmail) {
        Section section = findActiveSection(id);
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        permissionService.requireOwnerOrAdmin(section.getCourse(), actor);
        requireVersionFlowForPublishedCourse(section.getCourse(), actor);
        int orderIndex = defaultInt(request.getOrderIndex());
        ensureUniqueOrder(section.getCourse().getId(), orderIndex, id);

        section.setTitle(request.getTitle().trim());
        section.setDescription(trimToNull(request.getDescription()));
        section.setOrderIndex(orderIndex);
        section.setPublished(request.getPublished() == null || request.getPublished());
        return toResponse(sectionRepository.save(section), true);
    }

    @Transactional
    public void delete(Long id, String actorEmail) {
        Section section = findActiveSection(id);
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        permissionService.requireOwnerOrAdmin(section.getCourse(), actor);
        requireVersionFlowForPublishedCourse(section.getCourse(), actor);

        LocalDateTime now = LocalDateTime.now();
        section.setDeletedAt(now);
        lessonRepository.findBySectionIdAndDeletedAtIsNullOrderByOrderIndexAscIdAsc(section.getId())
                .forEach(lesson -> lesson.setDeletedAt(now));
        sectionRepository.save(section);
        courseTotalsService.refreshTotals(section.getCourse().getId());
    }

    @Transactional(readOnly = true)
    public Section findActiveSection(Long id) {
        return sectionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));
    }

    private boolean hasManageAccess(Course course, String actorEmail) {
        if (actorEmail == null || actorEmail.isBlank()) {
            return false;
        }
        try {
            User actor = permissionService.requireActor(actorEmail);
            return permissionService.canManage(course, actor);
        } catch (BusinessException ex) {
            return false;
        }
    }

    private void requireVersionFlowForPublishedCourse(Course course, User actor) {
        if (course.getStatus() == CourseStatus.PUBLISHED && !permissionService.isAdmin(actor)) {
            throw new BusinessException(
                    "Published courses must be changed through a course version draft",
                    HttpStatus.CONFLICT);
        }
    }

    private void ensureUniqueOrder(Long courseId, int orderIndex, Long currentId) {
        boolean exists = currentId == null
                ? sectionRepository.existsByCourseIdAndOrderIndex(courseId, orderIndex)
                : sectionRepository.existsByCourseIdAndOrderIndexAndIdNot(courseId, orderIndex, currentId);
        if (exists) {
            throw new BusinessException("Section order index already exists in this course", HttpStatus.CONFLICT);
        }
    }

    private SectionResponse toResponse(Section section, boolean includeUnpublishedLessons) {
        List<LessonSummaryResponse> lessons = lessonRepository
                .findBySectionIdAndDeletedAtIsNullOrderByOrderIndexAscIdAsc(section.getId()).stream()
                .filter(lesson -> includeUnpublishedLessons || lesson.isPublished())
                .map(this::toLessonSummary)
                .toList();
        return SectionResponse.builder()
                .id(section.getId())
                .courseId(section.getCourse().getId())
                .title(section.getTitle())
                .description(section.getDescription())
                .orderIndex(section.getOrderIndex())
                .published(section.isPublished())
                .lessons(lessons)
                .deletedAt(section.getDeletedAt())
                .createdAt(section.getCreatedAt())
                .updatedAt(section.getUpdatedAt())
                .build();
    }

    private LessonSummaryResponse toLessonSummary(Lesson lesson) {
        return LessonSummaryResponse.builder()
                .id(lesson.getId())
                .sectionId(lesson.getSection().getId())
                .quizId(quizId(lesson.getId()))
                .assignmentId(assignmentId(lesson.getId()))
                .title(lesson.getTitle())
                .slug(lesson.getSlug())
                .type(lesson.getType())
                .durationSeconds(lesson.getDurationSeconds())
                .preview(lesson.isPreview())
                .published(lesson.isPublished())
                .orderIndex(lesson.getOrderIndex())
                .build();
    }

    private Long quizId(Long lessonId) {
        return quizRepository.findByLessonId(lessonId)
                .map(quiz -> quiz.getId())
                .orElse(null);
    }

    private Long assignmentId(Long lessonId) {
        return assignmentRepository.findByLessonId(lessonId)
                .map(assignment -> assignment.getId())
                .orElse(null);
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
