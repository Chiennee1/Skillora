# Rule: Security

> Quy tắc bảo mật cho dự án Skillora.

## Authentication

### JWT Configuration
- Access token TTL: 15 minutes
- Refresh token TTL: 7 days
- Algorithm: HS256 (HMAC-SHA256)
- Secret: Min 32 bytes, from env var `JWT_SECRET`

### OAuth2 (Google)
- Provider: Google only
- Flow: Authorization Code → JWT token exchange
- Callback: Server creates/finds user → generates JWT → redirects to FE

## Authorization

```java
// Controller-level authorization
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasRole('INSTRUCTOR')")
@PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
@PreAuthorize("isAuthenticated()")

// Service-level permission checks
permissionService.requireOwnerOrAdmin(course, actorEmail);
```

## SecurityConfig Pattern

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/**").permitAll()
            .requestMatchers("/api/v1/courses").permitAll()  // Public listing
            .requestMatchers("/actuator/health").permitAll()
            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

## Password Security

- Hash: BCrypt (strength 10)
- Min length: 8 characters
- Reset token: SecureRandom, SHA-256 hashed, expires in 1 hour

## CORS

```java
// Dev: Allow localhost origins
// Prod: Whitelist specific frontend domain ONLY
// NEVER use "*" in production
```

## Input Validation

- Always use `@Valid` on request body
- Sanitize text inputs to prevent XSS
- Use parameterized queries (JPA handles this)
- Validate file uploads (type, size)

## Secrets Management

- All secrets in env vars or `.env` file
- `.env` in `.gitignore` — NEVER commit
- Use `@Value("${...}")` or `@ConfigurationProperties`
