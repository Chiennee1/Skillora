# Skill: Debug API Error

> Systematically debug API errors.

## Steps

1. Check error response body for exception type
2. Check server logs: `cd skillora_platform && .\mvnw.cmd spring-boot:run`
3. Enable verbose SQL: `spring.jpa.show-sql=true`
4. Common issues:
   - 401: Missing/invalid JWT token
   - 403: Missing `@PreAuthorize` or wrong role
   - 404: Wrong URL path or resource not found
   - 500: Null pointer, DB constraint violation
5. Fix and verify with test
