# Command: /test

> Run tests for Skillora.

## Syntax
```
/test [--all] [--module <module>] [--class <TestClass>] [--coverage]
```

## Execution

### All tests
```powershell
cd skillora_platform && .\mvnw.cmd test
```

### Single test class
```powershell
cd skillora_platform && .\mvnw.cmd test -Dtest=CourseIntegrationTest
```

### With coverage (when JaCoCo added)
```powershell
cd skillora_platform && .\mvnw.cmd verify
```

## Test → Module Mapping

| Module | Test Classes |
|--------|-------------|
| user | AuthIntegrationTest, UserProfileTest |
| course | CourseIntegrationTest, SectionLessonTest |
| enrollment | EnrollmentFlowTest |
| quiz | QuizSubmissionTest |
| commerce | OrderPaymentTest |
| review | ReviewIntegrationTest |
| chat | ChatbotTest |
