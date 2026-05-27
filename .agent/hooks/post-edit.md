# Hook: Post-Edit

> Actions to consider after editing a Java file.

## Trigger

After any `.java` file is modified.

## Auto-Suggestions

### 1. Related Test Notification
When editing a **Service** file:
- Suggest running related integration test

### 2. Cache Eviction Reminder
When editing a **cached service** method:
- Remind to check `@CacheEvict` is on all write methods

### 3. SecurityConfig Sync
When editing a **Controller** file:
- Check if new endpoint paths need SecurityConfig update
- Remind about `@PreAuthorize` annotations

### 4. DTO Sync
When editing an **Entity** file:
- Remind to update corresponding Response DTO
- Remind to update `toResponse()` mapper in service

### 5. Import Cleanup
After any edit:
- Remove unused imports
- Organize imports: java → jakarta → springframework → com.example.skillora_platform → lombok

## Mapping: Source → Test

| Source File Pattern | Related Test |
|---------------------|-------------|
| `user/service/*` | AuthIntegrationTest |
| `course/service/*` | CourseIntegrationTest |
| `enrollment/service/*` | EnrollmentFlowTest |
| `quiz/service/*` | QuizSubmissionTest |
| `commerce/service/*` | OrderPaymentTest |
| `review/service/*` | ReviewIntegrationTest |
| `chat/service/*` | ChatbotTest |
