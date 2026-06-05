# Context: Project Overview

> Snapshot of Skillora backend state. Updated: 2026-06-05 after production-backend completion pass.

## Project Summary

| Attribute | Value |
|-----------|-------|
| Name | Skillora Platform |
| Type | E-learning platform backend |
| Stack | Spring Boot 3.5.14, Java 17, MySQL 8+, Redis 7 |
| Architecture | Layered Controller -> Service -> Repository -> Entity |
| API Base | `/api/v1` |
| Modules | 10: user, course, enrollment, quiz, assignment, commerce, review, chat, notification, admin |
| Java Files | 314 |
| Test Suites | 14 |
| Latest Verification | `mvnw.cmd verify` passed 81/81, packaged the jar, generated JaCoCo, and passed the 60% coverage gate on 2026-06-05 |
| API MVP Completeness | ~100% backend API surface implemented |
| Production Readiness | ~88-90%; remaining work is mostly external-provider validation and scale hardening |

## Completion Status

| Module | API Status | Production Notes |
|--------|------------|------------------|
| Foundation/Core Config | Complete | Actuator health, OpenAPI, profiles, Docker, CI, JaCoCo report, Flyway migration added |
| User + Auth + JWT + OAuth2 | Complete | SMTP reset-email delivery and one-time OAuth code exchange remain future hardening |
| Course Catalog + Content | Complete | Redis-backed public course cache added; Bunny webhook/status sync still deferred |
| Enrollment + Progress | Complete | Free enrollment, progress, certificates, dashboard verified |
| Quiz Engine | Complete | Timed-attempt enforcement still deferred |
| Assignment + Grading | Complete | Managed file upload/scanning still deferred |
| Commerce + Payments | Complete | VNPay and MoMo create/return/IPN code implemented; sandbox/provider validation still required |
| Review/Rating | Complete | N+1 optimization for like counts remains scale debt |
| AI Chatbot | Complete | Gemini runtime path implemented; streaming, quota dashboard, and circuit breaker deferred |
| Notifications | Complete | REST + in-memory SSE implemented; broker/Redis pub-sub fanout needed for multi-instance deploy |
| Admin Dashboard + Audit | Complete | Dashboard enum bug and audit lazy-load bug fixed; admin regression tests pass |

## What's Ready

| Asset | Status | Notes |
|-------|--------|-------|
| DB Schema | Complete | Raw schema remains in `database/`; Flyway `V1__init_schema.sql` added for app migration |
| Seed Data | Complete | `database/skillora_seed_data.sql` sample data |
| Spring Boot backend | Complete | 314 Java files across 10 modules |
| Payment Gateway Code | Complete | VNPay HMAC-SHA512 and MoMo HMAC-SHA256 signing/callback validation implemented |
| Payment Idempotency | Complete | Duplicate successful callbacks do not duplicate coupon usage, enrollments, or notifications |
| Redis Cache | Complete | Course list/detail cache with 5 minute TTL; simple cache in test/dev by default |
| Rate Limiting | Initial | App-local limiter for auth writes, chat ask, and payment create/IPN endpoints |
| Observability | Initial | Actuator health/info/metrics/prometheus enabled |
| Docker | Initial | Dockerfile and compose for app/MySQL/Redis |
| CI | Initial | GitHub Actions backend `mvnw verify` workflow |
| Test Coverage | Initial | JaCoCo gate 60% instruction coverage; latest report: instruction 82.08%, line 85.21%, branch 53.10% |
| Agent Context | Updated | `.agent/context/*` reflects current implementation state |

## External Dependencies

| Service | Status | Config |
|---------|--------|--------|
| MySQL 8+ | Required | `DB_*`; production should use Flyway + `ddl-auto=validate` |
| Redis 7 | Production cache | `REDIS_*`; tests disable Redis health and use simple cache |
| VNPay | Implemented, needs merchant config | `VNPAY_*`, return/IPN endpoints implemented |
| MoMo | Implemented, needs partner config | `MOMO_*`, create/return/IPN endpoints implemented |
| Gemini AI | Implemented | `GEMINI_API_KEY`; explicit failure when missing |
| Bunny Stream | Upload-ticket implemented | `BUNNY_LIBRARY_ID`, `BUNNY_API_KEY`; webhook sync deferred |
| Cloudinary | Not implemented | Image upload hardening remains future work |
| SMTP | Not implemented | Password-reset email delivery remains future work |

## Remaining Production Gaps

| Gap | Priority | Notes |
|-----|----------|-------|
| Validate Flyway + `ddl-auto=validate` against clean MySQL | High | Migration is added but not yet verified against a live clean MySQL database in this pass |
| VNPay/MoMo sandbox end-to-end validation | High | Code and tests pass, but real provider credentials/callbacks still need sandbox verification |
| Circuit breakers/retries for external APIs | High | Needed for Gemini, Bunny, MoMo/VNPay create calls |
| Distributed rate limiting and SSE fanout | Medium | Current limiter/SSE are single-instance |
| Pending order expiration job | Medium | Manual cancel exists; scheduled cleanup not yet implemented |
| SMTP and upload security | Medium | Password reset email, Cloudinary/images, assignment file scanning |
