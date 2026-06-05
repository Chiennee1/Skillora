# Command: /migrate

> Create a database migration for Skillora.

## Syntax
```
/migrate <description>
```

## Execution

1. Load `agents/database-engineer.md`
2. Check current schema in `skillora_platform/database/schema_reference_only.sql`
3. Generate Flyway migration file: `V{n}__{description}.sql`
4. Place in `src/main/resources/db/migration/`
5. Update `rules/database.md` table status if new tables
