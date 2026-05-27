# Skillora — Local Agent Config (PRIVATE — DO NOT COMMIT)

## Local Environment

- **OS**: Windows
- **Java**: JDK 17
- **IDE**: VS Code / IntelliJ IDEA
- **DB Client**: MySQL Workbench / DBeaver
- **Project Path**: `e:\Profile\Skillora\skillora_platform`
- **Maven Wrapper**: `.\mvnw.cmd`

## Local Services

| Service | Host | Port |
|---------|------|------|
| MySQL | localhost | 3306 |
| Redis | localhost | 6379 |
| MailHog SMTP | localhost | 1025 |
| MailHog UI | localhost | 8025 |
| App (dev) | localhost | 8080 |
| Frontend (Vite) | localhost | 5173 |

## Local DB Credentials
<!-- Thay đổi theo máy local của bạn -->
- Username: `root`
- Password: `123456`
- Schema: `Skillora`

## Personal Preferences

- Prefer Vietnamese comments in complex business logic
- Use English for all public API docs and README
- Prefer verbose logging during development
- Run tests with H2 (no MySQL dependency)
