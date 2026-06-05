# Rule: Database

> Database conventions and rules for Skillora (MySQL 8+ / H2 test).

## Schema Management

### Current State
- Schema file: `skillora_platform/database/schema_reference_only.sql`
- Seed data: `skillora_platform/database/skillora_seed_data.sql`
- JPA: `ddl-auto=validate` (does NOT create/modify tables)
- No Flyway/Liquibase (yet)

### Target State
- Add Flyway for versioned migrations
- Migration files: `src/main/resources/db/migration/V{n}__{description}.sql`
- Baseline: current `schema_reference_only.sql` â†’ `V1__baseline.sql`

## Table Naming

| Convention | Example |
|-----------|---------|
| Plural, snake_case | `courses`, `user_roles` |
| Join tables | `{table1}_{table2}` â†’ `course_categories` |
| Stats/derived tables | `{entity}_stats` â†’ `course_stats` |
| Timestamps | `created_at`, `updated_at` as `TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP` |

## Entity Conventions

```java
// All entities extend BaseEntity
@MappedSuperclass
@Getter @Setter
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

// Entity example
@Entity
@Table(name = "courses")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course extends BaseEntity {
    // Fields match DB columns exactly
}
```

## Repository Conventions

```java
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByStatusOrderByPublishedAtDesc(CourseStatus status);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
    long countByStatus(CourseStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status")
    BigDecimal sumTotalAmountByStatus(@Param("status") OrderStatus status);
}
```

## Transaction Boundaries

```java
@Transactional(readOnly = true)
public CourseResponse getById(Long id) { ... }

@Transactional
public CourseResponse create(CourseCreateRequest req) { ... }

// Never put @Transactional on controller
```

## Indexing Strategy

- Primary key: auto `BIGINT AUTO_INCREMENT`
- Foreign keys: always indexed
- Search columns: add index (`idx_{table}_{column}`)
- Fulltext: use `FULLTEXT INDEX` for title/description search
- Composite index: order by selectivity (most selective first)

## Data Integrity

- Use `UNIQUE` constraints for business uniqueness (email, slug)
- Use `CHECK` constraints for value ranges (rating BETWEEN 1 AND 5)
- Use `ON DELETE CASCADE` for child tables
- Use `ON DELETE SET NULL` for optional references

## Current Tables (30+ in schema)

| Domain | Tables | Status |
|--------|--------|--------|
| User/Auth | users, roles, user_roles, user_profiles, instructor_profiles, refresh_tokens, password_reset_tokens | ðŸ”² Entity not created |
| Course Catalog | categories, courses, course_categories, course_requirements, course_outcomes | ðŸ”² Entity not created |
| Course Content | sections, lessons, lesson_videos, lesson_video_variants, lesson_resources | ðŸ”² Entity not created |
| Commerce | wishlists, carts, cart_items, coupons, orders, order_items, payment_transactions | ðŸ”² Entity not created |
| Enrollment | enrollments, lesson_progress, course_certificates | ðŸ”² Entity not created |
| Quiz | quizzes, questions, answer_options, quiz_attempts, quiz_attempt_answers, quiz_attempt_answer_options | ðŸ”² Entity not created |
| Assignment | assignments, assignment_submissions | ðŸ”² Entity not created |
| Social | reviews, review_likes | ðŸ”² Entity not created |
| Chat | chat_conversations, chat_messages | ðŸ”² Entity not created |
| Notification | notifications | ðŸ”² Entity not created |
| Analytics | course_stats, audit_logs | ðŸ”² Entity not created |

## DB Views

| View | Purpose | Used in Code? |
|------|---------|--------------|
| `v_course_detail` | Course + instructor + stats | ðŸ”² Not yet |
| `v_enrollment_progress` | Progress calculation | ðŸ”² Not yet |
| `v_student_courses` | Student enrolled courses | ðŸ”² Not yet |
