package com.example.skillora_platform.enrollment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.course.entity.Course;
import com.example.skillora_platform.course.entity.Lesson;
import com.example.skillora_platform.course.repository.LessonRepository;
import com.example.skillora_platform.enrollment.dto.LessonProgressResponse;
import com.example.skillora_platform.enrollment.entity.CourseCertificate;
import com.example.skillora_platform.enrollment.entity.Enrollment;
import com.example.skillora_platform.enrollment.entity.EnrollmentStatus;
import com.example.skillora_platform.enrollment.entity.LessonProgress;
import com.example.skillora_platform.enrollment.repository.CourseCertificateRepository;
import com.example.skillora_platform.enrollment.repository.EnrollmentRepository;
import com.example.skillora_platform.enrollment.repository.LessonProgressRepository;
import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.exception.ResourceNotFoundException;
import com.example.skillora_platform.notification.event.CertificateIssuedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningProgressService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final LessonRepository lessonRepository;
    private final CourseCertificateRepository courseCertificateRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<LessonProgressResponse> getProgress(Long enrollmentId) {
        Enrollment enrollment = findEnrollment(enrollmentId);
        List<Lesson> publishedLessons = getPublishedLessons(enrollment.getCourse());

        return publishedLessons.stream()
                .map(lesson -> {
                    LessonProgress progress = lessonProgressRepository
                            .findByEnrollmentIdAndLessonId(enrollmentId, lesson.getId())
                            .orElse(null);
                    return toLessonProgressResponse(lesson, progress, enrollmentId);
                })
                .toList();
    }

    @Transactional
    public LessonProgressResponse updateProgress(
            Long enrollmentId,
            Long lessonId,
            Integer watchedSeconds,
            Boolean completed
    ) {
        Enrollment enrollment = findEnrollment(enrollmentId);
        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new BusinessException("Enrollment is not active", HttpStatus.BAD_REQUEST);
        }

        Lesson lesson = lessonRepository.findByIdAndDeletedAtIsNull(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        validateLessonBelongsToCourse(lesson, enrollment.getCourse());

        LessonProgress progress = lessonProgressRepository
                .findByEnrollmentIdAndLessonId(enrollmentId, lessonId)
                .orElseGet(() -> LessonProgress.builder()
                        .enrollment(enrollment)
                        .lesson(lesson)
                        .watchedSeconds(0)
                        .completed(false)
                        .build());

        if (watchedSeconds != null) {
            progress.setWatchedSeconds(Math.max(progress.getWatchedSeconds(), watchedSeconds));
        }

        progress.setLastAccessedAt(LocalDateTime.now());

        boolean nowCompleted = Boolean.TRUE.equals(completed)
                || (lesson.getDurationSeconds() > 0
                        && progress.getWatchedSeconds() >= (int) (lesson.getDurationSeconds() * 0.9));

        if (nowCompleted && !progress.isCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
        }

        LessonProgress savedProgress = lessonProgressRepository.save(progress);

        recalculateEnrollmentProgress(enrollment);

        return toLessonProgressResponse(lesson, savedProgress, enrollmentId);
    }

    private void recalculateEnrollmentProgress(Enrollment enrollment) {
        List<Lesson> publishedLessons = getPublishedLessons(enrollment.getCourse());
        if (publishedLessons.isEmpty()) {
            return;
        }

        long completedCount = lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(enrollment.getId());
        BigDecimal totalLessons = BigDecimal.valueOf(publishedLessons.size());
        BigDecimal progressPercent = BigDecimal.valueOf(completedCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(totalLessons, 2, RoundingMode.HALF_UP);
        enrollment.setProgressPercent(progressPercent);

        if (completedCount == publishedLessons.size() && enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setCompletedAt(LocalDateTime.now());
            log.info("Enrollment {} completed — course {} by user {}",
                    enrollment.getId(), enrollment.getCourse().getId(), enrollment.getUser().getId());
            generateCertificateIfAbsent(enrollment);
        }

        enrollmentRepository.save(enrollment);
    }

    private void generateCertificateIfAbsent(Enrollment enrollment) {
        if (courseCertificateRepository.existsByEnrollmentId(enrollment.getId())) {
            return;
        }
        CourseCertificate certificate = CourseCertificate.builder()
                .enrollment(enrollment)
                .certificateCode("CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();
        CourseCertificate savedCertificate = courseCertificateRepository.save(certificate);
        eventPublisher.publishEvent(new CertificateIssuedEvent(savedCertificate.getId()));
        log.info("Certificate {} generated for enrollment {}",
                savedCertificate.getCertificateCode(), enrollment.getId());
    }

    private List<Lesson> getPublishedLessons(Course course) {
        return lessonRepository.findPublishedLessonsByCourseId(course.getId());
    }

    private void validateLessonBelongsToCourse(Lesson lesson, Course course) {
        Long lessonCourseId = lesson.getSection().getCourse().getId();
        if (!lessonCourseId.equals(course.getId())) {
            throw new BusinessException("Lesson does not belong to the enrolled course", HttpStatus.BAD_REQUEST);
        }
    }

    private Enrollment findEnrollment(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId));
    }

    private LessonProgressResponse toLessonProgressResponse(
            Lesson lesson,
            LessonProgress progress,
            Long enrollmentId
    ) {
        return LessonProgressResponse.builder()
                .id(progress != null ? progress.getId() : null)
                .enrollmentId(enrollmentId)
                .lessonId(lesson.getId())
                .lessonTitle(lesson.getTitle())
                .watchedSeconds(progress != null ? progress.getWatchedSeconds() : 0)
                .totalDurationSeconds(lesson.getDurationSeconds())
                .completed(progress != null && progress.isCompleted())
                .completedAt(progress != null ? progress.getCompletedAt() : null)
                .lastAccessedAt(progress != null ? progress.getLastAccessedAt() : null)
                .build();
    }
}
