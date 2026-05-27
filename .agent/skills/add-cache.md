# Skill: Add Cache

> Add Redis caching to a service.

## Template

```java
@Cacheable(value = Constants.CACHE_{ENTITY}_KEY, key = "#root.methodName")
@Transactional(readOnly = true)
public List<{Entity}Response> getAll() { ... }

@CacheEvict(value = Constants.CACHE_{ENTITY}_KEY, allEntries = true)
@Transactional
public {Entity}Response create(...) { ... }
```

## Constants

```java
public static final String CACHE_COURSES_PUBLISHED = "courses:published";
```

## Redis Config

Ensure `RedisCacheConfig` is set up with TTL (default 5 min).
