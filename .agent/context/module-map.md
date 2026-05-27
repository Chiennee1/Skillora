# Context: Module Map

> Module plan and inter-module relationships for Skillora.

## Dependency Graph

```
user ←── course ←── quiz
  ↑        ↑         ↑
  │        ├── enrollment ←── assignment
  │        ├── review
  │        ├── commerce
  │        └── chat (AI chatbot)
  │              
  └── admin (reads from: user, course, commerce, review, enrollment)
  
notification (cross-cutting — triggered by events from any module)
```

## Module Details

### user (core — no external module dependency)
- **Tables**: users, roles, user_roles, user_profiles, instructor_profiles, refresh_tokens, password_reset_tokens
- **Entities**: User, Role, RoleName, UserProfile, InstructorProfile, RefreshToken, PasswordResetToken, UserStatus
- **Services**: AuthService, JwtService, RefreshTokenService, SocialAuthService, UserProfileService, InstructorProfileService, UserDetailsServiceImpl
- **Used by**: ALL other modules (User entity referenced everywhere)

### course (depends on: user)
- **Tables**: categories, courses, course_categories, course_requirements, course_outcomes, sections, lessons, lesson_videos, lesson_video_variants, lesson_resources
- **Entities**: Category, Course, CourseCategory, CourseRequirement, CourseOutcome, Section, Lesson, LessonVideo, LessonVideoVariant, LessonResource + enums (CourseLevel, CourseStatus, LessonType, VideoProvider, VideoStatus, ResourceType)
- **Services**: CategoryService, CourseService, SectionService, LessonService, LessonVideoService, LessonResourceService, CoursePermissionService

### enrollment (depends on: user, course)
- **Tables**: enrollments, lesson_progress, course_certificates
- **Entities**: Enrollment, LessonProgress, CourseCertificate, EnrollmentStatus
- **Services**: EnrollmentService, LearningProgressService, CertificateService, LearningDashboardFacade, LearningAccessService

### quiz (depends on: user, course, enrollment)
- **Tables**: quizzes, questions, answer_options, quiz_attempts, quiz_attempt_answers, quiz_attempt_answer_options
- **Entities**: Quiz, Question, AnswerOption, QuizAttempt, QuizAttemptAnswer, QuizAttemptAnswerOption, QuestionType
- **Services**: QuizService, QuizSubmissionService, QuizHistoryService

### assignment (depends on: user, course, enrollment)
- **Tables**: assignments, assignment_submissions
- **Entities**: Assignment, AssignmentSubmission, SubmissionStatus
- **Services**: AssignmentService, AssignmentSubmissionService, AssignmentGradingService

### commerce (depends on: user, course)
- **Tables**: wishlists, carts, cart_items, coupons, orders, order_items, payment_transactions
- **Entities**: Wishlist, Cart, CartItem, Coupon, Order, OrderItem, PaymentTransaction, OrderStatus, PaymentGateway, TxStatus, DiscountType
- **Services**: WishlistService, CartService, CouponService, OrderService, PaymentService (interface), VNPayPaymentService, MoMoPaymentService

### review (depends on: user, course, enrollment)
- **Tables**: reviews, review_likes
- **Entities**: Review, ReviewLike, ReviewLikeId, ReviewStatus
- **Services**: ReviewService, CourseStatsService

### chat (depends on: user, course)
- **Tables**: chat_conversations, chat_messages
- **Entities**: ChatConversation, ChatMessage, ChatRole
- **Services**: ChatbotService, GeminiClient, ChatRateLimiterService

### notification (depends on: user — event-driven from all modules)
- **Tables**: notifications
- **Entities**: Notification, NotificationType
- **Services**: NotificationService
- **Listeners**: EnrollmentNotificationListener, PaymentNotificationListener, ReviewNotificationListener

### admin (depends on: user, course, commerce, review, enrollment)
- **Tables**: course_stats, audit_logs
- **Entities**: CourseStats, AuditLog
- **Services**: AdminDashboardService, AuditLogService

## Cross-Cutting Packages

### config (cross-cutting)
- SecurityConfig, JwtConfig, VNPayConfig, MoMoConfig, RedisCacheConfig, GeminiConfig, BunnyStreamConfig, CloudinaryConfig, SecurityBeansConfig, JpaConfig

### common (shared utilities)
- ApiResponse, BaseEntity, Constants, PageResponse, SlugUtils, ExecutionTimeLoggingAspect, ExecutionLogProperties

### exception (error handling)
- GlobalExceptionHandler, BusinessException, ResourceNotFoundException
