# MCP Tools: Database (MySQL + Redis)

> MCP tool configurations for database management.

## MySQL Tools

### Connect to MySQL
```powershell
mysql -u root -p123456 -h localhost -P 3306 Skillora
```

### Import Schema
```sql
SOURCE e:/Profile/Skillora/skillora_platform/database/skill_database_schema.sql;
```

### Import Seed Data
```sql
SOURCE e:/Profile/Skillora/skillora_platform/database/skillora_seed_data.sql;
```

### Verify Schema
```sql
SHOW TABLES;
DESCRIBE courses;

SELECT 'users' AS tbl, COUNT(*) AS cnt FROM users
UNION ALL SELECT 'courses', COUNT(*) FROM courses
UNION ALL SELECT 'enrollments', COUNT(*) FROM enrollments
UNION ALL SELECT 'orders', COUNT(*) FROM orders;
```

### Query Diagnostics
```sql
EXPLAIN SELECT * FROM courses WHERE status = 'PUBLISHED' ORDER BY published_at DESC;

SELECT table_name, table_rows, data_length/1024/1024 AS size_mb
FROM information_schema.tables
WHERE table_schema = 'Skillora'
ORDER BY data_length DESC;
```

## Redis Tools

### Connect to Redis
```powershell
redis-cli -h localhost -p 6379
```

### Useful Commands
```redis
KEYS *
KEYS courses:*
FLUSHALL
INFO memory
```

### Redis Docker
```powershell
docker run -d --name skillora-redis -p 6379:6379 redis:7-alpine
docker stop skillora-redis
```

## Connection Parameters

### MySQL
| Parameter | Default | Env Var |
|-----------|---------|---------|
| Host | localhost | `DB_HOST` |
| Port | 3306 | `DB_PORT` |
| Database | Skillora | `DB_NAME` |
| Username | root | `DB_USERNAME` |
| Password | 123456 | `DB_PASSWORD` |

### Redis
| Parameter | Default | Env Var |
|-----------|---------|---------|
| Host | localhost | `REDIS_HOST` |
| Port | 6379 | `REDIS_PORT` |
| Timeout | 2000ms | `REDIS_TIMEOUT` |
