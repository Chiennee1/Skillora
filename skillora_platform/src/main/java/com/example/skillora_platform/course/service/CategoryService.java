package com.example.skillora_platform.course.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.skillora_platform.common.SlugUtils;
import com.example.skillora_platform.course.dto.CategoryCreateRequest;
import com.example.skillora_platform.course.dto.CategoryResponse;
import com.example.skillora_platform.course.dto.CategoryUpdateRequest;
import com.example.skillora_platform.course.entity.Category;
import com.example.skillora_platform.course.repository.CategoryRepository;
import com.example.skillora_platform.exception.BusinessException;
import com.example.skillora_platform.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> listActive() {
        return categoryRepository.findActiveOrdered().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryCreateRequest request) {
        Category category = Category.builder()
                .name(request.getName().trim())
                .slug(generateUniqueSlug(request.getName(), null))
                .parent(resolveParent(request.getParentId(), null))
                .iconUrl(trimToNull(request.getIconUrl()))
                .orderIndex(defaultInt(request.getOrderIndex()))
                .active(true)
                .build();
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Integer id, CategoryUpdateRequest request) {
        Category category = findById(id);
        category.setName(request.getName().trim());
        category.setSlug(generateUniqueSlug(request.getName(), id));
        category.setParent(resolveParent(request.getParentId(), id));
        category.setIconUrl(trimToNull(request.getIconUrl()));
        category.setOrderIndex(defaultInt(request.getOrderIndex()));
        category.setActive(request.getActive() == null || request.getActive());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deactivate(Integer id) {
        Category category = findById(id);
        category.setActive(false);
        categoryRepository.save(category);
    }

    private Category findById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private Category resolveParent(Integer parentId, Integer currentId) {
        if (parentId == null) {
            return null;
        }
        if (parentId.equals(currentId)) {
            throw new BusinessException("Category cannot be its own parent", HttpStatus.BAD_REQUEST);
        }
        return categoryRepository.findByIdAndActiveTrue(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + parentId));
    }

    private String generateUniqueSlug(String source, Integer currentId) {
        String baseSlug = SlugUtils.toSlug(source);
        String candidate = baseSlug;
        int suffix = 2;
        while (currentId == null
                ? categoryRepository.existsBySlug(candidate)
                : categoryRepository.existsBySlugAndIdNot(candidate, currentId)) {
            candidate = baseSlug + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private CategoryResponse toResponse(Category category) {
        Category parent = category.getParent();
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(parent == null ? null : parent.getId())
                .parentName(parent == null ? null : parent.getName())
                .iconUrl(category.getIconUrl())
                .orderIndex(category.getOrderIndex())
                .active(category.isActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
