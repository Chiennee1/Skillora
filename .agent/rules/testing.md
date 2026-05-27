# Rule: Testing

> Quy tắc testing cho dự án Skillora.

## Test Strategy

- **Primary**: Integration tests with `@SpringBootTest` + H2
- **Secondary**: Unit tests for complex business logic
- **Coverage target**: 60%+ (measured by JaCoCo when added)

## Test File Naming

| Source | Test |
|--------|------|
| `CourseService.java` | `CourseServiceTest.java` (unit) |
| `CourseController.java` | `CourseIntegrationTest.java` (integration) |

## Integration Test Pattern

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CourseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        courseRepository.deleteAll();
    }

    @Test
    void shouldCreateCourse() throws Exception {
        mockMvc.perform(post("/api/v1/courses")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Java Basics",
                        "level": "BEGINNER",
                        "price": 0
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Java Basics"));
    }
}
```

## Test Configuration

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
```

## Test Data

- Use `@BeforeEach` to set up test data
- Use Builder pattern for test entities
- NEVER depend on seed data from SQL files

## Running Tests

```powershell
# All tests
cd skillora_platform && .\mvnw.cmd test

# Single test class
cd skillora_platform && .\mvnw.cmd test -Dtest=CourseIntegrationTest

# With coverage (when JaCoCo added)
cd skillora_platform && .\mvnw.cmd verify
```
