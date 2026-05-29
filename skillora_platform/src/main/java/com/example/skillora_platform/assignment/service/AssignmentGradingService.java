package com.example.skillora_platform.assignment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.assignment.dto.AssignmentGradeRequest;
import com.example.skillora_platform.assignment.dto.AssignmentSubmissionResponse;
import com.example.skillora_platform.assignment.entity.AssignmentSubmission;
import com.example.skillora_platform.assignment.entity.SubmissionStatus;
import com.example.skillora_platform.assignment.repository.AssignmentSubmissionRepository;
import com.example.skillora_platform.course.service.CoursePermissionService;
import com.example.skillora_platform.enrollment.entity.Enrollment;
import com.example.skillora_platform.enrollment.entity.EnrollmentStatus;
import com.example.skillora_platform.enrollment.service.LearningProgressService;
import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.exception.ResourceNotFoundException;
import com.example.skillora_platform.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentGradingService {

    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final CoursePermissionService permissionService;
    private final LearningProgressService learningProgressService;
    private final AssignmentSubmissionService assignmentSubmissionService;

    @Transactional
    public AssignmentSubmissionResponse grade(Long submissionId, AssignmentGradeRequest request, String actorEmail) {
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        AssignmentSubmission submission = assignmentSubmissionRepository.findByIdWithDetails(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assignment submission not found with id: " + submissionId));
        permissionService.requireOwnerOrAdmin(
                submission.getAssignment().getLesson().getSection().getCourse(), actor);

        SubmissionStatus previousStatus = submission.getStatus();
        if (request.getStatus() == SubmissionStatus.GRADED) {
            applyGraded(submission, request, actor);
        } else if (request.getStatus() == SubmissionStatus.RETURNED) {
            applyReturned(submission, request, actor);
        } else {
            throw new BusinessException("Submission can only be graded or returned", HttpStatus.BAD_REQUEST);
        }

        AssignmentSubmission savedSubmission = assignmentSubmissionRepository.save(submission);
        if (previousStatus != SubmissionStatus.GRADED && savedSubmission.getStatus() == SubmissionStatus.GRADED) {
            completeAssignmentLesson(savedSubmission);
        }

        log.info("Instructor {} updated assignment submission {} to {}",
                actor.getId(), savedSubmission.getId(), savedSubmission.getStatus());
        return assignmentSubmissionService.toResponse(savedSubmission);
    }

    private void applyGraded(
            AssignmentSubmission submission,
            AssignmentGradeRequest request,
            User grader
    ) {
        if (request.getScore() == null) {
            throw new BusinessException("Score is required when grading a submission", HttpStatus.BAD_REQUEST);
        }
        BigDecimal maxScore = BigDecimal.valueOf(submission.getAssignment().getMaxScore());
        if (request.getScore().compareTo(maxScore) > 0) {
            throw new BusinessException("Score must not exceed assignment max score", HttpStatus.BAD_REQUEST);
        }

        submission.setStatus(SubmissionStatus.GRADED);
        submission.setScore(request.getScore());
        submission.setFeedback(trimToNull(request.getFeedback()));
        submission.setGradedAt(LocalDateTime.now());
        submission.setGradedBy(grader);
    }

    private void applyReturned(
            AssignmentSubmission submission,
            AssignmentGradeRequest request,
            User grader
    ) {
        if (submission.getStatus() == SubmissionStatus.GRADED) {
            throw new BusinessException("Graded submissions cannot be returned", HttpStatus.CONFLICT);
        }
        String feedback = trimToNull(request.getFeedback());
        if (feedback == null) {
            throw new BusinessException("Feedback is required when returning a submission", HttpStatus.BAD_REQUEST);
        }

        submission.setStatus(SubmissionStatus.RETURNED);
        submission.setScore(null);
        submission.setFeedback(feedback);
        submission.setGradedAt(LocalDateTime.now());
        submission.setGradedBy(grader);
    }

    private void completeAssignmentLesson(AssignmentSubmission submission) {
        Enrollment enrollment = submission.getEnrollment();
        if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
            learningProgressService.updateProgress(
                    enrollment.getId(),
                    submission.getAssignment().getLesson().getId(),
                    null,
                    true
            );
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
