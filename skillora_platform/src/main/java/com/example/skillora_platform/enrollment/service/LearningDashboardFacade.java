package com.example.skillora_platform.enrollment.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.course.service.CoursePermissionService;
import com.example.skillora_platform.enrollment.dto.EnrollmentResponse;
import com.example.skillora_platform.enrollment.dto.LearningDashboardResponse;
import com.example.skillora_platform.enrollment.entity.Enrollment;
import com.example.skillora_platform.enrollment.entity.EnrollmentStatus;
import com.example.skillora_platform.enrollment.repository.CourseCertificateRepository;
import com.example.skillora_platform.enrollment.repository.EnrollmentRepository;
import com.example.skillora_platform.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LearningDashboardFacade {

    private static final int RECENT_LIMIT = 5;

    private final EnrollmentRepository enrollmentRepository;
    private final CourseCertificateRepository courseCertificateRepository;
    private final CoursePermissionService permissionService;

    @Transactional(readOnly = true)
    public LearningDashboardResponse getDashboard(String actorEmail) {
        User actor = permissionService.requireActor(actorEmail);
        Long userId = actor.getId();

        long totalEnrolled = enrollmentRepository.countByUserId(userId);
        long inProgress = enrollmentRepository.countByUserIdAndStatus(userId, EnrollmentStatus.ACTIVE);
        long completed = enrollmentRepository.countByUserIdAndStatus(userId, EnrollmentStatus.COMPLETED);

        List<Enrollment> recentEnrollments = enrollmentRepository
                .findByUserIdOrderByEnrolledAtDesc(userId, PageRequest.of(0, RECENT_LIMIT))
                .getContent();

        List<EnrollmentResponse> recent = recentEnrollments.stream()
                .map(this::toResponse)
                .toList();

        return LearningDashboardResponse.builder()
                .totalEnrolled(totalEnrolled)
                .inProgress(inProgress)
                .completed(completed)
                .certificatesEarned(completed)
                .recentEnrollments(recent)
                .build();
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUser().getId())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .courseSlug(enrollment.getCourse().getSlug())
                .courseThumbnailUrl(enrollment.getCourse().getThumbnailUrl())
                .courseLevel(enrollment.getCourse().getLevel() != null
                        ? enrollment.getCourse().getLevel().name() : null)
                .instructorName(enrollment.getCourse().getInstructor().getFullName())
                .status(enrollment.getStatus())
                .amountPaid(enrollment.getAmountPaid())
                .progressPercent(enrollment.getProgressPercent())
                .enrolledAt(enrollment.getEnrolledAt())
                .completedAt(enrollment.getCompletedAt())
                .expiresAt(enrollment.getExpiresAt())
                .build();
    }
}
