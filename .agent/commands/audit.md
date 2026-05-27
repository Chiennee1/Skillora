# Command: /audit

> Run a security/quality audit for Skillora.

## Syntax
```
/audit [--security] [--quality] [--performance]
```

## Execution

1. Load `agents/security-auditor.md`
2. Scan for hardcoded secrets, missing `@PreAuthorize`, CORS wildcards
3. Check `rules/security.md` compliance
4. Report findings with severity levels
5. Update `context/tech-debt.md` with new findings
