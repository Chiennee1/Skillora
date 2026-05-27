# Hook: Pre-Test

> Setup before running tests.

## Trigger

Before `.\mvnw.cmd test`.

## Steps

1. Ensure H2 test profile is configured in `application-test.yml`
2. Verify `ddl-auto=create-drop` for test profile
3. Clean previous test data
4. Run:
   ```powershell
   cd skillora_platform && .\mvnw.cmd test
   ```
