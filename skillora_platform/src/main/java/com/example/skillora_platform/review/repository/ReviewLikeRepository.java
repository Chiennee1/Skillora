package com.example.skillora_platform.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.review.entity.ReviewLike;
import com.example.skillora_platform.review.entity.ReviewLikeId;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, ReviewLikeId> {

    long countByReviewId(Long reviewId);

    boolean existsByUserIdAndReviewId(Long userId, Long reviewId);
}
