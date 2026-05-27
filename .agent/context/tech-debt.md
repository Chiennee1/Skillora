# Context: Tech Debt

> Known technical debt items for Skillora. Updated: 2026-05-28.

## Completed Setup (Foundation phase)

| ID | Item | Module | Status |
|----|------|--------|--------|
| TD-001 | Configure application.yml with profiles (dev/prod/test) | config | ✅ Done 2026-05-27 |
| TD-002 | Set up SecurityConfig with JWT filter chain | config | ✅ Done 2026-05-27 |
| TD-003 | Create BaseEntity with audit fields | common | ✅ Done 2026-05-27 |
| TD-004 | Create ApiResponse/PageResponse wrappers | common | ✅ Done 2026-05-27 |
| TD-005 | Create GlobalExceptionHandler | exception | ✅ Done 2026-05-27 |
| TD-006 | Configure CORS properly (no wildcards) | config | ✅ Done 2026-05-27 |
| TD-007 | Add Swagger/SpringDoc OpenAPI | config | ✅ Done 2026-05-27 |
| TD-014 | Implement User/Auth/JWT/OAuth2 Phase 1 APIs | user | ✅ Done 2026-05-28 |
| TD-017 | Implement Course Catalog + Content Phase 2 APIs | course | ✅ Done 2026-05-28 |

## Architecture Debt (track during development)

| ID | Item | Module | Effort |
|----|------|--------|--------|
| TD-008 | No Flyway DB migrations (raw SQL) | database | 2 hours |
| TD-009 | No rate limiting on auth endpoints | user | 1 hour |
| TD-010 | Limited unit tests (integration-first coverage is primary) | all | ongoing |
| TD-011 | No JaCoCo test coverage | build | 1 hour |
| TD-012 | No circuit breaker for external APIs | common | 3 hours |
| TD-013 | No MapStruct for DTO mapping (manual) | all | 4 hours |
| TD-015 | Password reset email delivery not implemented for production | user | 2 hours |
| TD-016 | OAuth2 callback returns tokens in redirect URL; prefer one-time-code exchange later | user | 3 hours |
| TD-018 | Bunny Stream webhook/status sync not implemented | course | 3 hours |
| TD-019 | Redis cache annotations/config for public course catalog not implemented yet | course | 2 hours |
| TD-020 | Enrolled-student lesson access deferred until Enrollment module | course/enrollment | Phase 3 |
