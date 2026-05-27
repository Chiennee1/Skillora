# MCP Tools: Docker

> Docker configuration for Skillora.

## Docker Commands

```powershell
# Start all services
cd skillora_platform && docker-compose up -d

# Rebuild and start
cd skillora_platform && docker-compose up --build -d

# Stop
cd skillora_platform && docker-compose down

# View logs
cd skillora_platform && docker-compose logs -f app

# Check status
docker ps | findstr skillora
```

## docker-compose.yml Services

| Service | Image | Port |
|---------|-------|------|
| app | skillora-platform:latest | 8080 |
| mysql | mysql:8.0 | 3306 |
| redis | redis:7-alpine | 6379 |

## Dockerfile (multi-stage)

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```
