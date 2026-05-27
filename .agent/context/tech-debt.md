# Context: Tech Debt

> Known technical debt items for Skillora. Updated: 2026-05-27.
> Project is freshly initialized — these are anticipated items to watch.

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

## Architecture Debt (track during development)

| ID | Item | Module | Effort |
|----|------|--------|--------|
| TD-008 | No Flyway DB migrations (raw SQL) | database | 2 hours |
| TD-009 | No rate limiting on auth endpoints | user | 1 hour |
| TD-010 | No unit tests (plan integration-first) | all | ongoing |
| TD-011 | No JaCoCo test coverage | build | 1 hour |
| TD-012 | No circuit breaker for external APIs | common | 3 hours |
| TD-013 | No MapStruct for DTO mapping (manual) | all | 4 hours |
