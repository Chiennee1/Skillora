# Rule: API Design

> Quy tắc thiết kế REST API cho Skillora.

## URL Structure

```
/api/v1/{resource}              → Collection
/api/v1/{resource}/{id}         → Single resource
/api/v1/{resource}/{id}/{sub}   → Sub-resource
```

## HTTP Methods

| Method | Purpose | Response Code |
|--------|---------|--------------|
| GET | Read | 200 OK |
| POST | Create | 201 Created |
| PUT | Full update | 200 OK |
| PATCH | Partial update / action | 200 OK |
| DELETE | Delete | 204 No Content |

## Response Envelope

```java
// Success response
{
    "success": true,
    "message": "Course created successfully",
    "data": { ... }
}

// Paginated response
{
    "success": true,
    "data": {
        "content": [ ... ],
        "page": 0,
        "size": 20,
        "totalElements": 100,
        "totalPages": 5
    }
}

// Error response
{
    "success": false,
    "message": "Course not found",
    "errors": [ ... ]
}
```

## Pagination

- Default page size: 20
- Max page size: 100
- Parameters: `?page=0&size=20&sort=createdAt,desc`

## Filtering & Search

- Simple filters: `?status=PUBLISHED&level=BEGINNER`
- Search: `?search=java+programming`
- Date range: `?from=2026-01-01&to=2026-12-31`

## Naming Conventions

| Pattern | Example |
|---------|---------|
| List resources | `GET /api/v1/courses` |
| Get by ID | `GET /api/v1/courses/{id}` |
| Get by slug | `GET /api/v1/courses/{idOrSlug}` |
| Create | `POST /api/v1/courses` |
| Update | `PUT /api/v1/courses/{id}` |
| Action | `PATCH /api/v1/courses/{id}/publish` |
| Sub-resource | `GET /api/v1/courses/{id}/sections` |
| Current user | `GET /api/v1/enrollments/me` |

## Security Headers

- All endpoints return `Content-Type: application/json`
- CORS configured for specific origins (no wildcards in prod)
- JWT token in `Authorization: Bearer {token}` header
