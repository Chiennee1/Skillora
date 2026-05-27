# Rule: Architecture

> Quy tắc kiến trúc cho dự án Skillora.

## Layered Architecture

All modules MUST follow this layer structure:

```
module/
├── controller/     ← HTTP entry points
├── dto/            ← Request/Response contracts
├── entity/         ← JPA domain models
├── repository/     ← Spring Data JPA interfaces
├── service/        ← Business logic
├── config/         ← Module-specific configuration (optional)
├── event/          ← Domain events (optional)
├── listener/       ← Event listeners (optional)
├── spec/           ← JPA Specifications (optional)
└── util/           ← Module-specific utilities (optional)
```

## Layer Dependency Rules

```
Controller → Service → Repository → Entity
     ↓           ↓
    DTO         DTO (mapping in Service)
```

- **Controllers** MUST NOT access repositories directly
- **Services** MUST NOT return entities to controllers — always map to DTOs
- **Repositories** MUST NOT contain business logic
- **Entities** MUST NOT depend on any other layer
- **Cross-module access**: Service-to-Service only (never Controller-to-Service of another module)

## Package Naming

- Base package: `com.example.skillora_platform`
- Module packages: `com.example.skillora_platform.{module}` (e.g., `com.example.skillora_platform.course`)
- Shared utilities: `com.example.skillora_platform.common`
- Configuration: `com.example.skillora_platform.config`
- Exception handling: `com.example.skillora_platform.exception`

## Service Size Guidelines

| Size | Action |
|------|--------|
| < 200 lines | ✅ Good |
| 200-400 lines | ⚠️ Consider splitting |
| > 400 lines | 🔴 Must refactor — extract sub-services |

## New Module Checklist

When creating a new module, create these directories/files in order:
1. `entity/` — Domain model + enum classes
2. `repository/` — JPA repository interfaces
3. `dto/` — Request and Response DTOs
4. `service/` — Business logic
5. `controller/` — REST endpoints
6. Register endpoints in `SecurityConfig` if public access needed
