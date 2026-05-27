package com.example.skillora_platform.common;

public final class Constants {

    public static final String API_V1_PREFIX = "/api/v1";
    public static final String AUTH_API_PREFIX = API_V1_PREFIX + "/auth";

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
