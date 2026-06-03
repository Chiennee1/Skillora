package com.example.skillora_platform.admin.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DashboardStatsResponse {

    private UserStats users;
    private CourseStatsDto courses;
    private EnrollmentStats enrollments;
    private RevenueStats revenue;
    private ReviewStats reviews;

    @Getter
    @Setter
    @Builder
    public static class UserStats {
        private long total;
        private long active;
        private long banned;
        private long newThisMonth;
    }

    @Getter
    @Setter
    @Builder
    public static class CourseStatsDto {
        private long total;
        private long published;
        private long draft;
        private long reviewing;
        private long archived;
    }

    @Getter
    @Setter
    @Builder
    public static class EnrollmentStats {
        private long total;
        private long active;
        private long completed;
    }

    @Getter
    @Setter
    @Builder
    public static class RevenueStats {
        private long totalOrders;
        private long paidOrders;
        private BigDecimal totalRevenue;
    }

    @Getter
    @Setter
    @Builder
    public static class ReviewStats {
        private long totalPublished;
        private BigDecimal avgPlatformRating;
    }
}
