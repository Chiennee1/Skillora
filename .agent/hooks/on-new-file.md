# Hook: On-New-File

> Checks when a new Java file is created.

## Trigger

When a new `.java` file is created.

## Checks

1. Verify file is in correct package: `com.example.skillora_platform.{module}.{layer}`
2. Verify naming conventions:
   - Entity: `{Name}.java` (singular)
   - Service: `{Name}Service.java`
   - Controller: `{Name}Controller.java`
   - Repository: `{Name}Repository.java`
   - DTO: `{Name}Request.java` / `{Name}Response.java`
3. Verify required annotations are present
4. Suggest adding to related test file
