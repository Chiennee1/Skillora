# Security Foundation Design

## Data Flow
1. Public requests to `/actuator/health`, `/api/v1/auth/**`, `/oauth2/**`, `/v3/api-docs/**`, and `/swagger-ui/**` pass through without authentication.
2. Protected API requests must send `Authorization: Bearer {jwt}`.
3. Spring Security OAuth2 Resource Server decodes the JWT with the configured HS256 secret.
4. Values from the `roles` claim are mapped to Spring authorities with the `ROLE_` prefix.
5. Controllers can use `@PreAuthorize` once domain modules are added.

## Response Contract
- Business and validation errors are returned by `GlobalExceptionHandler` using `ApiResponse`.
- Authentication and authorization errors are returned from the security filter chain using the same `ApiResponse` shape.
- Future controllers should return `ApiResponse<T>` or `PageResponse<T>` consistently.

## Security Notes
- Dev and test profiles may use local fallback JWT secrets.
- Production must provide `JWT_SECRET`, `DB_*`, and `FRONTEND_URL` through environment variables.
- CORS does not use wildcard origins; allowed origins are configured under `skillora.cors.allowed-origins`.
- Auth module will be responsible for issuing access and refresh tokens using the existing `JwtEncoder`.
