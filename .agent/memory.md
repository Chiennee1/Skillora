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

## Lessons Learned

<!-- Will be populated as the project develops -->

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
