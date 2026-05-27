# Skill: Create CRUD API

> Scaffold complete CRUD endpoints for an existing entity.

## Trigger

- User requests: "add CRUD for X", "create endpoints for X"
- Entity already exists but needs REST API

## Parameters

- `{entity}`: Entity class name (e.g., `UserProfile`)
- `{module}`: Module package name (e.g., `user`)
- `{path}`: API path (e.g., `/api/v1/profiles`)

## Generated Files

### 1. Create Request DTO
```java
@Getter @Setter
public class {Entity}CreateRequest {
    @NotBlank private String field1;
}
```

### 2. Response DTO
```java
@Getter @Setter @Builder
public class {Entity}Response {
    private Long id;
    private LocalDateTime createdAt;
}
```

### 3. Service
```java
@Service
@RequiredArgsConstructor
public class {Entity}Service {
    private final {Entity}Repository repository;

    @Transactional(readOnly = true)
    public List<{Entity}Response> getAll() { ... }

    @Transactional
    public {Entity}Response create({Entity}CreateRequest request, String actorEmail) { ... }

    private {Entity}Response toResponse({Entity} entity) { ... }
}
```

### 4. Controller
```java
@RestController
@RequestMapping(Constants.{MODULE}_API_PREFIX)
@RequiredArgsConstructor
public class {Entity}Controller {
    private final {Entity}Service service;

    @GetMapping
    public ApiResponse<List<{Entity}Response>> getAll() { ... }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<{Entity}Response> create(@Valid @RequestBody ...) { ... }
}
```

## Checklist

- [ ] Request DTOs with validation annotations
- [ ] Response DTO with Builder
- [ ] Service with @Transactional
- [ ] Controller with proper HTTP status codes
- [ ] Constants updated
- [ ] SecurityConfig updated (if public GET)
