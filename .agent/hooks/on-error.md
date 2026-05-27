# Hook: On-Error

> Error handling and debugging workflow.

## Trigger

When a compilation or runtime error occurs.

## Steps

1. Read the full error stack trace
2. Identify error type:
   - **Compilation**: Missing imports, type mismatches, syntax errors
   - **Bean creation**: Missing annotations, circular dependencies
   - **DB**: Schema mismatch, connection refused
   - **Auth**: JWT errors, 401/403 responses
3. Check `skills/debug-startup.md` for common startup issues
4. Check `skills/debug-api-error.md` for API errors
5. Fix and recompile: `cd skillora_platform && .\mvnw.cmd compile`
