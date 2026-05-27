# Context: API Catalog

> Planned API endpoint catalog for Skillora. Updated: 2026-05-27.
> Status: 🔲 = Planned, ✅ = Implemented

## User & Auth Module

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| POST | `/api/v1/auth/register` | Public | — | Register new user |
| POST | `/api/v1/auth/login` | Public | — | Login, returns JWT tokens |
| POST | `/api/v1/auth/refresh` | Public | — | Refresh access token |
| POST | `/api/v1/auth/logout` | Public | — | Logout (revoke refresh token) |
| POST | `/api/v1/auth/forgot-password` | Public | — | Request password reset |
| POST | `/api/v1/auth/reset-password` | Public | — | Reset password with token |
| GET | `/api/v1/auth/me` | JWT | Any | Get current user info |
| GET | `/oauth2/authorization/google` | Public | — | Google OAuth2 login |
| GET | `/api/v1/profiles/me` | JWT | Any | Get my profile |
| PUT | `/api/v1/profiles/me` | JWT | Any | Update my profile |
| GET | `/api/v1/instructors/{id}` | Public | — | Get instructor profile |

## Course Module

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| GET | `/api/v1/categories` | Public | — | List categories |
| POST | `/api/v1/categories` | JWT | ADMIN | Create category |
| PUT | `/api/v1/categories/{id}` | JWT | ADMIN | Update category |
| DELETE | `/api/v1/categories/{id}` | JWT | ADMIN | Delete category |
| GET | `/api/v1/courses` | Public | — | List/search courses |
| GET | `/api/v1/courses/{idOrSlug}` | Public | — | Get course detail |
| POST | `/api/v1/courses` | JWT | INST/ADMIN | Create course |
| PUT | `/api/v1/courses/{id}` | JWT | Owner/ADMIN | Update course |
| DELETE | `/api/v1/courses/{id}` | JWT | Owner/ADMIN | Soft delete course |
| PATCH | `/api/v1/courses/{id}/publish` | JWT | Owner/ADMIN | Publish course |
| PATCH | `/api/v1/courses/{id}/archive` | JWT | Owner/ADMIN | Archive course |
| GET | `/api/v1/courses/{id}/sections` | Public | — | List sections |
| POST | `/api/v1/courses/{id}/sections` | JWT | Owner/ADMIN | Create section |
| POST | `/api/v1/sections/{id}/lessons` | JWT | Owner/ADMIN | Create lesson |
| GET | `/api/v1/lessons/{id}` | JWT | Enrolled/Owner | Get lesson |
| POST | `/api/v1/lessons/{id}/video/upload-url` | JWT | Owner/ADMIN | Get presigned upload URL |

## Enrollment & Learning Module

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| POST | `/api/v1/courses/{id}/enroll` | JWT | STUDENT | Enroll in free course |
| GET | `/api/v1/enrollments/me` | JWT | STUDENT | My enrollments |
| GET | `/api/v1/enrollments/{id}/progress` | JWT | Owner | Lesson progress |
| PATCH | `/api/v1/enrollments/{id}/lessons/{lid}/progress` | JWT | Owner | Update progress |
| GET | `/api/v1/enrollments/{id}/certificate` | JWT | Owner | Get certificate |
| GET | `/api/v1/learning/dashboard` | JWT | STUDENT | Dashboard |

## Quiz Module

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| POST | `/api/v1/quizzes` | JWT | INST/ADMIN | Create quiz |
| GET | `/api/v1/quizzes/{id}` | JWT | Enrolled/Owner | Get quiz |
| PUT | `/api/v1/quizzes/{id}` | JWT | INST/ADMIN | Update quiz |
| POST | `/api/v1/quizzes/{id}/submit` | JWT | STUDENT | Submit quiz |
| GET | `/api/v1/quizzes/{id}/attempts` | JWT | STUDENT | My attempts |

## Assignment Module

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| POST | `/api/v1/assignments` | JWT | INST/ADMIN | Create assignment |
| GET | `/api/v1/assignments/{id}` | JWT | Enrolled/Owner | Get assignment |
| POST | `/api/v1/assignments/{id}/submit` | JWT | STUDENT | Submit |
| GET | `/api/v1/assignments/{id}/submissions` | JWT | INST/ADMIN | List submissions |
| PATCH | `/api/v1/submissions/{id}/grade` | JWT | INST/ADMIN | Grade |

## Commerce Module

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| GET | `/api/v1/wishlist` | JWT | STUDENT | Get wishlist |
| POST | `/api/v1/wishlist/{courseId}` | JWT | STUDENT | Add to wishlist |
| DELETE | `/api/v1/wishlist/{courseId}` | JWT | STUDENT | Remove |
| GET | `/api/v1/cart` | JWT | STUDENT | Get cart |
| POST | `/api/v1/cart/{courseId}` | JWT | STUDENT | Add to cart |
| DELETE | `/api/v1/cart/{courseId}` | JWT | STUDENT | Remove |
| POST | `/api/v1/orders/checkout` | JWT | STUDENT | Checkout |
| GET | `/api/v1/orders/me` | JWT | STUDENT | My orders |
| POST | `/api/v1/payments/vnpay/create` | JWT | STUDENT | Create VNPay payment |
| GET | `/api/v1/payments/vnpay/return` | Public | — | VNPay return |
| POST | `/api/v1/payments/vnpay/ipn` | Public | — | VNPay IPN |
| POST | `/api/v1/payments/momo/ipn` | Public | — | MoMo IPN |
| POST | `/api/v1/coupons/validate` | JWT | STUDENT | Validate coupon |

## Review Module

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| GET | `/api/v1/reviews?courseId=X` | Public | — | List reviews |
| POST | `/api/v1/reviews` | JWT | STUDENT | Create review |
| PUT | `/api/v1/reviews/{id}` | JWT | Owner | Update |
| DELETE | `/api/v1/reviews/{id}` | JWT | Owner/ADMIN | Delete |
| POST | `/api/v1/reviews/{id}/like` | JWT | STUDENT | Like |
| DELETE | `/api/v1/reviews/{id}/like` | JWT | STUDENT | Unlike |

## Chat Module

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| POST | `/api/v1/chat/ask` | JWT | Any | Send message to AI |
| GET | `/api/v1/chat/conversations` | JWT | Owner | List conversations |
| GET | `/api/v1/chat/conversations/{id}/messages` | JWT | Owner | Get messages |

## Notification Module

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| GET | `/api/v1/notifications` | JWT | Owner | List notifications |
| PATCH | `/api/v1/notifications/{id}/read` | JWT | Owner | Mark as read |
| PATCH | `/api/v1/notifications/read-all` | JWT | Owner | Mark all read |

## Admin Module

| Method | Path | Auth | Role | Description |
|--------|------|------|------|-------------|
| GET | `/api/v1/admin/dashboard` | JWT | ADMIN | Dashboard stats |
| GET | `/api/v1/admin/users` | JWT | ADMIN | List users |
| PATCH | `/api/v1/admin/users/{id}/status` | JWT | ADMIN | Ban/activate |
| PATCH | `/api/v1/admin/courses/{id}/approve` | JWT | ADMIN | Approve/reject |
| GET | `/api/v1/admin/audit-logs` | JWT | ADMIN | Audit logs |

## Health

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/actuator/health` | Public | App health |
