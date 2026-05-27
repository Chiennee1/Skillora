# Rule: Git Workflow

> Quy tắc Git cho dự án Skillora.

## Branch Strategy

| Branch | Purpose | Naming |
|--------|---------|--------|
| `main` | Production-ready | — |
| `develop` | Integration branch | — |
| `feature/*` | New features | `feature/user-auth`, `feature/course-crud` |
| `bugfix/*` | Bug fixes | `bugfix/login-error` |
| `hotfix/*` | Production fixes | `hotfix/security-patch` |

## Commit Messages

```
<type>(<scope>): <description>

feat(user): add JWT authentication
fix(course): fix slug generation for unicode titles
refactor(quiz): split QuizService into sub-services
docs(readme): update setup instructions
test(enrollment): add progress tracking tests
chore(deps): upgrade Spring Boot to 3.5.14
```

## Types

| Type | Usage |
|------|-------|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code restructuring (no behavior change) |
| `docs` | Documentation |
| `test` | Tests |
| `chore` | Build, deps, CI |
| `style` | Formatting (no logic change) |

## PR Rules

- PR title follows commit message format
- At least 1 reviewer
- All CI checks must pass
- No merge conflicts
- Squash merge to `develop`
