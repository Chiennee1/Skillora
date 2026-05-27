# Rule: Coding Standards

> Quy chuẩn viết code cho dự án Skillora (Java 17 + Spring Boot 3.x).

## General

- Java 17 features allowed: records, text blocks, sealed classes, pattern matching
- Use Lombok for boilerplate reduction
- Max line length: 120 characters
- Indent: 4 spaces (no tabs)

## Import Order

```
java.*
jakarta.*
org.springframework.*
com.example.skillora_platform.*
lombok.*
```

## Naming Conventions

| Type | Convention | Example |
|------|-----------|---------|
| Package | lowercase, singular | `com.example.skillora_platform.course` |
| Class | PascalCase | `CourseService`, `CourseCreateRequest` |
| Method | camelCase | `findBySlug()`, `createCourse()` |
| Constant | UPPER_SNAKE | `CACHE_COURSES_PUBLISHED` |
| Table | plural, snake_case | `courses`, `user_roles` |
| Column | snake_case | `instructor_id`, `created_at` |
| Enum | UPPER_SNAKE values | `CourseStatus.PUBLISHED` |

## Lombok Usage

```java
// Entity
@Entity @Table(name = "courses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Course extends BaseEntity { ... }

// DTO Request
@Getter @Setter
public class CourseCreateRequest { ... }

// DTO Response
@Getter @Setter @Builder
public class CourseResponse { ... }

// Service
@Service @RequiredArgsConstructor @Slf4j
public class CourseService { ... }
```

## Dependency Injection

```java
// ✅ GOOD: Constructor injection via Lombok
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
}

// ❌ BAD: Field injection
@Autowired
private CourseRepository courseRepository;
```

## DTO Mapping

```java
// In Service layer — private method
private CourseResponse toResponse(Course course) {
    return CourseResponse.builder()
            .id(course.getId())
            .title(course.getTitle())
            .slug(course.getSlug())
            .createdAt(course.getCreatedAt())
            .build();
}
```

## Error Handling

```java
// Throw BusinessException for business rule violations
throw new BusinessException("Course already published", HttpStatus.CONFLICT);

// Throw ResourceNotFoundException for 404
Course course = courseRepository.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
```

## Validation

```java
// Use Jakarta validation on DTOs
@Getter @Setter
public class CourseCreateRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255)
    private String title;

    @NotNull(message = "Level is required")
    private CourseLevel level;

    @DecimalMin(value = "0.00")
    private BigDecimal price;
}
```
