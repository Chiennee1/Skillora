# Context: Project Overview

> Snapshot of Skillora project state. Updated: 2026-05-28 (Phase 3).

## Project Summary

| Attribute | Value |
|-----------|-------|
| Name | Skillora Platform |
| Type | E-Learning Platform Backend |
| Stack | Spring Boot 3.5.14, Java 17, MySQL 8+, Redis 7 |
| Architecture | Layered (Controller → Service → Repository → Entity) |
| API Base | `/api/v1` |
| Planned Modules | 10 (user, course, enrollment, quiz, assignment, commerce, review, chat, notification, admin) |
| Java Files | 130+ |
| Test Suites | 8 |
| DB Tables | 30+ (full schema designed, seed data ready) |

## Completion Status

| Module | Status | Completeness |
|--------|--------|-------------|
| Foundation/Core Config | ✅ Complete | 100% |
| User + Auth + JWT + OAuth2 | ✅ Complete | 100% |
| Course Catalog + Content | ✅ Complete | 100% |
| Enrollment + Progress | ✅ Complete | 100% |
| Quiz Engine | ❌ Not started | 0% |
| Assignment + Grading | ❌ Not started | 0% |
| Commerce (Cart + Payment) | ❌ Not started | 0% |
| Review/Rating | ❌ Not started | 0% |
| AI Chatbot (Gemini) | ❌ Not started | 0% |
| Notifications | ❌ Not started | 0% |
| Admin Dashboard | ❌ Not started | 0% |

## What's Ready

| Asset | Status | Notes |
|-------|--------|-------|
| DB Schema | ✅ Complete | `database/skill_database_schema.sql` — 30+ tables, 3 views |
| Seed Data | ✅ Complete | `database/skillora_seed_data.sql` — sample data |
| Spring Boot scaffold | ✅ Created | `SkilloraPlatformApplication.java` + pom.xml |
| Core foundation | ✅ Complete | `common`, `exception`, `config` packages with tests |
| User/Auth module | ✅ Complete | Register/login/refresh/logout/reset/profile/instructor/OAuth2 callback with integration tests |
| Course module | ✅ Complete | Categories, courses, sections, lessons, resources, ownership, Bunny TUS upload ticket |
| Enrollment module | ✅ Complete | Free enrollment, progress tracking, auto-completion, certificates, enrolled student lesson access, learning dashboard |
| Maven dependencies | ✅ Configured | Web, Security, OAuth2 Resource Server, JPA, Validation, Actuator, SpringDoc, MySQL, H2, Lombok |
| Agent framework | ✅ Configured | `.agent/` directory with rules, skills, context |

## External Dependencies

| Service | Status | Config |
|---------|--------|--------|
| MySQL 8+ | ✅ Required | `DB_*` env vars |
| Redis 7 | ⚡ Optional | Fallback to simple cache |
| VNPay | 🔲 To configure | Primary payment gateway |
| MoMo | 🔲 To configure | Secondary payment gateway |
| Gemini AI | 🔲 To configure | AI Studio API key |
| Cloudinary | 🔲 To configure | Image upload |
| Bunny Stream | 🔲 To configure | `BUNNY_LIBRARY_ID`, `BUNNY_API_KEY`, TUS upload ticket flow implemented |
| SMTP (MailHog) | ⚡ Optional | Disabled by default |
