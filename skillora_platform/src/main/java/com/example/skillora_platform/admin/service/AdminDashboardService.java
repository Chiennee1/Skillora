package com.example.skillora_platform.admin.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.admin.dto.DashboardStatsResponse;
import com.example.skillora_platform.admin.dto.RevenueResponse;
import com.example.skillora_platform.commerce.entity.OrderStatus;
import com.example.skillora_platform.commerce.repository.OrderRepository;
import com.example.skillora_platform.course.entity.CourseStatus;
import com.example.skillora_platform.course.repository.CourseRepository;
import com.example.skillora_platform.enrollment.entity.EnrollmentStatus;
import com.example.skillora_platform.enrollment.repository.EnrollmentRepository;
import com.example.skillora_platform.review.repository.ReviewRepository;
import com.example.skillora_platform.user.entity.UserStatus;
import com.example.skillora_platform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        return DashboardStatsResponse.builder()
                .users(buildUserStats())
                .courses(buildCourseStats())
                .enrollments(buildEnrollmentStats())
                .revenue(buildRevenueStats())
                .reviews(buildReviewStats())
                .build();
    }

    @Transactional(readOnly = true)
    public RevenueResponse getRevenueSummary() {
        long paidOrders = orderRepository.countByStatus(OrderStatus.PAID);
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);
        BigDecimal totalRevenue = orderRepository.sumTotalAmountByStatus(OrderStatus.PAID);
        BigDecimal avgOrderValue = paidOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(paidOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return RevenueResponse.builder()
                .totalRevenue(totalRevenue)
                .totalPaidOrders(paidOrders)
                .totalPendingOrders(pendingOrders)
                .totalCancelledOrders(cancelledOrders)
                .avgOrderValue(avgOrderValue)
                .build();
    }

    private DashboardStatsResponse.UserStats buildUserStats() {
        return DashboardStatsResponse.UserStats.builder()
                .total(userRepository.count())
                .active(userRepository.countByStatus(UserStatus.ACTIVE))
                .banned(userRepository.countByStatus(UserStatus.BANNED))
                .newThisMonth(userRepository.countByCreatedAtAfter(
                        LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)))
                .build();
    }

    private DashboardStatsResponse.CourseStatsDto buildCourseStats() {
        return DashboardStatsResponse.CourseStatsDto.builder()
                .total(courseRepository.countByDeletedAtIsNull())
                .published(courseRepository.countByStatusAndDeletedAtIsNull(CourseStatus.PUBLISHED))
                .draft(courseRepository.countByStatusAndDeletedAtIsNull(CourseStatus.DRAFT))
                .reviewing(courseRepository.countByStatusAndDeletedAtIsNull(CourseStatus.REVIEWING))
                .archived(courseRepository.countByStatusAndDeletedAtIsNull(CourseStatus.ARCHIVED))
                .build();
    }

    private DashboardStatsResponse.EnrollmentStats buildEnrollmentStats() {
        return DashboardStatsResponse.EnrollmentStats.builder()
                .total(enrollmentRepository.count())
                .active(enrollmentRepository.countByStatus(EnrollmentStatus.ACTIVE))
                .completed(enrollmentRepository.countByStatus(EnrollmentStatus.COMPLETED))
                .build();
    }

    private DashboardStatsResponse.RevenueStats buildRevenueStats() {
        return DashboardStatsResponse.RevenueStats.builder()
                .totalOrders(orderRepository.count())
                .paidOrders(orderRepository.countByStatus(OrderStatus.PAID))
                .totalRevenue(orderRepository.sumTotalAmountByStatus(OrderStatus.PAID))
                .build();
    }

    private DashboardStatsResponse.ReviewStats buildReviewStats() {
        return DashboardStatsResponse.ReviewStats.builder()
                .totalPublished(reviewRepository.countAllPublished())
                .avgPlatformRating(reviewRepository.avgRatingAllPublished()
                        .map(avg -> BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP))
                        .orElse(BigDecimal.ZERO))
                .build();
    }
}
