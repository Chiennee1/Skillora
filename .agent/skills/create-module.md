# Skill: Create Module

> Scaffold a new module package structure.

## Trigger

- User requests: "create module X", "add new package X"

## Parameters

- `{module}`: Module name (e.g., `notification`)
- `{entity}`: Primary entity (e.g., `Notification`)

## Generated Structure

```
com/example/skillora_platform/{module}/
├── entity/
│   └── {Entity}.java
├── repository/
│   └── {Entity}Repository.java
├── dto/
│   ├── {Entity}CreateRequest.java
│   └── {Entity}Response.java
├── service/
│   └── {Entity}Service.java
└── controller/
    └── {Entity}Controller.java
```

## Steps

1. Create package directories
2. Create Entity extending BaseEntity
3. Create Repository extending JpaRepository
4. Create Request/Response DTOs
5. Create Service with CRUD operations
6. Create Controller with REST endpoints
7. Add `Constants.{MODULE}_API_PREFIX = "/api/v1/{module}s"`
8. Update SecurityConfig if needed
9. Verify: `cd skillora_platform && .\mvnw.cmd compile`
