# Context: Module Map

> Module plan and real code relationships for Skillora. Updated: 2026-06-05.

## Dependency Graph

```text
user <- course <- quiz
  ^      ^        ^
  |      |        |
  |      +-- enrollment <- assignment
  |      +-- review
  |      +-- commerce
  |      +-- chat
  |
  +-- admin reads user/course/commerce/review/enrollment

notification is cross-cutting and listens to domain events.
```

## Module Details

### user
- Tables: `users`, `roles`, `user_roles`, `user_profiles`, `instructor_profiles`, `refresh_tokens`, `password_reset_tokens`
- Key entities: `User`, `Role`, `UserProfile`, `InstructorProfile`, `RefreshToken`, `PasswordResetToken`, `UserStatus`
- Key services: `AuthService`, `JwtService`, `RefreshTokenService`, `SocialAuthService`, `UserProfileService`, `InstructorProfileService`, `UserDetailsServiceImpl`
- Used by every domain module.

### course
- Tables: `categories`, `courses`, `course_categories`, `course_requirements`, `course_outcomes`, `sections`, `lessons`, `lesson_videos`, `lesson_video_variants`, `lesson_resources`
- Key entities: `Category`, `Course`, `CourseCategory`, `CourseRequirement`, `CourseOutcome`, `Section`, `Lesson`, `LessonVideo`, `LessonVideoVariant`, `LessonResource`
- Key services: `CategoryService`, `CourseService`, `SectionService`, `LessonService`, `LessonVideoService`, `LessonResourceService`, `CoursePermissionService`
- Cache: `CourseService` caches public list/detail reads and evicts on course writes.

### enrollment
- Tables: `enrollments`, `lesson_progress`, `course_certificates`
- Key entities: `Enrollment`, `LessonProgress`, `CourseCertificate`, `EnrollmentStatus`
- Key services: `EnrollmentService`, `LearningProgressService`, `CertificateService`, `LearningDashboardFacade`, `LearningAccessService`

### quiz
- Tables: `quizzes`, `questions`, `answer_options`, `quiz_attempts`, `quiz_attempt_answers`, `quiz_attempt_answer_options`
- Key entities: `Quiz`, `Question`, `AnswerOption`, `QuizAttempt`, `QuizAttemptAnswer`, `QuizAttemptAnswerOption`, `QuestionType`
- Key services: `QuizService`, `QuizSubmissionService`, `QuizHistoryService`

### assignment
- Tables: `assignments`, `assignment_submissions`
- Key entities: `Assignment`, `AssignmentSubmission`, `SubmissionStatus`
- Key services: `AssignmentService`, `AssignmentSubmissionService`, `AssignmentGradingService`

### commerce
- Tables: `wishlists`, `carts`, `cart_items`, `coupons`, `orders`, `order_items`, `payment_transactions`
- Key entities: `Wishlist`, `Cart`, `CartItem`, `Coupon`, `Order`, `OrderItem`, `PaymentTransaction`, `OrderStatus`, `PaymentGateway`, `TxStatus`, `DiscountType`
- Key services: `WishlistService`, `CartService`, `CouponService`, `OrderService`, `PaymentService`
- Payment support classes: `PaymentSignatureUtils`, `MomoClient`, `DefaultMomoClient`, `MomoCreatePaymentPayload`, `MomoCreatePaymentResult`
- Controllers: `WishlistController`, `CartController`, `CouponController`, `OrderController`, `PaymentController`

### review
- Tables: `reviews`, `review_likes`
- Key entities: `Review`, `ReviewLike`, `ReviewLikeId`, `ReviewStatus`
- Key services: `ReviewService`, `CourseRatingService`

### chat
- Tables: `chat_conversations`, `chat_messages`
- Key entities: `ChatConversation`, `ChatMessage`, `ChatRole`
- Key services: `ChatbotService`, `GeminiClient`, `DefaultGeminiClient`
- Rate limiting is currently handled by the cross-cutting `RateLimitFilter`, not a chat-specific service.

### notification
- Tables: `notifications`
- Key entities: `Notification`, `NotificationType`
- Key services: `NotificationService`, `NotificationSseService`
- Listeners: enrollment, assignment, payment, review, and course lifecycle notification listeners.

### admin
- Tables: `course_stats`, `audit_logs`
- Key entities: `CourseStats`, `AuditLog`
- Key services: `AdminDashboardService`, `AdminUserService`, `AdminCourseService`, `AdminCouponService`, `AdminAuditLogService`, `AuditLogService`, `CourseStatsService`
- `AdminAuditLogService` maps audit actor fields inside a read-only transaction to avoid lazy-loading failures.

## Cross-Cutting Packages

### config
- Real classes: `SecurityConfig`, `OpenApiConfig`, `JwtProperties`, `CorsProperties`, `AppOAuth2Properties`, `GeminiProperties`, `BunnyStreamProperties`, `PaymentProperties`, `VnPayProperties`, `MomoProperties`, `CacheConfig`, `RateLimitProperties`, `RateLimitFilter`
- Not currently present: `CloudinaryConfig`, dedicated circuit breaker config, distributed rate-limit config.

### common
- Real classes: `ApiResponse`, `BaseEntity`, `Constants`, `PageResponse`, `SlugUtils`
- Not currently present: execution-time AOP logging classes.

### exception
- Real classes: `GlobalExceptionHandler`, `BusinessException`, `ResourceNotFoundException`

## Infrastructure

- Flyway migration: `src/main/resources/db/migration/V1__init_schema.sql`
- Docker: `Dockerfile`, `.dockerignore`, `docker-compose.yml`
- CI: `.github/workflows/backend-ci.yml`
- Observability: Spring Actuator health/info/metrics/prometheus endpoints
- Cache: Redis cache manager in production; simple cache in test/dev defaults
