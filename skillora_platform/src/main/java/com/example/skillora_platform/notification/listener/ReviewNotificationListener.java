package com.example.skillora_platform.notification.listener;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.skillora_platform.notification.entity.NotificationType;
import com.example.skillora_platform.notification.event.ReviewCreatedEvent;
import com.example.skillora_platform.notification.event.ReviewDeletedEvent;
import com.example.skillora_platform.notification.service.NotificationService;
import com.example.skillora_platform.review.entity.Review;
import com.example.skillora_platform.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewNotificationListener {

    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReviewCreated(ReviewCreatedEvent event) {
        Review review = reviewRepository.findById(event.reviewId()).orElse(null);
        if (review == null) {
            return;
        }
        notificationService.createNotification(
                review.getCourse().getInstructor().getId(),
                NotificationType.REVIEW_CREATED,
                "Khóa học có đánh giá mới",
                review.getUser().getFullName() + " đã đánh giá khóa " + review.getCourse().getTitle() + ".",
                Map.of(
                        "courseId", review.getCourse().getId(),
                        "reviewId", review.getId(),
                        "rating", review.getRating()
                )
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReviewDeleted(ReviewDeletedEvent event) {
        Review review = reviewRepository.findById(event.reviewId()).orElse(null);
        if (review == null) {
            return;
        }
        notificationService.createNotification(
                review.getCourse().getInstructor().getId(),
                NotificationType.REVIEW_DELETED,
                "Một đánh giá đã bị xóa",
                "Một đánh giá của khóa " + review.getCourse().getTitle() + " đã bị xóa.",
                Map.of(
                        "courseId", review.getCourse().getId(),
                        "reviewId", review.getId()
                )
        );
    }
}
