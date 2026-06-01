package com.example.skillora_platform.commerce.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.commerce.dto.CartItemResponse;
import com.example.skillora_platform.commerce.dto.CartResponse;
import com.example.skillora_platform.commerce.dto.CommerceCourseResponse;
import com.example.skillora_platform.commerce.entity.Cart;
import com.example.skillora_platform.commerce.entity.CartItem;
import com.example.skillora_platform.commerce.repository.CartItemRepository;
import com.example.skillora_platform.commerce.repository.CartRepository;
import com.example.skillora_platform.course.entity.Course;
import com.example.skillora_platform.course.entity.CourseStatus;
import com.example.skillora_platform.course.repository.CourseRepository;
import com.example.skillora_platform.course.service.CoursePermissionService;
import com.example.skillora_platform.enrollment.repository.EnrollmentRepository;
import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.exception.ResourceNotFoundException;
import com.example.skillora_platform.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final String DEFAULT_CURRENCY = "VND";

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CoursePermissionService permissionService;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public CartResponse getCart(String actorEmail) {
        User actor = permissionService.requireActor(actorEmail);
        return cartRepository.findByUserIdWithItems(actor.getId())
                .map(this::toResponse)
                .orElseGet(this::emptyResponse);
    }

    @Transactional
    public CartResponse add(Long courseId, String actorEmail) {
        User actor = permissionService.requireActor(actorEmail);
        Course course = findPurchasableCourse(courseId);
        if (enrollmentRepository.existsByUserIdAndCourseId(actor.getId(), course.getId())) {
            throw new BusinessException("Already enrolled in this course", HttpStatus.CONFLICT);
        }

        Cart cart = getOrCreateCart(actor);
        if (!cartItemRepository.existsByCartIdAndCourseId(cart.getId(), course.getId())) {
            cartItemRepository.save(CartItem.builder()
                    .cartId(cart.getId())
                    .courseId(course.getId())
                    .cart(cart)
                    .course(course)
                    .build());
            entityManager.flush();
            entityManager.clear();
        }
        return cartRepository.findByUserIdWithItems(actor.getId())
                .map(this::toResponse)
                .orElseGet(this::emptyResponse);
    }

    @Transactional
    public CartResponse remove(Long courseId, String actorEmail) {
        User actor = permissionService.requireActor(actorEmail);
        cartRepository.findByUserId(actor.getId()).ifPresent(cart -> {
            if (cartItemRepository.existsByCartIdAndCourseId(cart.getId(), courseId)) {
                cartItemRepository.deleteByCartIdAndCourseId(cart.getId(), courseId);
            }
        });
        return cartRepository.findByUserIdWithItems(actor.getId())
                .map(this::toResponse)
                .orElseGet(this::emptyResponse);
    }

    Cart findCartWithItems(User actor) {
        return cartRepository.findByUserIdWithItems(actor.getId())
                .orElse(null);
    }

    CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = sortedItems(cart).stream()
                .map(item -> CartItemResponse.builder()
                        .course(toCourseResponse(item.getCourse()))
                        .addedAt(item.getAddedAt())
                        .build())
                .toList();
        return CartResponse.builder()
                .id(cart.getId())
                .itemCount(items.size())
                .subtotalAmount(subtotal(cart))
                .currency(currencyOrDefault(cart))
                .items(items)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    BigDecimal subtotal(Cart cart) {
        return sortedItems(cart).stream()
                .map(item -> effectivePrice(item.getCourse()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    String currencyOrDefault(Cart cart) {
        return sortedItems(cart).stream()
                .map(item -> item.getCourse().getCurrency())
                .filter(currency -> currency != null && !currency.isBlank())
                .findFirst()
                .orElse(DEFAULT_CURRENCY);
    }

    List<CartItem> sortedItems(Cart cart) {
        if (cart == null || cart.getItems() == null) {
            return List.of();
        }
        return cart.getItems().stream()
                .sorted(Comparator
                        .comparing(CartItem::getAddedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(CartItem::getCourseId))
                .toList();
    }

    BigDecimal effectivePrice(Course course) {
        return course.getDiscountPrice() == null ? money(course.getPrice()) : money(course.getDiscountPrice());
    }

    void requireSingleCurrency(Cart cart) {
        long currencyCount = sortedItems(cart).stream()
                .map(item -> item.getCourse().getCurrency())
                .filter(currency -> currency != null && !currency.isBlank())
                .distinct()
                .count();
        if (currencyCount > 1) {
            throw new BusinessException("Cart contains multiple currencies", HttpStatus.BAD_REQUEST);
        }
    }

    private Cart getOrCreateCart(User actor) {
        return cartRepository.findByUserId(actor.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder()
                        .user(actor)
                        .build()));
    }

    private Course findPurchasableCourse(Long courseId) {
        Course course = courseRepository.findByIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BusinessException("Course is not available for purchase", HttpStatus.BAD_REQUEST);
        }
        return course;
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

    private CartResponse emptyResponse() {
        return CartResponse.builder()
                .itemCount(0)
                .subtotalAmount(BigDecimal.ZERO)
                .currency(DEFAULT_CURRENCY)
                .items(List.of())
                .build();
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
