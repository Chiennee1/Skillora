# Context: Entity Relationship

> Database entity relationships for Skillora. Based on `schema_reference_only.sql`.

## ER Diagram

```mermaid
erDiagram
    %% User & Auth
    users ||--o{ user_roles : has
    roles ||--o{ user_roles : has
    users ||--o| user_profiles : has
    users ||--o| instructor_profiles : has
    users ||--o{ refresh_tokens : has
    users ||--o{ password_reset_tokens : has

    %% Course Catalog
    users ||--o{ courses : instructs
    categories ||--o{ course_categories : has
    courses ||--o{ course_categories : has
    categories ||--o| categories : parent
    courses ||--o{ course_requirements : has
    courses ||--o{ course_outcomes : has

    %% Course Content
    courses ||--o{ sections : contains
    sections ||--o{ lessons : contains
    lessons ||--o| lesson_videos : has
    lesson_videos ||--o{ lesson_video_variants : has
    lessons ||--o{ lesson_resources : has

    %% Commerce
    users ||--o{ wishlists : has
    courses ||--o{ wishlists : in
    users ||--o| carts : has
    carts ||--o{ cart_items : contains
    courses ||--o{ cart_items : in
    users ||--o{ orders : places
    coupons ||--o{ orders : applied
    orders ||--o{ order_items : contains
    courses ||--o{ order_items : referenced_in
    orders ||--o{ payment_transactions : has

    %% Enrollment & Learning
    users ||--o{ enrollments : enrolls
    courses ||--o{ enrollments : has
    order_items ||--o| enrollments : creates
    enrollments ||--o{ lesson_progress : tracks
    lessons ||--o{ lesson_progress : tracked_in
    enrollments ||--o| course_certificates : earns

    %% Quiz
    lessons ||--o| quizzes : has
    quizzes ||--o{ questions : contains
    questions ||--o{ answer_options : has
    enrollments ||--o{ quiz_attempts : has
    quizzes ||--o{ quiz_attempts : has
    users ||--o{ quiz_attempts : takes
    quiz_attempts ||--o{ quiz_attempt_answers : contains
    questions ||--o{ quiz_attempt_answers : answered_in
    quiz_attempt_answers ||--o{ quiz_attempt_answer_options : selects
    answer_options ||--o{ quiz_attempt_answer_options : selected_in

    %% Assignments
    lessons ||--o| assignments : has
    assignments ||--o{ assignment_submissions : receives
    enrollments ||--o{ assignment_submissions : submits

    %% Reviews
    enrollments ||--o| reviews : writes
    users ||--o{ reviews : authors
    courses ||--o{ reviews : has
    reviews ||--o{ review_likes : receives
    users ||--o{ review_likes : gives

    %% Chat
    users ||--o{ chat_conversations : owns
    courses ||--o{ chat_conversations : about
    chat_conversations ||--o{ chat_messages : contains

    %% Notifications
    users ||--o{ notifications : receives

    %% Stats & Audit
    courses ||--o| course_stats : has
    users ||--o{ audit_logs : performed_by
```

## Key Relationships

| Relationship | Type | FK | Cascade |
|-------------|------|-----|---------| 
| User â†’ Roles | Many-to-Many | user_roles join table | CASCADE |
| User â†’ UserProfile | One-to-One | user_profiles.user_id | CASCADE |
| User â†’ InstructorProfile | One-to-One | instructor_profiles.user_id | CASCADE |
| User â†’ Courses | One-to-Many | courses.instructor_id | RESTRICT |
| Course â†’ Categories | Many-to-Many | course_categories join table | CASCADE |
| Category â†’ Parent | Self-referencing | categories.parent_id | SET NULL |
| Course â†’ Requirements | One-to-Many | course_requirements.course_id | CASCADE |
| Course â†’ Outcomes | One-to-Many | course_outcomes.course_id | CASCADE |
| Course â†’ Sections | One-to-Many | sections.course_id | CASCADE |
| Section â†’ Lessons | One-to-Many | lessons.section_id | CASCADE |
| Lesson â†’ LessonVideo | One-to-One | lesson_videos.lesson_id | CASCADE |
| LessonVideo â†’ Variants | One-to-Many | lesson_video_variants.lesson_video_id | CASCADE |
| Lesson â†’ Resources | One-to-Many | lesson_resources.lesson_id | CASCADE |
| User + Course â†’ Wishlist | Many-to-Many | wishlists (composite PK) | CASCADE |
| User â†’ Cart | One-to-One | carts.user_id | CASCADE |
| Cart â†’ CartItems | One-to-Many | cart_items.cart_id | CASCADE |
| User â†’ Orders | One-to-Many | orders.user_id | RESTRICT |
| Order â†’ Coupon | Many-to-One | orders.coupon_id | SET NULL |
| Order â†’ OrderItems | One-to-Many | order_items.order_id | CASCADE |
| Order â†’ PaymentTransactions | One-to-Many | payment_transactions.order_id | CASCADE |
| User + Course â†’ Enrollment | Many-to-Many (with attrs) | UNIQUE(user_id, course_id) | RESTRICT |
| OrderItem â†’ Enrollment | One-to-One (optional) | enrollments.order_item_id | SET NULL |
| Enrollment â†’ LessonProgress | One-to-Many | UNIQUE(enrollment_id, lesson_id) | CASCADE |
| Enrollment â†’ Certificate | One-to-One | UNIQUE(enrollment_id) | CASCADE |
| Lesson â†’ Quiz | One-to-One | UNIQUE(lesson_id) | CASCADE |
| Quiz â†’ Questions | One-to-Many | CASCADE |
| Question â†’ AnswerOptions | One-to-Many | CASCADE |
| Enrollment + Quiz â†’ QuizAttempt | UNIQUE(enrollment_id, quiz_id, attempt_no) | CASCADE |
| QuizAttempt â†’ Answers | One-to-Many | CASCADE |
| Answer â†’ SelectedOptions | Many-to-Many | CASCADE / RESTRICT |
| Lesson â†’ Assignment | One-to-One | UNIQUE(lesson_id) | CASCADE |
| Assignment + Enrollment â†’ Submission | UNIQUE(assignment_id, enrollment_id) | CASCADE |
| Enrollment â†’ Review | One-to-One | UNIQUE(enrollment_id) | CASCADE |
| Review â†’ ReviewLikes | Composite key (user_id, review_id) | CASCADE |
| Course â†’ CourseStats | One-to-One | UNIQUE(course_id) | CASCADE |
| User â†’ ChatConversations | One-to-Many | CASCADE |
| Conversation â†’ Messages | One-to-Many | CASCADE |
| User â†’ Notifications | One-to-Many | CASCADE |
| User â†’ AuditLogs | One-to-Many (optional) | SET NULL |

## DB Views

| View | Purpose |
|------|---------|
| `v_course_detail` | Course detail with instructor info + aggregated stats |
| `v_enrollment_progress` | Enrollment with calculated lesson progress percentage |
| `v_student_courses` | Student's enrolled courses with instructor name |
