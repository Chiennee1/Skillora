package com.example.skillora_platform.admin.service;

import java.time.LocalDateTime;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.admin.dto.AdminCourseResponse;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.common.PageResponse;
import com.example.skillora_platform.course.entity.Course;
import com.example.skillora_platform.course.entity.CourseStatus;
import com.example.skillora_platform.course.repository.CourseRepository;
import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCourseService {

    private final CourseRepository courseRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public PageResponse<AdminCourseResponse> listCourses(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), Constants.MAX_PAGE_SIZE);
        Page<AdminCourseResponse> result = courseRepository.findByDeletedAtIsNull(
                        PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::toResponse);
        return PageResponse.from(result);
    }

    @Transactional
    @CacheEvict(cacheNames = {
            Constants.CACHE_COURSES_PUBLISHED,
            Constants.CACHE_COURSE_DETAIL
    }, allEntries = true)
    public AdminCourseResponse approveCourse(Long id, String adminEmail, String ipAddress) {
        Course course = courseRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        if (course.getStatus() != CourseStatus.REVIEWING) {
            throw new BusinessException(
                    "Only courses in REVIEWING status can be approved, current: " + course.getStatus(),
                    HttpStatus.CONFLICT);
        }

        CourseStatus oldStatus = course.getStatus();
        course.setStatus(CourseStatus.PUBLISHED);
        course.setPublishedAt(LocalDateTime.now());
        course.setRejectReason(null);
        courseRepository.save(course);

        auditLogService.log(adminEmail, "COURSE", id, "APPROVE_COURSE",
                "{\"status\":\"" + oldStatus + "\"}",
                "{\"status\":\"PUBLISHED\"}",
                ipAddress, null);

        log.info("Admin {} approved course {} (was {})", adminEmail, id, oldStatus);
        return toResponse(course);
    }

    @Transactional
    @CacheEvict(cacheNames = {
            Constants.CACHE_COURSES_PUBLISHED,
            Constants.CACHE_COURSE_DETAIL
    }, allEntries = true)
    public AdminCourseResponse rejectCourse(Long id, String reason, String adminEmail, String ipAddress) {
        Course course = courseRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        if (course.getStatus() != CourseStatus.REVIEWING) {
            throw new BusinessException(
                    "Only courses in REVIEWING status can be rejected, current: " + course.getStatus(),
                    HttpStatus.CONFLICT);
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("Reject reason is required", HttpStatus.BAD_REQUEST);
        }

        String cleanReason = reason.trim();
        CourseStatus oldStatus = course.getStatus();
        course.setStatus(CourseStatus.REJECTED);
        course.setRejectReason(cleanReason);
        courseRepository.save(course);

        auditLogService.log(adminEmail, "COURSE", id, "REJECT_COURSE",
                "{\"status\":\"" + oldStatus + "\"}",
                "{\"status\":\"REJECTED\",\"rejectReason\":\"" + cleanReason + "\"}",
                ipAddress, null);

        log.info("Admin {} rejected course {} with reason: {}", adminEmail, id, cleanReason);
        return toResponse(course);
    }

    private AdminCourseResponse toResponse(Course course) {
        return AdminCourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .slug(course.getSlug())
                .status(course.getStatus().name())
                .level(course.getLevel().name())
                .price(course.getPrice())
                .currency(course.getCurrency())
                .rejectReason(course.getRejectReason())
                .totalLessons(course.getTotalLessons())
                .totalEnrollments(course.getTotalEnrollments())
                .avgRating(course.getAvgRating())
                .totalReviews(course.getTotalReviews())
                .instructorName(course.getInstructor().getFullName())
                .instructorEmail(course.getInstructor().getEmail())
                .publishedAt(course.getPublishedAt())
                .createdAt(course.getCreatedAt())
                .build();
    }
}
