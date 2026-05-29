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
- **Implementation**: Unified PaymentService interface, provider-specific implementations

### AD-005: Video Hosting Strategy (2026-05)
- **Decision**: Bunny Stream for video courses + Cloudinary for images
- **Reason**: Bunny Stream is the most cost-effective for VOD at scale, Cloudinary excels at image transformations
- **Implementation**: Presigned URL upload (frontend → provider direct), store reference ID in DB

### AD-006: AI Chatbot with Gemini (2026-05)
- **Decision**: Use Google Gemini 2.5 Flash via AI Studio API key
- **Reason**: Free tier generous (1,500 RPD, 1M TPM), fast response, supports Vietnamese
- **Implementation**: REST API calls with Spring WebClient, conversation history stored in DB

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
