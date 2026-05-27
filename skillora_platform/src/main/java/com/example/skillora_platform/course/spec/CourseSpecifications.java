package com.example.skillora_platform.course.spec;

import java.util.Locale;

import jakarta.persistence.criteria.Join;

import org.springframework.data.jpa.domain.Specification;

import com.example.skillora_platform.course.entity.Category;
import com.example.skillora_platform.course.entity.Course;
import com.example.skillora_platform.course.entity.CourseLevel;
import com.example.skillora_platform.course.entity.CourseStatus;

public final class CourseSpecifications {

    private CourseSpecifications() {
    }

    public static Specification<Course> publicCatalog(String search, CourseLevel level, Integer categoryId) {
        Specification<Course> specification = published().and(notDeleted());
        Specification<Course> searchSpecification = search(search);
        Specification<Course> levelSpecification = level(level);
        Specification<Course> categorySpecification = category(categoryId);
        if (searchSpecification != null) {
            specification = specification.and(searchSpecification);
        }
        if (levelSpecification != null) {
            specification = specification.and(levelSpecification);
        }
        if (categorySpecification != null) {
            specification = specification.and(categorySpecification);
        }
        return specification;
    }

    private static Specification<Course> published() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), CourseStatus.PUBLISHED);
    }

    private static Specification<Course> notDeleted() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt"));
    }

    private static Specification<Course> search(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("subtitle")), pattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
        );
    }

    private static Specification<Course> level(CourseLevel level) {
        if (level == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("level"), level);
    }

    private static Specification<Course> category(Integer categoryId) {
        if (categoryId == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            if (query != null) {
                query.distinct(true);
            }
            Join<Course, Category> categories = root.join("categories");
            return criteriaBuilder.equal(categories.get("id"), categoryId);
        };
    }
}
