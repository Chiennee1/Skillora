# Skill: Add DTO Mapping

> Add DTO mapping between entity and response.

## Template

```java
// In Service class
private {Entity}Response toResponse({Entity} entity) {
    return {Entity}Response.builder()
            .id(entity.getId())
            // map all fields
            .createdAt(entity.getCreatedAt())
            .build();
}
```

## Rules
- Map in service layer only (private method)
- Use Builder pattern
- Never expose entity directly to controller
