package com.example.skillora_platform.commerce.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.commerce.dto.CommerceCourseResponse;
import com.example.skillora_platform.commerce.dto.WishlistItemResponse;
import com.example.skillora_platform.commerce.dto.WishlistResponse;
import com.example.skillora_platform.commerce.entity.Wishlist;
import com.example.skillora_platform.commerce.repository.WishlistRepository;
import com.example.skillora_platform.course.entity.Course;
import com.example.skillora_platform.course.entity.CourseStatus;
import com.example.skillora_platform.course.repository.CourseRepository;
import com.example.skillora_platform.course.service.CoursePermissionService;
import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.exception.ResourceNotFoundException;
import com.example.skillora_platform.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final CourseRepository courseRepository;
    private final CoursePermissionService permissionService;

    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(String actorEmail) {
        User actor = permissionService.requireActor(actorEmail);
        return toResponse(wishlistRepository.findByUserIdWithCourses(actor.getId()));
    }

    @Transactional
    public WishlistResponse add(Long courseId, String actorEmail) {
        User actor = permissionService.requireActor(actorEmail);
        Course course = findCommerceCourse(courseId);

        if (!wishlistRepository.existsByUserIdAndCourseId(actor.getId(), course.getId())) {
            wishlistRepository.save(Wishlist.builder()
                    .userId(actor.getId())
                    .courseId(course.getId())
                    .user(actor)
                    .course(course)
                    .build());
        }
        return toResponse(wishlistRepository.findByUserIdWithCourses(actor.getId()));
    }

    @Transactional
    public WishlistResponse remove(Long courseId, String actorEmail) {
        User actor = permissionService.requireActor(actorEmail);
        if (wishlistRepository.existsByUserIdAndCourseId(actor.getId(), courseId)) {
            wishlistRepository.deleteByUserIdAndCourseId(actor.getId(), courseId);
        }
        return toResponse(wishlistRepository.findByUserIdWithCourses(actor.getId()));
    }

    private Course findCommerceCourse(Long courseId) {
        Course course = courseRepository.findByIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BusinessException("Course is not available", HttpStatus.BAD_REQUEST);
        }
        return course;
    }

    private WishlistResponse toResponse(List<Wishlist> wishlists) {
        List<WishlistItemResponse> items = wishlists.stream()
                .map(wishlist -> WishlistItemResponse.builder()
                        .course(toCourseResponse(wishlist.getCourse()))
                        .addedAt(wishlist.getCreatedAt())
                        .build())
                .toList();
        return WishlistResponse.builder()
                .itemCount(items.size())
                .items(items)
                .build();
    }

    private CommerceCourseResponse toCourseResponse(Course course) {
        return CommerceCourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .slug(course.getSlug())
                .thumbnailUrl(course.getThumbnailUrl())
                .instructorId(course.getInstructor().getId())
                .instructorName(course.getInstructor().getFullName())
                .price(money(course.getPrice()))
                .discountPrice(course.getDiscountPrice())
                .effectivePrice(effectivePrice(course))
                .currency(course.getCurrency())
                .build();
    }

    private BigDecimal effectivePrice(Course course) {
        return course.getDiscountPrice() == null ? money(course.getPrice()) : money(course.getDiscountPrice());
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
