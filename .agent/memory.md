# Skillora — Project Memory

> Living document that captures key decisions, lessons learned, and important notes.
> Updated as the project evolves.

## Architecture Decisions

### AD-001: Layered Architecture (2026-05)
- **Decision**: Use layered architecture (Controller → Service → Repository → Entity)
- **Reason**: Simple, well-understood by team, fits the project scope
- **Trade-off**: Less flexibility than hexagonal/clean architecture, but faster to develop

### AD-002: 10-Module Package Structure (2026-05)
- **Decision**: Split into 10 domain modules: user, course, enrollment, quiz, assignment, commerce, review, chat, notification, admin
- **Reason**: Maps 1:1 to DB schema domains, clear separation of concerns
- **Trade-off**: More packages but each module is self-contained and testable

### AD-003: JWT + OAuth2 Auth (2026-05)
- **Decision**: Use JWT for API auth + OAuth2 for Google login, with callback returning JWT tokens
- **Reason**: Stateless API auth + seamless social login experience
- **Flow**: OAuth2 login → server creates/finds user → generates JWT → redirects to FE with tokens

### AD-004: Dual Payment Gateway (2026-05)
- **Decision**: VNPay as primary + MoMo as secondary payment gateway
- **Reason**: VNPay covers banking apps (most reliable), MoMo covers e-wallet users (most popular)
- **Implementation**: Concrete commerce `PaymentService` handles shared validation/idempotency/signing flow; MoMo provider calls are isolated behind `MomoClient` for testability. See AD-019 for the production implementation pass.

### AD-005: Video Hosting Strategy (2026-05)
- **Decision**: Bunny Stream for video courses + Cloudinary for images
- **Reason**: Bunny Stream is the most cost-effective for VOD at scale, Cloudinary excels at image transformations
- **Implementation**: Presigned URL upload (frontend → provider direct), store reference ID in DB

### AD-006: AI Chatbot with Gemini (2026-05)
- **Decision**: Use Google Gemini 2.5 Flash via AI Studio API key
- **Reason**: Free tier generous (1,500 RPD, 1M TPM), fast response, supports Vietnamese
- **Implementation**: REST API calls with Spring `RestClient`, conversation history stored in DB

### AD-007: Redis Cache Strategy (2026-05)
- **Decision**: Cache published courses list + popular course details, TTL 5 minutes
- **Reason**: Most frequently accessed, relatively stable data
- **Eviction**: All write operations on courses trigger `@CacheEvict(allEntries = true)`

### AD-008: Foundation Security with Spring Resource Server (2026-05-27)
- **Decision**: Use Spring Security OAuth2 Resource Server with HS256 JWT encoder/decoder for API authentication.
- **Reason**: Keeps JWT validation inside the Spring Security filter chain and avoids a custom JWT filter at the foundation phase.
- **Implementation**: JWT roles are read from the `roles` claim and converted to `ROLE_*` authorities.

### AD-009: Standard API Envelope at Framework Boundaries (2026-05-27)
- **Decision**: Use `ApiResponse<T>` and `PageResponse<T>` as shared response contracts, including security 401/403 errors.
- **Reason**: Keeps controller, validation, business exception, and security error responses consistent for all future modules.
- **Implementation**: MVC exceptions are handled by `GlobalExceptionHandler`; security filter errors are written by `SecurityConfig`.

### AD-010: Phase 1 Auth Token and Profile Model (2026-05-28)
- **Decision**: User self-registration supports `STUDENT` and `INSTRUCTOR`; self-registration as `ADMIN` is rejected.
- **Reason**: Allows instructor onboarding while protecting privileged platform access.
- **Implementation**: JWT access tokens include `sub`, `userId`, `email`, `fullName`, and `roles`; refresh tokens are random raw tokens stored as SHA-256 hashes and rotated on refresh.
- **Password reset**: Reset tokens are stored as SHA-256 hashes, expire after 1 hour, and are only returned in dev/test responses until SMTP delivery is implemented.
- **OAuth2**: Google login creates or updates a `STUDENT` user, marks verified Google emails as verified, and redirects to the frontend with access and refresh tokens.

