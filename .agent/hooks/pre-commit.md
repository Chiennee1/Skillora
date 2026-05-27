# Hook: Pre-Commit

> Checks to run before committing code.

## Trigger

Before `git commit`.

## Steps

1. Run compilation check:
   ```powershell
   cd skillora_platform && .\mvnw.cmd compile
   ```

2. Check for common issues:
   - No `System.out.println` in production code
   - No hardcoded secrets (API keys, passwords)
   - No `@Autowired` field injection
   - No unused imports

3. Verify commit message format:
   ```
   <type>(<scope>): <description>
   ```
