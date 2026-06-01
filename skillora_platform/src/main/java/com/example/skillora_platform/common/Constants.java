package com.example.skillora_platform.common;

public final class Constants {

    public static final String API_V1_PREFIX = "/api/v1";
    public static final String AUTH_API_PREFIX = API_V1_PREFIX + "/auth";
    public static final String PROFILE_API_PREFIX = API_V1_PREFIX + "/profiles";
    public static final String INSTRUCTOR_API_PREFIX = API_V1_PREFIX + "/instructors";
    public static final String CATEGORY_API_PREFIX = API_V1_PREFIX + "/categories";
    public static final String COURSE_API_PREFIX = API_V1_PREFIX + "/courses";
    public static final String SECTION_API_PREFIX = API_V1_PREFIX + "/sections";
    public static final String LESSON_API_PREFIX = API_V1_PREFIX + "/lessons";
    public static final String LESSON_RESOURCE_API_PREFIX = API_V1_PREFIX + "/lesson-resources";
    public static final String ENROLLMENT_API_PREFIX = API_V1_PREFIX + "/enrollments";
    public static final String LEARNING_API_PREFIX = API_V1_PREFIX + "/learning";
    public static final String QUIZ_API_PREFIX = API_V1_PREFIX + "/quizzes";
    public static final String ASSIGNMENT_API_PREFIX = API_V1_PREFIX + "/assignments";
    public static final String SUBMISSION_API_PREFIX = API_V1_PREFIX + "/submissions";
    public static final String REVIEW_API_PREFIX = API_V1_PREFIX + "/reviews";
    public static final String WISHLIST_API_PREFIX = API_V1_PREFIX + "/wishlist";
    public static final String CART_API_PREFIX = API_V1_PREFIX + "/cart";
    public static final String COUPON_API_PREFIX = API_V1_PREFIX + "/coupons";
    public static final String ORDER_API_PREFIX = API_V1_PREFIX + "/orders";

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_INSTRUCTOR = "INSTRUCTOR";
    public static final String ROLE_STUDENT = "STUDENT";

    public static final String CACHE_COURSES_PUBLISHED = "courses:published";
    public static final String CACHE_COURSE_DETAIL = "courses:detail";

    private Constants() {
    }
}
