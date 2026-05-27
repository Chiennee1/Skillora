# Skill: Add Flyway Migration

> Create a versioned Flyway migration.

## Naming Convention

```
V{version}__{description}.sql
```
Example: `V2__add_notifications_table.sql`

## Placement

`skillora_platform/src/main/resources/db/migration/`

## Template

```sql
-- V{N}__{description}.sql
-- Description: {what this migration does}

CREATE TABLE {table_name} (
    id BIGINT NOT NULL AUTO_INCREMENT,
    -- columns
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_{table} PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## Baseline

Current schema: `skillora_platform/database/skill_database_schema.sql` → `V1__baseline.sql`
