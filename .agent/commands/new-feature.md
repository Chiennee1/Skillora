# Command: /new-feature

> Create a complete new feature with all layers.

## Syntax
```
/new-feature <module> <entity> [--public] [--cached]
```

## Parameters
- `module`: Package name (e.g., `notification`, `assignment`)
- `entity`: Main entity name (e.g., `Notification`, `Assignment`)
- `--public`: Make GET endpoints public (no auth)
- `--cached`: Add Redis caching

## Execution Flow

1. Load `skills/create-module.md`
2. Create package structure under `com.example.skillora_platform.{module}`
3. Create entity, repository, DTOs, service, controller
4. Add API prefix to `Constants.java`
5. Update `SecurityConfig` if `--public` flag
6. Add cache annotations if `--cached` flag
7. Load `skills/create-integration-test.md`
8. Create basic integration test
9. Run `cd skillora_platform && .\mvnw.cmd compile` to verify

## Example
```
/new-feature assignment Assignment
```
Creates:
- `com.example.skillora_platform.assignment.entity.Assignment`
- `com.example.skillora_platform.assignment.repository.AssignmentRepository`
- `com.example.skillora_platform.assignment.dto.AssignmentCreateRequest`
- `com.example.skillora_platform.assignment.dto.AssignmentResponse`
- `com.example.skillora_platform.assignment.service.AssignmentService`
- `com.example.skillora_platform.assignment.controller.AssignmentController`
- Test: `AssignmentIntegrationTest`
