# Command: /status

> Show current project status for Skillora.

## Syntax
```
/status
```

## Output

1. Load `context/project-overview.md` — show completion status
2. Count Java files in `skillora_platform/src/main/java/`
3. Count test files in `skillora_platform/src/test/java/`
4. Check `memory.md` — show known issues
5. Check `context/tech-debt.md` — show critical items
6. Run `cd skillora_platform && .\mvnw.cmd compile` — report build status
