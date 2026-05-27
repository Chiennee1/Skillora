package com.example.skillora_platform.course.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.common.SlugUtils;
import com.example.skillora_platform.course.dto.LessonCreateRequest;
import com.example.skillora_platform.course.dto.LessonResourceCreateRequest;
import com.example.skillora_platform.course.dto.LessonResourceResponse;
import com.example.skillora_platform.course.dto.LessonResourceUpdateRequest;
import com.example.skillora_platform.course.dto.LessonResponse;
import com.example.skillora_platform.course.dto.LessonUpdateRequest;
import com.example.skillora_platform.course.dto.LessonVideoResponse;
import com.example.skillora_platform.course.entity.Course;
import com.example.skillora_platform.course.entity.CourseStatus;
import com.example.skillora_platform.course.entity.Lesson;
import com.example.skillora_platform.course.entity.LessonResource;
import com.example.skillora_platform.course.entity.LessonType;
import com.example.skillora_platform.course.entity.LessonVideo;
import com.example.skillora_platform.course.entity.ResourceType;
import com.example.skillora_platform.course.entity.Section;
import com.example.skillora_platform.course.repository.LessonRepository;
import com.example.skillora_platform.course.repository.LessonResourceRepository;
import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.exception.ResourceNotFoundException;
import com.example.skillora_platform.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final SectionService sectionService;
    private final CoursePermissionService permissionService;
    private final CourseTotalsService courseTotalsService;
    private final LessonRepository lessonRepository;
    private final LessonResourceRepository lessonResourceRepository;

    @Transactional
    public LessonResponse create(Long sectionId, LessonCreateRequest request, String actorEmail) {
        Section section = sectionService.findActiveSection(sectionId);
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        permissionService.requireOwnerOrAdmin(section.getCourse(), actor);
        int orderIndex = defaultInt(request.getOrderIndex());
        ensureUniqueOrder(sectionId, orderIndex, null);

        Lesson lesson = Lesson.builder()
                .section(section)
                .title(request.getTitle().trim())
                .slug(generateUniqueLessonSlug(sectionId, request.getTitle(), null))
                .type(defaultLessonType(request.getType()))
                .content(trimToNull(request.getContent()))
                .durationSeconds(defaultInt(request.getDurationSeconds()))
                .preview(Boolean.TRUE.equals(request.getPreview()))
                .published(request.getPublished() == null || request.getPublished())
                .orderIndex(orderIndex)
                .build();
        Lesson savedLesson = lessonRepository.save(lesson);
        courseTotalsService.refreshTotals(section.getCourse().getId());
        return toResponse(savedLesson, true);
    }

    @Transactional(readOnly = true)
    public LessonResponse get(Long id, String actorEmail) {
        Lesson lesson = findActiveLesson(id);
        Course course = lesson.getSection().getCourse();
        if (isPublicPreviewLesson(lesson)) {
            return toResponse(lesson, false);
        }

        if (actorEmail == null || actorEmail.isBlank()) {
            throw new BusinessException("Authentication required", HttpStatus.UNAUTHORIZED);
        }

        User actor = permissionService.requireActor(actorEmail);
        permissionService.requireOwnerOrAdmin(course, actor);
        return toResponse(lesson, true);
    }

    @Transactional
    public LessonResponse update(Long id, LessonUpdateRequest request, String actorEmail) {
        Lesson lesson = findActiveLesson(id);
        Course course = lesson.getSection().getCourse();
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        permissionService.requireOwnerOrAdmin(course, actor);
        int orderIndex = defaultInt(request.getOrderIndex());
        ensureUniqueOrder(lesson.getSection().getId(), orderIndex, id);

        lesson.setTitle(request.getTitle().trim());
        lesson.setSlug(generateUniqueLessonSlug(lesson.getSection().getId(), request.getTitle(), id));
        lesson.setType(defaultLessonType(request.getType()));
        lesson.setContent(trimToNull(request.getContent()));
        lesson.setDurationSeconds(defaultInt(request.getDurationSeconds()));
        lesson.setPreview(Boolean.TRUE.equals(request.getPreview()));
        lesson.setPublished(request.getPublished() == null || request.getPublished());
        lesson.setOrderIndex(orderIndex);
        Lesson savedLesson = lessonRepository.save(lesson);
        courseTotalsService.refreshTotals(course.getId());
        return toResponse(savedLesson, true);
    }

    @Transactional
    public void delete(Long id, String actorEmail) {
        Lesson lesson = findActiveLesson(id);
        Course course = lesson.getSection().getCourse();
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        permissionService.requireOwnerOrAdmin(course, actor);

        lesson.setDeletedAt(LocalDateTime.now());
        lessonRepository.save(lesson);
        courseTotalsService.refreshTotals(course.getId());
    }

    @Transactional
    public LessonResourceResponse addResource(
            Long lessonId,
            LessonResourceCreateRequest request,
            String actorEmail
    ) {
        Lesson lesson = findActiveLesson(lessonId);
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        permissionService.requireOwnerOrAdmin(lesson.getSection().getCourse(), actor);

        LessonResource resource = LessonResource.builder()
                .lesson(lesson)
                .name(request.getName().trim())
                .fileUrl(request.getFileUrl().trim())
                .resourceType(request.getResourceType())
                .sizeBytes(request.getSizeBytes())
                .orderIndex(defaultInt(request.getOrderIndex()))
                .build();
        return toResourceResponse(lessonResourceRepository.save(resource));
    }

    @Transactional
    public LessonResourceResponse updateResource(
            Long id,
            LessonResourceUpdateRequest request,
            String actorEmail
    ) {
        LessonResource resource = findResource(id);
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        permissionService.requireOwnerOrAdmin(resource.getLesson().getSection().getCourse(), actor);

        resource.setName(request.getName().trim());
        resource.setFileUrl(request.getFileUrl().trim());
        resource.setResourceType(request.getResourceType());
        resource.setSizeBytes(request.getSizeBytes());
        resource.setOrderIndex(defaultInt(request.getOrderIndex()));
        return toResourceResponse(lessonResourceRepository.save(resource));
    }

    @Transactional
    public void deleteResource(Long id, String actorEmail) {
        LessonResource resource = findResource(id);
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        permissionService.requireOwnerOrAdmin(resource.getLesson().getSection().getCourse(), actor);
        lessonResourceRepository.delete(resource);
    }

    @Transactional(readOnly = true)
    public Lesson findActiveLesson(Long id) {
        return lessonRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + id));
    }

    public LessonResponse toResponse(Lesson lesson, boolean includeProtectedContent) {
        return LessonResponse.builder()
                .id(lesson.getId())
                .sectionId(lesson.getSection().getId())
                .courseId(lesson.getSection().getCourse().getId())
                .title(lesson.getTitle())
                .slug(lesson.getSlug())
                .type(lesson.getType())
                .content(includeProtectedContent || lesson.isPreview() ? lesson.getContent() : null)
                .durationSeconds(lesson.getDurationSeconds())
                .preview(lesson.isPreview())
                .published(lesson.isPublished())
                .orderIndex(lesson.getOrderIndex())
                .video(toVideoResponse(lesson.getVideo()))
                .resources(includeProtectedContent ? toResourceResponses(lesson.getId()) : List.of())
                .deletedAt(lesson.getDeletedAt())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }

    private boolean isPublicPreviewLesson(Lesson lesson) {
        Section section = lesson.getSection();
        Course course = section.getCourse();
        return lesson.isPreview()
                && lesson.isPublished()
                && section.isPublished()
                && course.getStatus() == CourseStatus.PUBLISHED
                && course.getDeletedAt() == null;
    }

    private LessonResource findResource(Long id) {
        return lessonResourceRepository.findByIdAndLessonDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson resource not found with id: " + id));
    }

    private void ensureUniqueOrder(Long sectionId, int orderIndex, Long currentId) {
        boolean exists = currentId == null
                ? lessonRepository.existsBySectionIdAndOrderIndex(sectionId, orderIndex)
                : lessonRepository.existsBySectionIdAndOrderIndexAndIdNot(sectionId, orderIndex, currentId);
        if (exists) {
            throw new BusinessException("Lesson order index already exists in this section", HttpStatus.CONFLICT);
        }
    }

    private String generateUniqueLessonSlug(Long sectionId, String source, Long currentId) {
        String baseSlug = SlugUtils.toSlug(source);
        String candidate = baseSlug;
        int suffix = 2;
        while (currentId == null
                ? lessonRepository.existsBySectionIdAndSlugAndDeletedAtIsNull(sectionId, candidate)
                : lessonRepository.existsBySectionIdAndSlugAndDeletedAtIsNullAndIdNot(sectionId, candidate,
                        currentId)) {
            candidate = baseSlug + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private LessonVideoResponse toVideoResponse(LessonVideo video) {
        if (video == null) {
            return null;
        }
        return LessonVideoResponse.builder()
                .id(video.getId())
                .provider(video.getProvider())
                .assetId(video.getAssetId())
                .playbackUrl(video.getPlaybackUrl())
                .hlsUrl(video.getHlsUrl())
                .thumbnailUrl(video.getThumbnailUrl())
                .durationSeconds(video.getDurationSeconds())
                .sizeBytes(video.getSizeBytes())
                .mimeType(video.getMimeType())
                .status(video.getStatus())
                .errorMessage(video.getErrorMessage())
                .createdAt(video.getCreatedAt())
                .updatedAt(video.getUpdatedAt())
                .build();
    }

    private List<LessonResourceResponse> toResourceResponses(Long lessonId) {
        return lessonResourceRepository.findByLessonIdOrderByOrderIndexAscIdAsc(lessonId).stream()
                .map(this::toResourceResponse)
                .toList();
    }

    private LessonResourceResponse toResourceResponse(LessonResource resource) {
        return LessonResourceResponse.builder()
                .id(resource.getId())
                .lessonId(resource.getLesson().getId())
                .name(resource.getName())
                .fileUrl(resource.getFileUrl())
                .resourceType(resource.getResourceType() == null ? ResourceType.OTHER : resource.getResourceType())
                .sizeBytes(resource.getSizeBytes())
                .orderIndex(resource.getOrderIndex())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .build();
    }

    private LessonType defaultLessonType(LessonType type) {
        return type == null ? LessonType.VIDEO : type;
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
