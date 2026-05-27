# Command: /deploy

> Build and deploy Skillora application.

## Syntax
```
/deploy [--docker] [--skip-tests]
```

## Execution Flow

### Local Build
```powershell
cd skillora_platform && .\mvnw.cmd clean package -DskipTests
```

### Docker Build & Deploy
```powershell
cd skillora_platform && docker-compose up --build -d
```

### Verify
```powershell
# Check health
curl http://localhost:8080/actuator/health

# Check logs
cd skillora_platform && docker-compose logs -f app
```

## Pre-deploy Checklist
- [ ] All tests pass
- [ ] No hardcoded secrets
- [ ] `application.properties` configured for target env
- [ ] Database schema up to date
- [ ] `.env` file configured
