package com.example.skillora_platform.review.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.common.PageResponse;
import com.example.skillora_platform.course.entity.Course;
import com.example.skillora_platform.course.repository.CourseRepository;
import com.example.skillora_platform.course.service.CoursePermissionService;
import com.example.skillora_platform.enrollment.entity.Enrollment;
import com.example.skillora_platform.enrollment.service.LearningAccessService;
import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.exception.ResourceNotFoundException;
import com.example.skillora_platform.notification.event.ReviewCreatedEvent;
import com.example.skillora_platform.notification.event.ReviewDeletedEvent;
import com.example.skillora_platform.review.dto.ReviewCreateRequest;
import com.example.skillora_platform.review.dto.ReviewResponse;
import com.example.skillora_platform.review.dto.ReviewUpdateRequest;
import com.example.skillora_platform.review.entity.Review;
import com.example.skillora_platform.review.entity.ReviewLike;
import com.example.skillora_platform.review.entity.ReviewLikeId;
import com.example.skillora_platform.review.entity.ReviewStatus;
import com.example.skillora_platform.review.repository.ReviewLikeRepository;
import com.example.skillora_platform.review.repository.ReviewRepository;
import com.example.skillora_platform.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final CourseRepository courseRepository;
    private final CoursePermissionService permissionService;
    private final LearningAccessService learningAccessService;
    private final CourseRatingService courseRatingService;
    private final ApplicationEventPublisher eventPublisher;

    // ── List reviews (public, paginated) ──

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> listReviews(Long courseId, Long currentUserId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), Constants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Math.max(page, 0), safeSize);

        Page<Review> reviewPage = reviewRepository.findByCourseIdAndStatus(
                courseId, ReviewStatus.PUBLISHED, pageable);

        Page<ReviewResponse> responsePage = reviewPage.map(review -> toResponse(review, currentUserId));
        return PageResponse.from(responsePage);
    }

    // ── Create review ──

    @Transactional
    public ReviewResponse create(ReviewCreateRequest request, String actorEmail) {
        User actor = permissionService.requireActor(actorEmail);
        Long courseId = request.getCourseId();

        Course course = courseRepository.findByIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        Enrollment enrollment = learningAccessService.getActiveEnrollment(actor.getId(), courseId);
        if (enrollment == null) {
            throw new BusinessException(
                    "You must be enrolled in this course to write a review", HttpStatus.FORBIDDEN);
        }

        if (reviewRepository.existsByEnrollmentId(enrollment.getId())) {
            throw new BusinessException(
                    "You have already reviewed this course", HttpStatus.CONFLICT);
        }

        Review review = Review.builder()
                .enrollment(enrollment)
                .user(actor)
                .course(course)
                .rating(request.getRating())
                .content(trimToNull(request.getContent()))
                .status(ReviewStatus.PUBLISHED)
                .build();

        Review saved = reviewRepository.save(review);
        courseRatingService.refreshCourseRating(courseId);
        eventPublisher.publishEvent(new ReviewCreatedEvent(saved.getId()));

        log.info("User {} created review {} for course {}", actor.getId(), saved.getId(), courseId);
        return toResponse(saved, actor.getId());
    }

    // ── Update review ──

    @Transactional
    public ReviewResponse update(Long reviewId, ReviewUpdateRequest request, String actorEmail) {
        User actor = permissionService.requireActor(actorEmail);
        Review review = findActiveReview(reviewId);

        requireReviewOwner(review, actor);

        review.setRating(request.getRating());
        review.setContent(trimToNull(request.getContent()));
        Review saved = reviewRepository.save(review);
        courseRatingService.refreshCourseRating(review.getCourse().getId());

        log.info("User {} updated review {}", actor.getId(), reviewId);
        return toResponse(saved, actor.getId());
    }

    // ── Delete review (soft) ──

    @Transactional
    public void delete(Long reviewId, String actorEmail) {
        User actor = permissionService.requireActor(actorEmail);
        Review review = findActiveReview(reviewId);

        if (!isReviewOwner(review, actor) && !permissionService.isAdmin(actor)) {
            throw new BusinessException("Only the review owner or an admin can delete this review",
                    HttpStatus.FORBIDDEN);
        }

        review.setStatus(ReviewStatus.DELETED);
        Review saved = reviewRepository.save(review);
        courseRatingService.refreshCourseRating(review.getCourse().getId());
        eventPublisher.publishEvent(new ReviewDeletedEvent(saved.getId()));

        log.info("User {} soft-deleted review {}", actor.getId(), reviewId);
    }

    // ── Like review ──

    @Transactional
    public void like(Long reviewId, String actorEmail) {
        User actor = permissionService.requireActor(actorEmail);
        Review review = findActiveReview(reviewId);

        ReviewLikeId likeId = new ReviewLikeId(actor.getId(), review.getId());
        if (reviewLikeRepository.existsById(likeId)) {
            return; // idempotent
        }

        ReviewLike like = ReviewLike.builder()
                .userId(actor.getId())
                .reviewId(review.getId())
                .build();
        reviewLikeRepository.save(like);
        log.debug("User {} liked review {}", actor.getId(), reviewId);
    }

    // ── Unlike review ──

    @Transactional
    public void unlike(Long reviewId, String actorEmail) {
        User actor = permissionService.requireActor(actorEmail);
        findActiveReview(reviewId); // verify review exists and is active

        ReviewLikeId likeId = new ReviewLikeId(actor.getId(), reviewId);
        if (!reviewLikeRepository.existsById(likeId)) {
            return; // idempotent
        }

        reviewLikeRepository.deleteById(likeId);
        log.debug("User {} unliked review {}", actor.getId(), reviewId);
    }

    // ── Private helpers ──

    private Review findActiveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        if (review.getStatus() == ReviewStatus.DELETED) {
            throw new ResourceNotFoundException("Review not found with id: " + reviewId);
        }
        return review;
    }

    private void requireReviewOwner(Review review, User actor) {
        if (!isReviewOwner(review, actor)) {
            throw new BusinessException("Only the review owner can update this review", HttpStatus.FORBIDDEN);
        }
    }

    private boolean isReviewOwner(Review review, User actor) {
        return review.getUser().getId().equals(actor.getId());
    }

    private ReviewResponse toResponse(Review review, Long currentUserId) {
        long likeCount = reviewLikeRepository.countByReviewId(review.getId());
        boolean likedByMe = currentUserId != null
                && reviewLikeRepository.existsByUserIdAndReviewId(currentUserId, review.getId());

        User user = review.getUser();
        return ReviewResponse.builder()
                .id(review.getId())
                .courseId(review.getCourse().getId())
                .userId(user.getId())
                .userName(user.getFullName())
                .userAvatarUrl(user.getAvatarUrl())
                .rating(review.getRating())
                .content(review.getContent())
                .status(review.getStatus())
                .likeCount(likeCount)
                .likedByMe(likedByMe)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
