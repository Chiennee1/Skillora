package com.example.skillora_platform.course.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.EntityManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.common.PageResponse;
import com.example.skillora_platform.common.SlugUtils;
import com.example.skillora_platform.course.dto.CategoryResponse;
import com.example.skillora_platform.course.dto.CourseCreateRequest;
import com.example.skillora_platform.course.dto.CourseResponse;
import com.example.skillora_platform.course.dto.CourseSummaryResponse;
import com.example.skillora_platform.course.dto.CourseUpdateRequest;
import com.example.skillora_platform.course.entity.Category;
import com.example.skillora_platform.course.entity.Course;
import com.example.skillora_platform.course.entity.CourseLevel;
import com.example.skillora_platform.course.entity.CourseOutcome;
import com.example.skillora_platform.course.entity.CourseRequirement;
import com.example.skillora_platform.course.entity.CourseStatus;
import com.example.skillora_platform.course.repository.CategoryRepository;
import com.example.skillora_platform.course.repository.CourseRepository;
import com.example.skillora_platform.course.spec.CourseSpecifications;
import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.exception.ResourceNotFoundException;
import com.example.skillora_platform.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseService {

    private static final String DEFAULT_LANGUAGE = "vi";
    private static final String DEFAULT_CURRENCY = "VND";

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final CoursePermissionService permissionService;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> listPublic(
            String search,
            CourseLevel level,
            Integer categoryId,
            int page,
            int size,
            String sort
    ) {
        Page<CourseSummaryResponse> courses = courseRepository.findAll(
                CourseSpecifications.publicCatalog(search, level, categoryId),
                pageable(page, size, sort)
        ).map(this::toSummaryResponse);
        return PageResponse.from(courses);
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> listMine(String actorEmail, int page, int size, String sort) {
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        Page<CourseSummaryResponse> courses = courseRepository
                .findByInstructorAndDeletedAtIsNull(actor, pageable(page, size, sort))
                .map(this::toSummaryResponse);
        return PageResponse.from(courses);
    }

    @Transactional(readOnly = true)
    public CourseResponse getByIdOrSlug(String idOrSlug, String actorEmail) {
        Course course = resolveByIdOrSlug(idOrSlug);
        if (course.getStatus() == CourseStatus.PUBLISHED && course.getDeletedAt() == null) {
            return toResponse(course);
        }

        User actor = permissionService.requireActor(actorEmail);
        permissionService.requireOwnerOrAdmin(course, actor);
        return toResponse(course);
    }

    @Transactional
    public CourseResponse create(CourseCreateRequest request, String actorEmail) {
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        User instructor = resolveInstructor(actor, request.getInstructorId());
        validatePricing(request.getPrice(), request.getDiscountPrice());

        Course course = Course.builder()
                .instructor(instructor)
                .title(request.getTitle().trim())
                .slug(generateUniqueSlug(request.getTitle(), null))
                .subtitle(trimToNull(request.getSubtitle()))
                .description(trimToNull(request.getDescription()))
                .thumbnailUrl(trimToNull(request.getThumbnailUrl()))
                .previewVideoUrl(trimToNull(request.getPreviewVideoUrl()))
                .level(defaultLevel(request.getLevel()))
                .language(defaultString(request.getLanguage(), DEFAULT_LANGUAGE))
                .price(defaultMoney(request.getPrice()))
                .discountPrice(request.getDiscountPrice())
                .currency(defaultString(request.getCurrency(), DEFAULT_CURRENCY).toUpperCase())
                .status(CourseStatus.DRAFT)
                .totalLessons(0)
                .totalDurationSeconds(0)
                .totalEnrollments(0)
                .avgRating(BigDecimal.ZERO)
                .totalReviews(0)
                .categories(resolveCategories(request.getCategoryIds()))
                .build();
        applyRequirements(course, request.getRequirements());
        applyOutcomes(course, request.getOutcomes());
        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse update(Long id, CourseUpdateRequest request, String actorEmail) {
        Course course = findActiveCourse(id);
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        permissionService.requireOwnerOrAdmin(course, actor);
        validatePricing(request.getPrice(), request.getDiscountPrice());

        course.setTitle(request.getTitle().trim());
        course.setSlug(generateUniqueSlug(request.getTitle(), id));
        course.setSubtitle(trimToNull(request.getSubtitle()));
        course.setDescription(trimToNull(request.getDescription()));
        course.setThumbnailUrl(trimToNull(request.getThumbnailUrl()));
        course.setPreviewVideoUrl(trimToNull(request.getPreviewVideoUrl()));
        course.setLevel(defaultLevel(request.getLevel()));
        course.setLanguage(defaultString(request.getLanguage(), DEFAULT_LANGUAGE));
        course.setPrice(defaultMoney(request.getPrice()));
        course.setDiscountPrice(request.getDiscountPrice());
        course.setCurrency(defaultString(request.getCurrency(), DEFAULT_CURRENCY).toUpperCase());
        course.getCategories().clear();
        course.getCategories().addAll(resolveCategories(request.getCategoryIds()));
        course.getRequirements().clear();
        course.getOutcomes().clear();
        entityManager.flush();
        applyRequirements(course, request.getRequirements());
        applyOutcomes(course, request.getOutcomes());
        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public void delete(Long id, String actorEmail) {
        Course course = findActiveCourse(id);
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        permissionService.requireOwnerOrAdmin(course, actor);
        course.setDeletedAt(LocalDateTime.now());
        courseRepository.save(course);
    }

    @Transactional
    public CourseResponse publish(Long id, String actorEmail) {
        Course course = findActiveCourse(id);
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        permissionService.requireOwnerOrAdmin(course, actor);
        course.setStatus(CourseStatus.PUBLISHED);
        course.setPublishedAt(LocalDateTime.now());
        course.setRejectReason(null);
        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse archive(Long id, String actorEmail) {
        Course course = findActiveCourse(id);
        User actor = permissionService.requireInstructorOrAdmin(actorEmail);
        permissionService.requireOwnerOrAdmin(course, actor);
        course.setStatus(CourseStatus.ARCHIVED);
        return toResponse(courseRepository.save(course));
    }

    @Transactional(readOnly = true)
    public Course findActiveCourse(Long id) {
        return courseRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    private Course resolveByIdOrSlug(String idOrSlug) {
        if (idOrSlug.matches("\\d+")) {
            return findActiveCourse(Long.valueOf(idOrSlug));
        }
        return courseRepository.findBySlugAndDeletedAtIsNull(idOrSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + idOrSlug));
    }

    private User resolveInstructor(User actor, Long requestedInstructorId) {
        if (!permissionService.isAdmin(actor)) {
            return actor;
        }
        if (requestedInstructorId == null) {
            return actor;
        }
        User instructor = permissionService.requireActiveUser(requestedInstructorId);
        if (!permissionService.isInstructor(instructor) && !permissionService.isAdmin(instructor)) {
            throw new BusinessException("Instructor user must have INSTRUCTOR or ADMIN role", HttpStatus.BAD_REQUEST);
        }
        return instructor;
    }

    private void applyRequirements(Course course, List<String> requirements) {
        course.getRequirements().clear();
        if (requirements == null) {
            return;
        }
        int order = 1;
        for (String requirement : cleanList(requirements)) {
            course.getRequirements().add(CourseRequirement.builder()
                    .course(course)
                    .description(requirement)
                    .orderIndex(order++)
                    .build());
        }
    }

    private void applyOutcomes(Course course, List<String> outcomes) {
        course.getOutcomes().clear();
        if (outcomes == null) {
            return;
        }
        int order = 1;
        for (String outcome : cleanList(outcomes)) {
            course.getOutcomes().add(CourseOutcome.builder()
                    .course(course)
                    .description(outcome)
                    .orderIndex(order++)
                    .build());
        }
    }

    private Set<Category> resolveCategories(List<Integer> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        Set<Category> categories = new LinkedHashSet<>();
        for (Integer categoryId : categoryIds.stream().distinct().toList()) {
            Category category = categoryRepository.findByIdAndActiveTrue(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
            categories.add(category);
        }
        return categories;
    }

    private void validatePricing(BigDecimal price, BigDecimal discountPrice) {
        BigDecimal effectivePrice = defaultMoney(price);
        if (discountPrice != null && discountPrice.compareTo(effectivePrice) > 0) {
            throw new BusinessException("Discount price cannot be greater than price", HttpStatus.BAD_REQUEST);
        }
    }

    private Pageable pageable(int page, int size, String sort) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, Constants.MAX_PAGE_SIZE));
        return PageRequest.of(safePage, safeSize, parseSort(sort));
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sort.split(",");
        String property = parts[0].isBlank() ? "createdAt" : parts[0].trim();
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }

    private String generateUniqueSlug(String source, Long currentId) {
        String baseSlug = SlugUtils.toSlug(source);
        String candidate = baseSlug;
        int suffix = 2;
        while (currentId == null
                ? courseRepository.existsBySlug(candidate)
                : courseRepository.existsBySlugAndIdNot(candidate, currentId)) {
            candidate = baseSlug + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private CourseSummaryResponse toSummaryResponse(Course course) {
        return CourseSummaryResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .slug(course.getSlug())
                .subtitle(course.getSubtitle())
                .thumbnailUrl(course.getThumbnailUrl())
                .level(course.getLevel())
                .language(course.getLanguage())
                .price(course.getPrice())
                .discountPrice(course.getDiscountPrice())
                .currency(course.getCurrency())
                .status(course.getStatus())
                .instructorId(course.getInstructor().getId())
                .instructorName(course.getInstructor().getFullName())
                .categories(toCategoryResponses(course))
                .totalLessons(course.getTotalLessons())
                .totalDurationSeconds(course.getTotalDurationSeconds())
                .totalEnrollments(course.getTotalEnrollments())
                .avgRating(course.getAvgRating())
                .totalReviews(course.getTotalReviews())
                .publishedAt(course.getPublishedAt())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    private CourseResponse toResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .slug(course.getSlug())
                .subtitle(course.getSubtitle())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .previewVideoUrl(course.getPreviewVideoUrl())
                .level(course.getLevel())
                .language(course.getLanguage())
                .price(course.getPrice())
                .discountPrice(course.getDiscountPrice())
                .currency(course.getCurrency())
                .status(course.getStatus())
                .rejectReason(course.getRejectReason())
                .instructorId(course.getInstructor().getId())
                .instructorName(course.getInstructor().getFullName())
                .categories(toCategoryResponses(course))
                .requirements(course.getRequirements().stream()
                        .sorted(Comparator.comparingInt(CourseRequirement::getOrderIndex))
                        .map(CourseRequirement::getDescription)
                        .toList())
                .outcomes(course.getOutcomes().stream()
                        .sorted(Comparator.comparingInt(CourseOutcome::getOrderIndex))
                        .map(CourseOutcome::getDescription)
                        .toList())
                .totalLessons(course.getTotalLessons())
                .totalDurationSeconds(course.getTotalDurationSeconds())
                .totalEnrollments(course.getTotalEnrollments())
                .avgRating(course.getAvgRating())
                .totalReviews(course.getTotalReviews())
                .publishedAt(course.getPublishedAt())
                .deletedAt(course.getDeletedAt())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    private List<CategoryResponse> toCategoryResponses(Course course) {
        return course.getCategories().stream()
                .sorted(Comparator.comparing(Category::getOrderIndex).thenComparing(Category::getName))
                .map(category -> CategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .slug(category.getSlug())
                        .parentId(category.getParent() == null ? null : category.getParent().getId())
                        .parentName(category.getParent() == null ? null : category.getParent().getName())
                        .iconUrl(category.getIconUrl())
                        .orderIndex(category.getOrderIndex())
                        .active(category.isActive())
                        .createdAt(category.getCreatedAt())
                        .updatedAt(category.getUpdatedAt())
                        .build())
                .toList();
    }

    private List<String> cleanList(List<String> values) {
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .toList();
    }

    private CourseLevel defaultLevel(CourseLevel level) {
        return level == null ? CourseLevel.BEGINNER : level;
    }

    private BigDecimal defaultMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String defaultString(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
