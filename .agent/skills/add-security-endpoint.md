# Skill: Add Security Endpoint

> Add security annotations to a new endpoint.

## Template

```java
// In SecurityConfig — add to permitAll if public
.requestMatchers(HttpMethod.GET, "/api/v1/courses/**").permitAll()

// In Controller — add role restriction
@PreAuthorize("hasRole('ADMIN')")
@PostMapping
public ApiResponse<?> create(...) { ... }

// In Service — verify ownership
public void requireOwnerOrAdmin(Course course, String actorEmail) {
    User actor = getActor(actorEmail);
    if (!course.getInstructor().getEmail().equals(actorEmail)
            && !actor.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ADMIN)) {
        throw new BusinessException("Access denied", HttpStatus.FORBIDDEN);
    }
}
```
