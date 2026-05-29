package com.example.skillora_platform.enrollment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.enrollment.entity.Enrollment;
import com.example.skillora_platform.enrollment.entity.EnrollmentStatus;
import com.example.skillora_platform.enrollment.repository.EnrollmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LearningAccessService {

    private final EnrollmentRepository enrollmentRepository;

    @Transactional(readOnly = true)
    public boolean hasActiveEnrollment(Long userId, Long courseId) {
        return enrollmentRepository.findByUserIdAndCourseIdAndStatus(
                userId, courseId, EnrollmentStatus.ACTIVE
        ).isPresent() || enrollmentRepository.findByUserIdAndCourseIdAndStatus(
                userId, courseId, EnrollmentStatus.COMPLETED
        ).isPresent();
    }

    @Transactional(readOnly = true)
    public Enrollment getActiveEnrollment(Long userId, Long courseId) {
        return enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE
                        || e.getStatus() == EnrollmentStatus.COMPLETED)
                .orElse(null);
    }
}
