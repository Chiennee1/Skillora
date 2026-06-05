# Context: Tech Debt

> Known technical debt for Skillora. Updated: 2026-06-05.

## Completed / Resolved

| ID | Item | Module | Status |
|----|------|--------|--------|
| TD-001 | Profiles and environment-backed `application.yml` | config | Done 2026-05-27 |
| TD-002 | Spring Security JWT resource-server filter chain | config | Done 2026-05-27 |
| TD-003 | Shared `BaseEntity` audit fields | common | Done 2026-05-27 |
| TD-004 | `ApiResponse` and `PageResponse` wrappers | common | Done 2026-05-27 |
| TD-005 | `GlobalExceptionHandler` | exception | Done 2026-05-27 |
| TD-006 | CORS config without wildcards | config | Done 2026-05-27 |
| TD-007 | Swagger/SpringDoc OpenAPI | config | Done 2026-05-27 |
| TD-008 | Flyway baseline migration from current schema | database | Done 2026-06-05; live MySQL validation still tracked below |
| TD-009 | Rate limiting on auth/chat/payment sensitive writes | config | Done 2026-06-05; app-local only |
| TD-011 | JaCoCo coverage reporting | build | Done 2026-06-05 |
| TD-014 | User/Auth/JWT/OAuth2 APIs | user | Done 2026-05-28 |
| TD-017 | Course catalog/content APIs | course | Done 2026-05-28 |
| TD-019 | Redis cache config and public course cache annotations | course/config | Done 2026-06-05 |
| TD-020 | Enrolled-student lesson access | enrollment | Done 2026-05-28 |
| TD-021 | Enrollment/progress APIs | enrollment | Done 2026-05-28 |
| TD-022 | Quiz engine APIs | quiz | Done 2026-05-29 |
| TD-024 | Assignment/grading APIs | assignment | Done 2026-05-29 |
| TD-026 | Assignment graded/returned notifications | assignment/notification | Done 2026-06-01 |
| TD-027 | Review/rating APIs | review | Done 2026-05-29 |
| TD-028 | `CourseStatsService.refreshStats()` for admin stats | review/admin | Done 2026-06-01 |
| TD-030 | Review create/delete notifications | review/notification | Done 2026-06-01 |
| TD-031 | Commerce core APIs | commerce | Done 2026-05-30 |
| TD-032 | VNPay/MoMo payment create, return, IPN, signatures, idempotency | commerce | Done 2026-06-05; sandbox validation still tracked below |
| TD-034 | Admin coupon CRUD and audit trail | commerce/admin | Done 2026-06-01 |
| TD-038 | Payment success/failure domain events and notifications | commerce/notification | Done 2026-06-05 |
| TD-040 | AI chat + notifications APIs and tests | chat/notification | Done 2026-06-01 |
| TD-041 | Admin dashboard, user mgmt, course approval, coupon CRUD, audit logs | admin | Done 2026-06-01 |
| TD-045 | Admin dashboard enum-count bug | admin/course | Fixed 2026-06-05 |
| TD-046 | Admin audit-log lazy actor mapping bug | admin | Fixed 2026-06-05 |
| TD-047 | Dockerfile and docker-compose for app/MySQL/Redis | infra | Done 2026-06-05 |
| TD-048 | GitHub Actions backend test/package workflow | infra | Done 2026-06-05 |
| TD-049 | Prometheus actuator metrics endpoint | observability | Done 2026-06-05 |

## Open Production Debt

| ID | Item | Module | Effort |
|----|------|--------|--------|
| TD-010 | Limited unit tests; coverage is integration-heavy | all | ongoing |
| TD-012 | No circuit breaker/retry policy for external APIs | common | 3 hours |
| TD-013 | No MapStruct; DTO mapping is manual | all | 4 hours |
| TD-015 | Password reset email delivery not implemented for production | user | 2 hours |
| TD-016 | OAuth2 callback returns tokens in redirect URL; prefer one-time-code exchange | user | 3 hours |
| TD-018 | Bunny Stream webhook/status sync not implemented | course | 3 hours |
| TD-023 | Quiz `timeLimitMins` is stored but not strictly enforced | quiz | 2 hours |
| TD-025 | Assignment submissions store file URLs only; no managed upload/scanning | assignment | 2 hours |
| TD-029 | Review list like counts use per-review queries; optimize at scale | review | 2 hours |
| TD-033 | Pending order expiration scheduled cleanup not implemented | commerce | 1 hour |
| TD-035 | Notification SSE fanout is in-memory and single-instance only | notification | 3 hours |
| TD-036 | Gemini chat does not stream partial responses | chat | 2 hours |
| TD-037 | Gemini lacks circuit breaker, retry, and usage quota dashboard | chat/common | 4 hours |
| TD-039 | Chat content storage/rendering needs explicit sanitization policy for rich text | chat/security | 2 hours |
| TD-042 | Event-driven `course_stats` sync instead of on-demand admin refresh | admin | 3 hours |
| TD-043 | Per-user coupon usage limits not enforced | commerce/admin | 2 hours |
| TD-050 | Validate Flyway migration + `ddl-auto=validate` against clean MySQL | database | 1-2 hours |
| TD-051 | VNPay/MoMo sandbox end-to-end validation with real credentials/callbacks | commerce | 2-4 hours |
| TD-052 | Distributed rate limiting backed by Redis or gateway/WAF | config | 2-3 hours |
| TD-053 | Cloudinary/image upload pipeline not implemented | course/user | 3 hours |

## Latest Verification

| Date | Command | Result |
|------|---------|--------|
| 2026-06-05 | `mvnw.cmd test-compile` | Passed |
| 2026-06-05 | `mvnw.cmd test -Dtest=AdminIntegrationTest` | Passed 20/20 |
| 2026-06-05 | `mvnw.cmd test -Dtest=CommerceIntegrationTest` | Passed 14/14 |
| 2026-06-05 | `mvnw.cmd test -Dtest=CourseIntegrationTest` | Passed 7/7 |
| 2026-06-05 | `mvnw.cmd test` | Passed 81/81 |
| 2026-06-05 | `mvnw.cmd package -DskipTests` | Passed |
| 2026-06-05 | `mvnw.cmd verify` | Passed 81/81, packaged jar, generated JaCoCo, passed 60% instruction coverage gate |
