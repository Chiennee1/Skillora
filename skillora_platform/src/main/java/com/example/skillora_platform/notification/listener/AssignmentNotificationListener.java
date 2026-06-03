package com.example.skillora_platform.notification.listener;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.skillora_platform.assignment.entity.AssignmentSubmission;
import com.example.skillora_platform.assignment.repository.AssignmentSubmissionRepository;
import com.example.skillora_platform.notification.entity.NotificationType;
import com.example.skillora_platform.notification.event.AssignmentGradedEvent;
import com.example.skillora_platform.notification.event.AssignmentReturnedEvent;
import com.example.skillora_platform.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AssignmentNotificationListener {

    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final NotificationService notificationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAssignmentGraded(AssignmentGradedEvent event) {
        AssignmentSubmission submission = assignmentSubmissionRepository.findByIdWithDetails(event.submissionId())
                .orElse(null);
        if (submission == null) {
            return;
        }
        notificationService.createNotification(
                submission.getEnrollment().getUser().getId(),
                NotificationType.ASSIGNMENT_GRADED,
                "Bài tập đã được chấm",
                "Bài tập " + submission.getAssignment().getTitle() + " đã được chấm điểm.",
                Map.of(
                        "assignmentId", submission.getAssignment().getId(),
                        "submissionId", submission.getId(),
                        "score", submission.getScore()
                )
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAssignmentReturned(AssignmentReturnedEvent event) {
        AssignmentSubmission submission = assignmentSubmissionRepository.findByIdWithDetails(event.submissionId())
                .orElse(null);
        if (submission == null) {
            return;
        }
        notificationService.createNotification(
                submission.getEnrollment().getUser().getId(),
                NotificationType.ASSIGNMENT_RETURNED,
                "Bài tập cần chỉnh sửa",
                "Bài tập " + submission.getAssignment().getTitle() + " đã được trả lại để bạn cập nhật.",
                Map.of(
                        "assignmentId", submission.getAssignment().getId(),
                        "submissionId", submission.getId()
                )
        );
    }
}
