# Skill: Debug Startup

> Debug Spring Boot startup failures.

## Common Startup Issues

### Port 8080 Already in Use
```powershell
Get-Process -Name java | Stop-Process -Force
# Or change port in application.properties:
# server.port=8081
```

### DB Connection Failed
```powershell
# Verify MySQL is running
mysql -u root -p123456 -h localhost -P 3306 Skillora
```

### Entity-Schema Mismatch (ddl-auto=validate)
```
# Import schema first:
mysql -u root -p123456 Skillora < skillora_platform/database/skill_database_schema.sql
```

### Bean Creation Error
- Check for missing `@Service`, `@Repository`, `@Component` annotations
- Check for circular dependencies
- Check import statements (jakarta not javax)
