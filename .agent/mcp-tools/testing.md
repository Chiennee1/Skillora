# MCP Tools: Testing

> Testing tools for Skillora.

## Maven Test Commands

```powershell
# Run all tests
cd skillora_platform && .\mvnw.cmd test

# Run specific test
cd skillora_platform && .\mvnw.cmd test -Dtest=CourseIntegrationTest

# Run with verbose output
cd skillora_platform && .\mvnw.cmd test -X

# Skip tests during build
cd skillora_platform && .\mvnw.cmd package -DskipTests
```

## Test Profile

H2 in-memory database for tests:
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
```

## Test Tools

| Tool | Purpose |
|------|---------|
| MockMvc | Controller integration testing |
| @SpringBootTest | Full context tests |
| @ActiveProfiles("test") | Test profile activation |
| H2 Database | In-memory test DB |
| Mockito | Unit test mocking |
| AssertJ | Fluent assertions |
