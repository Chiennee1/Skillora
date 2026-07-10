package com.example.skillora_platform.course.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.admin.service.AuditLogService;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.common.PageResponse;
import com.example.skillora_platform.course.dto.CourseVersionResponse;
import com.example.skillora_platform.course.dto.CourseVersionUpdateRequest;
import com.example.skillora_platform.course.entity.Category;
import com.example.skillora_platform.course.entity.Course;
import com.example.skillora_platform.course.entity.CourseOutcome;
import com.example.skillora_platform.course.entity.CourseRequirement;
import com.example.skillora_platform.course.entity.CourseStatus;
import com.example.skillora_platform.course.entity.CourseVersion;
import com.example.skillora_platform.course.entity.CourseVersionStatus;
import com.example.skillora_platform.course.entity.Lesson;
import com.example.skillora_platform.course.entity.LessonResource;
import com.example.skillora_platform.course.entity.LessonVideo;
import com.example.skillora_platform.course.entity.Section;
import com.example.skillora_platform.course.repository.CourseRepository;
import com.example.skillora_platform.course.repository.CourseVersionRepository;
import com.example.skillora_platform.course.repository.LessonRepository;
import com.example.skillora_platform.course.repository.LessonResourceRepository;
import com.example.skillora_platform.course.repository.SectionRepository;
import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.exception.ResourceNotFoundException;
import com.example.skillora_platform.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseVersionService {

    private static final Set<CourseVersionStatus> ACTIVE_VERSION_STATUSES = Set.of(
            CourseVersionStatus.DRAFT,
            CourseVersionStatus.REVIEWING
    );

    private final CourseRepository courseRepository;
    private final CourseVersionRepository courseVersionRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final LessonResourceRepository lessonResourceRepository;
    private final CoursePermissionService permissionService;
    private final CoursePublishingReadinessService publishingReadinessService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Transactional
    public CourseVersionResponse createDraftVersion(Long courseId, String actorEmail) {
        Course course = findActiveCourse(courseId);
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        permissionService.requireOwnerOrAdmin(course, actor);

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BusinessException("Only PUBLISHED courses can create a new version", HttpStatus.CONFLICT);
        }
        if (courseVersionRepository.existsByCourseIdAndStatusIn(courseId, ACTIVE_VERSION_STATUSES)) {
            throw new BusinessException("Course already has a DRAFT or REVIEWING version", HttpStatus.CONFLICT);
        }

        int nextVersion = courseVersionRepository.findFirstByCourseIdOrderByVersionNumberDesc(courseId)
                .map(version -> version.getVersionNumber() + 1)
                .orElse(course.getCurrentVersion() == null ? 1 : course.getCurrentVersion() + 1);
        CourseVersion version = CourseVersion.builder()
                .course(course)
                .versionNumber(nextVersion)
                .status(CourseVersionStatus.DRAFT)
                .title(course.getTitle())
                .subtitle(course.getSubtitle())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .snapshotJson(snapshotJson(course, course.getTitle(), course.getSubtitle(),
                        course.getDescription(), course.getThumbnailUrl()))
                .build();
        return toResponse(courseVersionRepository.save(version));
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseVersionResponse> listCourseVersions(
            Long courseId,
            String actorEmail,
            int page,
            int size
    ) {
        Course course = findActiveCourse(courseId);
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        permissionService.requireOwnerOrAdmin(course, actor);
        Page<CourseVersionResponse> result = courseVersionRepository.findByCourseId(courseId, pageable(page, size))
                .map(this::toResponse);
        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseVersionResponse> listPendingReviews(String adminEmail, int page, int size) {
        requireAdmin(adminEmail);
        Page<CourseVersionResponse> result = courseVersionRepository
                .findByStatus(CourseVersionStatus.REVIEWING, pageable(page, size))
                .map(this::toResponse);
        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public CourseVersionResponse getVersion(Long courseId, Long versionId, String actorEmail) {
        CourseVersion version = findVersion(courseId, versionId);
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        permissionService.requireOwnerOrAdmin(version.getCourse(), actor);
        return toResponse(version);
    }

    @Transactional
    public CourseVersionResponse updateDraftVersion(
            Long courseId,
            Long versionId,
            CourseVersionUpdateRequest request,
            String actorEmail
    ) {
        CourseVersion version = findVersion(courseId, versionId);
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        permissionService.requireOwnerOrAdmin(version.getCourse(), actor);
        if (version.getStatus() != CourseVersionStatus.DRAFT) {
            throw new BusinessException("Only DRAFT versions can be edited", HttpStatus.CONFLICT);
        }

        version.setTitle(request.getTitle().trim());
        version.setSubtitle(trimToNull(request.getSubtitle()));
        version.setDescription(trimToNull(request.getDescription()));
        version.setThumbnailUrl(trimToNull(request.getThumbnailUrl()));
        version.setRejectReason(null);
        version.setSnapshotJson(snapshotJson(version.getCourse(), version.getTitle(), version.getSubtitle(),
                version.getDescription(), version.getThumbnailUrl()));
        return toResponse(courseVersionRepository.save(version));
    }

    @Transactional
    public CourseVersionResponse submitVersionForReview(Long courseId, Long versionId, String actorEmail) {
        CourseVersion version = findVersion(courseId, versionId);
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        permissionService.requireOwnerOrAdmin(version.getCourse(), actor);
        if (version.getStatus() == CourseVersionStatus.REVIEWING) {
            return toResponse(version);
        }
        if (version.getStatus() != CourseVersionStatus.DRAFT) {
            throw new BusinessException("Only DRAFT versions can be submitted for review", HttpStatus.CONFLICT);
        }
        publishingReadinessService.requirePublishedVideoLessonsReady(version.getCourse().getId());

        version.setStatus(CourseVersionStatus.REVIEWING);
        version.setRejectReason(null);
        return toResponse(courseVersionRepository.save(version));
    }

    @Transactional
    @CacheEvict(cacheNames = {
            Constants.CACHE_COURSES_PUBLISHED,
            Constants.CACHE_COURSE_DETAIL
    }, allEntries = true)
    public CourseVersionResponse approveVersion(
            Long courseId,
            Long versionId,
            String adminEmail,
            String ipAddress
    ) {
        requireAdmin(adminEmail);
        CourseVersion version = findVersion(courseId, versionId);
        if (version.getStatus() != CourseVersionStatus.REVIEWING) {
            throw new BusinessException("Only REVIEWING versions can be approved", HttpStatus.CONFLICT);
        }

        Course course = version.getCourse();
        String oldValues = auditJson(course);
        course.setTitle(version.getTitle());
        course.setSubtitle(version.getSubtitle());
        course.setDescription(version.getDescription());
        course.setThumbnailUrl(version.getThumbnailUrl());
        course.setCurrentVersion(version.getVersionNumber());
        course.setStatus(CourseStatus.PUBLISHED);
        course.setRejectReason(null);
        if (course.getPublishedAt() == null) {
            course.setPublishedAt(LocalDateTime.now());
        }
        courseRepository.save(course);

        version.setStatus(CourseVersionStatus.APPROVED);
        version.setRejectReason(null);
        CourseVersion saved = courseVersionRepository.save(version);
        auditLogService.log(adminEmail, "COURSE_VERSION", version.getId(), "APPROVE_COURSE_VERSION",
                oldValues, auditJson(course), ipAddress, null);
        return toResponse(saved);
    }

    @Transactional
    public CourseVersionResponse rejectVersion(
            Long courseId,
            Long versionId,
            String reason,
            String adminEmail,
            String ipAddress
    ) {
        requireAdmin(adminEmail);
        CourseVersion version = findVersion(courseId, versionId);
        if (version.getStatus() != CourseVersionStatus.REVIEWING) {
            throw new BusinessException("Only REVIEWING versions can be rejected", HttpStatus.CONFLICT);
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("Reject reason is required", HttpStatus.BAD_REQUEST);
        }

        version.setStatus(CourseVersionStatus.REJECTED);
        version.setRejectReason(reason.trim());
        CourseVersion saved = courseVersionRepository.save(version);
        auditLogService.log(adminEmail, "COURSE_VERSION", version.getId(), "REJECT_COURSE_VERSION",
                null, "{\"reason\":\"" + escapeJson(reason.trim()) + "\"}", ipAddress, null);
        return toResponse(saved);
    }

    private Course findActiveCourse(Long courseId) {
        return courseRepository.findByIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
    }

    private CourseVersion findVersion(Long courseId, Long versionId) {
        return courseVersionRepository.findByCourseIdAndId(courseId, versionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course version not found with id: " + versionId));
    }

    private void requireAdmin(String adminEmail) {
        User actor = permissionService.requireActor(adminEmail);
        if (!permissionService.isAdmin(actor)) {
            throw new BusinessException("Admin role is required", HttpStatus.FORBIDDEN);
        }
    }

    private PageRequest pageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), Constants.MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "updatedAt"));
    }

    private String snapshotJson(
            Course course,
            String title,
            String subtitle,
            String description,
            String thumbnailUrl
    ) {
        CourseSnapshot snapshot = new CourseSnapshot(
                course.getId(),
                title,
                subtitle,
                description,
                thumbnailUrl,
                course.getPreviewVideoUrl(),
                course.getLevel() == null ? null : course.getLevel().name(),
                course.getLanguage(),
                course.getPrice(),
                course.getDiscountPrice(),
                course.getCurrency(),
                course.getCategories().stream()
                        .sorted(Comparator.comparing(Category::getOrderIndex).thenComparing(Category::getName))
                        .map(this::snapshotCategory)
                        .toList(),
                course.getRequirements().stream()
                        .sorted(Comparator.comparingInt(CourseRequirement::getOrderIndex))
                        .map(CourseRequirement::getDescription)
                        .toList(),
                course.getOutcomes().stream()
                        .sorted(Comparator.comparingInt(CourseOutcome::getOrderIndex))
                        .map(CourseOutcome::getDescription)
                        .toList(),
                sectionRepository.findByCourseIdAndDeletedAtIsNullOrderByOrderIndexAscIdAsc(course.getId()).stream()
                        .map(this::snapshotSection)
                        .toList()
        );
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Could not create course version snapshot", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private SectionSnapshot snapshotSection(Section section) {
        return new SectionSnapshot(
                section.getId(),
                section.getTitle(),
                section.getDescription(),
                section.getOrderIndex(),
                section.isPublished(),
                lessonRepository.findBySectionIdAndDeletedAtIsNullOrderByOrderIndexAscIdAsc(section.getId()).stream()
                        .map(this::snapshotLesson)
                        .toList()
        );
    }

    private Map<String, Object> snapshotCategory(Category category) {
        return Map.of(
                "id", category.getId(),
                "name", category.getName(),
                "slug", category.getSlug()
        );
    }

    private LessonSnapshot snapshotLesson(Lesson lesson) {
        LessonVideo video = lesson.getVideo();
        return new LessonSnapshot(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getSlug(),
                lesson.getType() == null ? null : lesson.getType().name(),
                lesson.getContent(),
                lesson.getDurationSeconds(),
                lesson.isPreview(),
                lesson.isPublished(),
                lesson.getOrderIndex(),
                video == null ? null : Map.of(
                        "id", video.getId(),
                        "provider", video.getProvider() == null ? "" : video.getProvider().name(),
                        "assetId", nullToEmpty(video.getAssetId()),
                        "playbackUrl", nullToEmpty(video.getPlaybackUrl()),
                        "hlsUrl", nullToEmpty(video.getHlsUrl()),
                        "thumbnailUrl", nullToEmpty(video.getThumbnailUrl()),
                        "status", video.getStatus() == null ? "" : video.getStatus().name()
                ),
                lessonResourceRepository.findByLessonIdOrderByOrderIndexAscIdAsc(lesson.getId()).stream()
                        .map(this::snapshotResource)
                        .toList()
        );
    }

    private Map<String, Object> snapshotResource(LessonResource resource) {
        return Map.of(
                "id", resource.getId(),
                "name", resource.getName(),
                "fileUrl", resource.getFileUrl(),
                "resourceType", resource.getResourceType() == null ? "" : resource.getResourceType().name(),
                "sizeBytes", resource.getSizeBytes() == null ? 0 : resource.getSizeBytes(),
                "orderIndex", resource.getOrderIndex()
        );
    }

    private String auditJson(Course course) {
        return "{\"title\":\"" + escapeJson(course.getTitle()) + "\",\"currentVersion\":"
                + course.getCurrentVersion() + "}";
    }

    private CourseVersionResponse toResponse(CourseVersion version) {
        return CourseVersionResponse.builder()
                .id(version.getId())
                .courseId(version.getCourse().getId())
                .versionNumber(version.getVersionNumber())
                .status(version.getStatus())
                .title(version.getTitle())
                .subtitle(version.getSubtitle())
                .description(version.getDescription())
                .thumbnailUrl(version.getThumbnailUrl())
                .rejectReason(version.getRejectReason())
                .snapshotJson(version.getSnapshotJson())
                .createdAt(version.getCreatedAt())
                .updatedAt(version.getUpdatedAt())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private record CourseSnapshot(
            Long id,
            String title,
            String subtitle,
            String description,
            String thumbnailUrl,
            String previewVideoUrl,
            String level,
            String language,
            java.math.BigDecimal price,
            java.math.BigDecimal discountPrice,
            String currency,
            List<Map<String, Object>> categories,
            List<String> requirements,
            List<String> outcomes,
            List<SectionSnapshot> sections
    ) {
    }

    private record SectionSnapshot(
            Long id,
            String title,
            String description,
            int orderIndex,
            boolean published,
            List<LessonSnapshot> lessons
    ) {
    }

    private record LessonSnapshot(
            Long id,
            String title,
            String slug,
            String type,
            String content,
            int durationSeconds,
            boolean preview,
            boolean published,
            int orderIndex,
            Map<String, Object> video,
            List<Map<String, Object>> resources
    ) {
    }
}
