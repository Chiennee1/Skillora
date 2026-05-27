# Skillora — Agent Brain

> You are a senior full-stack engineer specializing in **Spring Boot + Java 17** backend systems.
> You are working on **Skillora**, a skill-based e-learning platform backend.

## Project Identity

- **Name**: Skillora Platform
- **Type**: E-Learning Platform (Backend REST API)
- **Language**: Java 17
- **Framework**: Spring Boot 3.5.14
- **Architecture**: Layered Architecture (Controller → Service → Repository → Entity)
- **API Base**: `/api/v1`
- **Database**: MySQL 8+ (production), H2 (test)
- **Cache**: Redis 7 + Spring Cache
- **Auth**: JWT Bearer + OAuth2 (Google)
- **AI**: Google Gemini (via AI Studio API — Gemini 2.5 Flash)
- **Payment**: VNPay (primary) + MoMo (secondary)
- **Video Hosting**: Cloudinary (image) + Bunny Stream (video courses)
- **Container**: Docker + Docker Compose

## Workspace Layout

```
e:\Profile\Skillora\                      ← workspace root
├── .agent\                               ← agent brain (this directory)
├── .github\                              ← GitHub workflows
└── skillora_platform\                    ← Spring Boot backend module
    ├── src/main/java/com/example/skillora_platform/  ← main source
    ├── src/test/java/com/example/skillora_platform/  ← tests
    ├── src/main/resources/               ← application.properties, profiles
    ├── database/                         ← SQL schema + seed data
    │   ├── skill_database_schema.sql     ← full schema (30+ tables)
    │   └── skillora_seed_data.sql        ← seed data
    ├── pom.xml                           ← Maven dependencies
    ├── mvnw / mvnw.cmd                   ← Maven wrapper
    └── .vscode/                          ← VS Code settings
```

## Module Map (10 packages)

| Package | Purpose | DB Tables |
|---------|---------|-----------|
| `user` | User management + Auth + JWT + OAuth2 + Profiles | users, roles, user_roles, user_profiles, instructor_profiles, refresh_tokens, password_reset_tokens |
| `course` | Course catalog + content management | categories, courses, course_categories, course_requirements, course_outcomes, sections, lessons, lesson_videos, lesson_video_variants, lesson_resources |
| `enrollment` | Enrollment + progress tracking + certificates | enrollments, lesson_progress, course_certificates |
| `quiz` | Quiz engine + auto-grading | quizzes, questions, answer_options, quiz_attempts, quiz_attempt_answers, quiz_attempt_answer_options |
| `assignment` | Assignment submission + grading | assignments, assignment_submissions |
| `commerce` | Cart + Wishlist + Orders + Payments + Coupons | wishlists, carts, cart_items, coupons, orders, order_items, payment_transactions |
| `review` | Course rating/review + likes | reviews, review_likes |
| `chat` | AI chatbot (Gemini) + conversations | chat_conversations, chat_messages |
| `notification` | User notifications | notifications |
| `admin` | Admin dashboard + audit logs + stats | course_stats, audit_logs |

## Cross-Cutting Packages

| Package | Purpose |
|---------|---------|
| `config` | Security, JWT, Redis, Payment, JPA, AI configs |
| `common` | ApiResponse, BaseEntity, Constants, PageResponse, SlugUtils, AOP logging |
| `exception` | GlobalExceptionHandler, BusinessException, ResourceNotFoundException |

## Roles & Authorization

| Role | Capabilities |
|------|-------------|
| `ADMIN` | Full platform access + admin dashboard + user management + course approval |
| `INSTRUCTOR` | Manage own courses + publish/archive + quiz/assignment management + grade |
| `STUDENT` | Enroll, track progress, submit quiz/assignment, write review, use chatbot |

## Key Conventions

1. **Response envelope**: Always wrap in `ApiResponse<T>` or `PageResponse<T>`
2. **Exceptions**: Throw `BusinessException` (with HttpStatus) or `ResourceNotFoundException`
3. **Transactions**: `@Transactional` on service methods, `readOnly = true` for queries
4. **Cache**: Use `@Cacheable` / `@CacheEvict` with `Constants.CACHE_*` keys
5. **Security**: `@PreAuthorize("hasRole('...')")` on controller methods
6. **Entity naming**: Singular (Course, not Courses), matches DB table name
7. **DTO suffix**: `*Request` for input, `*Response` for output
8. **Slug**: Auto-generated from title via `SlugUtils.toSlug()`
9. **Profiles**: `dev` (verbose), `prod` (quiet), `test` (H2 in-memory)
10. **Tests**: Integration tests with `@SpringBootTest` + H2

## External Services

| Service | Config Key | Notes |
|---------|-----------|-------|
| MySQL | `DB_*` | Schema: `database/skill_database_schema.sql`, ddl-auto=validate |
| Redis | `REDIS_*` | Cache TTL: 5 min, session store optional |
| VNPay | `VNPAY_*` | Primary payment, sandbox by default |
| MoMo | `MOMO_*` | Secondary payment, sandbox by default |
| Gemini AI | `GEMINI_*` | Gemini 2.5 Flash via AI Studio API key |
| Cloudinary | `CLOUDINARY_*` | Image upload (thumbnails, avatars) |
| Bunny Stream | `BUNNY_*` | Video course hosting + HLS streaming |
| SMTP | `MAIL_*` | MailHog for local dev |

## Loading Additional Context

For detailed rules, load files from:
- `rules/` — architecture, coding, security, database, testing, API, git rules
- `agents/` — specialized sub-agents for different tasks
- `skills/` — reusable task templates
- `context/` — project state snapshots
- `commands/` — slash command definitions
- `mcp-tools/` — MCP tool configurations
- `hooks/` — automated workflow hooks
