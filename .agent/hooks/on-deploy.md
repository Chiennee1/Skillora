# Hook: On-Deploy

> Deployment preparation checks.

## Trigger

Before deploying to any environment.

## Pre-Deploy Checklist

1. All tests pass:
   ```powershell
   cd skillora_platform && .\mvnw.cmd test
   ```

2. Build successful:
   ```powershell
   cd skillora_platform && .\mvnw.cmd clean package -DskipTests
   ```

3. Security checks:
   - [ ] No hardcoded secrets in code
   - [ ] `.env` file configured for target environment
   - [ ] CORS configured for production origins
   - [ ] JWT secret is strong and from env var

4. Database:
   - [ ] Schema is up to date
   - [ ] Seed data applied if needed

5. Docker (if applicable):
   ```powershell
   cd skillora_platform && docker-compose up --build -d
   cd skillora_platform && docker-compose logs -f app
   ```