### AD-011: Phase 2 Course Catalog and Content Model (2026-05-28)
- **Decision**: Course ownership is based on `courses.instructor_id`; owner and `ADMIN` can manage course content.
- **Reason**: Keeps Phase 2 independent from future admin approval and enrollment modules while matching the existing schema.
- **Implementation**: Courses use direct `DRAFT -> PUBLISHED` and `ARCHIVED` transitions; category delete deactivates taxonomy rows; course delete sets `deleted_at`.
- **Lesson access**: Published preview lessons are public; non-preview lessons require owner/admin until enrollment access exists in Phase 3.
- **Video upload**: Backend creates Bunny Stream videos and returns TUS upload headers/signature; API keys are never returned to clients.
- **Slugging**: Category/course/lesson slugs are generated through `SlugUtils`, including Vietnamese diacritic normalization and numeric suffixes for uniqueness.

### AD-012: Phase 3 Enrollment + Progress Model (2026-05-28)
- **Decision**: Free courses are directly enrollable via `POST /api/v1/courses/{id}/enroll`; paid courses require the Commerce module checkout flow (returns HTTP 402).
- **Reason**: Separates free enrollment from paid enrollment; Commerce module will call EnrollmentService programmatically after payment confirmation.
- **Implementation**:
  - Enrollment: `ACTIVE → COMPLETED → REFUNDED/CANCELLED` lifecycle; UNIQUE(user_id, course_id) prevents duplicates.
  - Progress tracking: Each lesson gets a `LessonProgress` record; lesson is auto-completed when `watchedSeconds >= 90% of durationSeconds` or explicitly marked completed.
  - Auto-completion: When all published lessons in active sections are completed, enrollment status transitions to `COMPLETED` and a `CourseCertificate` is auto-generated with a UUID-based code.
  - Enrolled student access: `LearningAccessService` is integrated into `LessonService.get()` to allow students with ACTIVE/COMPLETED enrollments to view non-preview lessons (resolves TD-020).
  - Dashboard: `GET /api/v1/learning/dashboard` aggregates total enrolled, in-progress, completed counts and recent enrollments.
  - Enrollment entity does not extend `BaseEntity` because the schema uses `enrolled_at` instead of `created_at`; `CourseCertificate` uses `issued_at`.

### AD-013: Phase 4 Nested Quiz and One-Step Submission Model (2026-05-29)
- **Decision**: Implement quiz management with nested quiz/question/answer payloads and one-step quiz submission.
- **Reason**: Keeps Phase 4 API compact and aligned with the existing API catalog while supporting full quiz authoring and auto-grading.
- **Implementation**:
  - Instructors/admins create and replace quiz content through `POST/PUT /api/v1/quizzes`.
  - Students submit through `POST /api/v1/quizzes/{id}/submit`; the service creates a submitted attempt immediately.
  - Auto-grading supports `SINGLE`, `MULTIPLE`, `TRUE_FALSE`, and exact-match `TEXT` questions.
  - Student quiz reads hide correct answer flags; course owners/admins can view answer correctness.
  - Passing a quiz marks the associated quiz lesson complete through `LearningProgressService` when the enrollment is still active.
  - `time_limit_mins` is stored for future timed-attempt UX but is not strictly enforced without a dedicated start-attempt endpoint.

### AD-014: Phase 5 Assignment Submission and Instructor Grading Model (2026-05-29)
- **Decision**: Implement one-assignment-per-lesson authoring, one-submission-per-enrollment, and instructor/admin grading through `SUBMITTED`, `RETURNED`, and `GRADED` states.
- **Reason**: Matches the existing schema uniqueness rules while keeping assignment workflow simple for students and instructors.
- **Implementation**:
  - Assignments attach only to `ASSIGNMENT` lessons and are managed by course owners/admins.
  - Students can submit only with an active/completed enrollment; late submissions are accepted and surfaced with `dueAt`/`late`.
  - Resubmission is allowed only after `RETURNED`, reusing the same submission row.
  - `GRADED` submissions mark the assignment lesson complete through `LearningProgressService` when the enrollment is still active.
  - File submissions are URL-based for this phase; managed upload/scanning is deferred.

