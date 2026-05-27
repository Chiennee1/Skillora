# Skill: Refactor Large Service

> Split a large service class into sub-services.

## When to Apply

- Service > 400 lines
- Service handles multiple distinct responsibilities

## Steps

1. Identify distinct responsibilities (CRUD, submission, history, etc.)
2. Create sub-service classes for each responsibility
3. Move relevant methods to sub-services
4. Keep the main service as a facade if needed
5. Update controller to use appropriate sub-service
6. Verify with existing tests

## Example

```
QuizService (600 lines) →
├── QuizCrudService (CRUD operations)
├── QuizSubmissionService (attempt + submit)
└── QuizHistoryService (attempt history + stats)
```
