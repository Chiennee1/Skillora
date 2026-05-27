# MCP Tools: Spring Boot

> Spring Boot build and run tools for Skillora.

## Maven Commands

```powershell
# Compile
cd skillora_platform && .\mvnw.cmd compile

# Run (default profile)
cd skillora_platform && .\mvnw.cmd spring-boot:run

# Run (dev profile)
cd skillora_platform && .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"

# Test
cd skillora_platform && .\mvnw.cmd test

# Test single class
cd skillora_platform && .\mvnw.cmd test -Dtest=CourseIntegrationTest

# Package
cd skillora_platform && .\mvnw.cmd clean package -DskipTests

# Dependency tree
cd skillora_platform && .\mvnw.cmd dependency:tree
```

## Key Dependencies (pom.xml)

| Dependency | Purpose |
|-----------|---------|
| spring-boot-starter-web | REST API |
| spring-boot-starter-security | Security + JWT |
| spring-boot-starter-oauth2-client | Google OAuth2 |
| spring-boot-starter-validation | Bean validation |
| spring-boot-starter-data-jpa | JPA + Hibernate |
| spring-boot-starter-data-redis | Redis cache |
| spring-boot-devtools | Hot reload |
| mysql-connector-j | MySQL driver |
| lombok | Boilerplate reduction |
| spring-boot-starter-test | Testing |
| spring-security-test | Security testing |