### AD-015: Phase 6 Review/Rating and Course Stats Model (2026-05-29)
- **Decision**: Implement course reviews with 1-review-per-enrollment, like/unlike, and soft-delete; update `courses.avg_rating/total_reviews` directly.
- **Reason**: Keeps Phase 6 self-contained while maintaining data consistency in the `courses` table that `v_course_detail` already reads from.
- **Implementation**:
  - Only students with `ACTIVE` or `COMPLETED` enrollment can create a review; `UNIQUE(enrollment_id)` prevents duplicates.
  - Review delete is soft (`status = DELETED`); only the owner or an admin can delete. `DELETED` reviews are excluded from public listing and rating calculations.
  - Each review create/update/delete triggers `CourseRatingService.refreshCourseRating()` which recalculates `courses.avg_rating` and `courses.total_reviews` from published reviews.
  - `course_stats` table is deferred to the Admin module (tracked as TD-028); Phase 6 writes only to the `courses` table.
  - Like/unlike are idempotent; composite PK `(user_id, review_id)` prevents duplicate likes.
  - `GET /api/v1/reviews?courseId=X` is public; authenticated users receive a `likedByMe` flag in the response.
  - Review update only allows changing `rating` and `content`; the `courseId` and `enrollmentId` are immutable.

### AD-016: Phase 7 Commerce Core Without External Payment Gateway (2026-05-30)
- **Decision**: Implement Wishlist, Cart, Coupon validation, and Orders without VNPay/MoMo gateway callbacks in Phase 7.
- **Reason**: Enables the purchase workflow data model while keeping gateway signing/IPN complexity isolated for a later payment phase.
- **Implementation**:
  - Wishlist/cart items are student-only, idempotent, and only accept published, non-deleted courses.
  - Coupon validation always calculates against the authenticated student's current cart; client-provided totals are not trusted.
  - Checkout creates `PENDING` orders for paid totals and does not create enrollments until a later payment confirmation flow.
  - Checkout with `totalAmount = 0` is marked `PAID`, uses `paymentGateway = FREE`, clears the cart, creates enrollments, and increments course enrollment counts.
  - Coupon `usedCount` increments only for auto-paid zero-total orders; pending paid orders do not consume coupon usage.
  - `PaymentTransaction`, VNPay/MoMo return, and IPN handling are deferred to the dedicated payment gateway phase.

### AD-017: Phase 9 Gemini Chat and Event-Driven Notifications (2026-06-01)
- **Decision**: Implement AI chat with DB-backed conversation history and notifications with REST + SSE delivery.
- **Reason**: Keeps chat and notification modules aligned with the existing layered architecture while allowing future realtime scale-out.
- **Implementation**:
  - Chat uses `RestClient` through a `GeminiClient` interface, default model `gemini-2.5-flash`, and env-driven `skillora.ai.gemini.*` config.
  - Local secrets are read from ignored `.env.local`; `.env.example` documents required variables without secrets.
  - Chat loads actor/context/history in a read-only transaction, calls Gemini outside the DB transaction, then opens a short write transaction to persist messages.
  - Chat stores `chat_conversations` and `chat_messages`; user/assistant messages are persisted only after Gemini returns a successful response.
  - Existing conversation course context is revalidated on every ask, including archived/deleted courses and owner/admin/enrollment access.
  - Gemini response parsing concatenates all text parts in `candidates[0].content.parts`.
  - Course chat context exposes protected lesson content only to course owners/admins or enrolled students; public users get published course metadata only.
  - Notifications store rows in `notifications`, expose list/read/read-all APIs, return structured JSON `data`, and stream new notifications over in-memory `SseEmitter` connections.
  - Domain modules publish lightweight events; notification listeners use `@TransactionalEventListener(AFTER_COMMIT)` plus `REQUIRES_NEW` notification writes so notifications are not created for rolled-back transactions.
  - SSE delivery is triggered by `NotificationCreatedEvent` after the notification row commits.
  - Verification: `mvn.cmd test-compile`, Phase 9 targeted integration tests, and full `mvn.cmd test` passed on 2026-06-01.

## Lessons Learned

### LL-001: Spring MVC Path Variables Need Explicit Names Without `-parameters` (2026-05-28)
- Controller path variables should use explicit names such as `@PathVariable("id")` because the Maven compiler does not currently emit Java parameter metadata.

### LL-002: Hibernate 6 Enum Mapping Must Match Existing MySQL Schema (2026-05-28)
- User/auth enums are forced to `VARCHAR` with `@JdbcTypeCode(SqlTypes.VARCHAR)` because the SQL schema uses `VARCHAR`, not native MySQL enum columns.

### LL-003: Orphan Collection Replacement Needs Flush Before Reinsert (2026-05-28)
- Course requirement/outcome replacement must flush orphan removals before inserting new rows because the schema has unique `(course_id, order_index)` constraints.

## Known Issues

<!-- Will be populated as issues are discovered -->

## Technology Notes

### TN-001: Spring Boot 3.5.14
- Uses Jakarta EE namespace (not javax)
- Requires Java 17+
- Auto-configuration for Redis, JPA, Security, OAuth2

### TN-002: Gemini AI Studio Free Tier
- Model: Gemini 2.5 Flash (recommended for chatbot)
- Free tier: ~1,500 RPD, 10-15 RPM, 1,000,000 TPM
- Pro models excluded from free tier (since April 2026)
- Data may be used by Google to improve models on free tier
- Keep production and dev on separate GCP projects

### TN-003: VNPay Sandbox
- Test TmnCode and HashSecret from VNPay merchant portal
- Sandbox endpoint: `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html`
- Return URL must be publicly accessible (use ngrok for local dev)

### TN-004: Bunny Stream
- Upload via presigned URL (avoid piping through backend)
- Supports HLS adaptive streaming out of the box
- Token authentication for video URLs (prevent link sharing)
- Store `videoId` in `lesson_videos.asset_id` column
- Current Phase 2 flow returns TUS headers: `AuthorizationSignature`, `AuthorizationExpire`, `VideoId`, and `LibraryId`.

### AD-018: Admin Dashboard + Audit Module
- **Date**: 2026-06-01
- **Decision**: Admin module is a cross-cutting module aggregating data from user, course, enrollment, commerce, and review modules
- **Details**:
  - Dashboard stats are computed on-demand via repository queries (no materialized view)
  - Course approval workflow: supports REVIEWING/DRAFT → PUBLISHED/REJECTED via admin action
  - Audit logging captures ALL admin write operations (status changes, coupon CRUD, course approval/rejection)
  - `course_stats` table populated on-demand via `CourseStatsService.refreshStats(courseId)`
  - Event-driven stats sync deferred to future iteration
  - All admin endpoints protected via `@PreAuthorize("hasRole('ADMIN')")` at controller level
  - AuditLog entity does not extend BaseEntity (only has `created_at`, no `updated_at`)
  - CourseStats uses `@MapsId` with course_id as PK (FK to courses)

### AD-019: Production Payment and Backend Hardening Pass
- **Date**: 2026-06-05
- **Decision**: Implement VNPay/MoMo payment runtime flows directly in the commerce module and add baseline production infrastructure.
- **Reason**: Commerce checkout had pending paid orders but no gateway completion path, which blocked production readiness.
- **Implementation**:
  - Added `PaymentController` under `/api/v1/payments` for VNPay create/return/IPN and MoMo create/return/IPN.
  - Added concrete `PaymentService`, signing helpers, VNPay/MoMo config properties, `MomoClient`, and idempotent callback handling.
  - Successful gateway callbacks mark orders `PAID`, create enrollments, consume coupons once, and publish payment/order events.
  - Terminal gateway failures mark payment transactions and orders failed without creating enrollments.
  - Fixed admin dashboard enum status counting and audit-log lazy actor mapping.
  - Added Flyway baseline migration, Redis course cache, app-local rate limiting, Prometheus actuator metrics, Dockerfile, docker-compose, GitHub Actions, and JaCoCo reporting.
- **Verification**:
  - `mvnw.cmd test-compile` passed.
  - `mvnw.cmd test -Dtest=AdminIntegrationTest` passed 20/20.
  - `mvnw.cmd test -Dtest=CommerceIntegrationTest` passed 14/14.
  - `mvnw.cmd test -Dtest=CourseIntegrationTest` passed 7/7.
  - `mvnw.cmd test` passed 81/81.
  - `mvnw.cmd package -DskipTests` passed.
  - `mvnw.cmd verify` passed 81/81, packaged the jar, generated JaCoCo, and passed the 60% instruction coverage gate.
- **Remaining production validation**:
  - Validate Flyway + `ddl-auto=validate` against clean MySQL.
  - Run VNPay/MoMo sandbox callbacks with real merchant credentials.
  - Add circuit breakers/retries and distributed rate limiting before high-traffic deployment.
