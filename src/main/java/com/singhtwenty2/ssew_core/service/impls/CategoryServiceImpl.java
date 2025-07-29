package com.singhtwenty2.ssew_core.service.impls;

import com.singhtwenty2.ssew_core.data.entity.Category;
import com.singhtwenty2.ssew_core.data.repository.CategoryRepository;
import com.singhtwenty2.ssew_core.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.singhtwenty2.ssew_core.data.dto.catalog_management.CategoryDTO.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse createCategory(CreateCategoryRequest createRequest) {
        log.debug("Creating new category with name: {}", createRequest.getName());

        validateCreateRequest(createRequest);

        String slug = generateSlug(createRequest.getName());
        ensureUniqueSlug(slug, null);

        Category savedCategory = saveCategoryData(createRequest, slug);

        log.info("Category created successfully with ID: {}", savedCategory.getId());
        return buildCategoryResponse(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(String categoryId) {
        log.debug("Fetching category by ID: {}", categoryId);

        Category category = findCategoryById(categoryId);
        return buildCategoryResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        log.debug("Fetching category by slug: {}", slug);

        if (!StringUtils.hasText(slug)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug is required");
        }

        Optional<Category> categoryOptional = categoryRepository.findBySlug(slug);
        if (categoryOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with slug: " + slug);
        }

        return buildCategoryResponse(categoryOptional.get());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        log.debug("Fetching all categories with pagination");

        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        return categoryPage.map(this::buildCategoryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getActiveCategories(Pageable pageable) {
        log.debug("Fetching active categories with pagination");

        Page<Category> categoryPage = categoryRepository.findByIsActive(true, pageable);
        return categoryPage.map(this::buildCategoryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> searchCategories(String searchTerm, Boolean isActive, Pageable pageable) {
        log.debug("Searching categories with term: {}, isActive: {}", searchTerm, isActive);

        Page<Category> categoryPage = categoryRepository.findCategoriesWithFilters(
                isActive,
                StringUtils.hasText(searchTerm) ? searchTerm.trim() : null,
                pageable
        );
        return categoryPage.map(this::buildCategoryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategoriesOrderedByDisplayOrder() {
        log.debug("Fetching active categories ordered by display order");

        List<Category> categories = categoryRepository.findByIsActiveOrderByDisplayOrderAsc(true);
        return categories.stream()
                .map(this::buildCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse updateCategory(String categoryId, UpdateCategoryRequest updateRequest) {
        log.debug("Updating category with ID: {}", categoryId);

        Category category = findCategoryById(categoryId);
        validateUpdateRequest(updateRequest, category);

        boolean needsNewSlug = false;
        if (StringUtils.hasText(updateRequest.getName()) &&
            !updateRequest.getName().trim().equals(category.getName())) {
            category.setName(updateRequest.getName().trim());
            needsNewSlug = true;
        }

        if (needsNewSlug) {
            String newSlug = generateSlug(category.getName());
            ensureUniqueSlug(newSlug, category.getId());
            category.setSlug(newSlug);
        }

        if (updateRequest.getDescription() != null) {
            category.setDescription(StringUtils.hasText(updateRequest.getDescription()) ?
                    updateRequest.getDescription().trim() : null);
        }

        if (updateRequest.getDisplay_order() != null) {
            category.setDisplayOrder(updateRequest.getDisplay_order());
        }

        if (updateRequest.getIs_active() != null) {
            category.setIsActive(updateRequest.getIs_active());
        }

        if (updateRequest.getMeta_title() != null) {
            category.setMetaTitle(StringUtils.hasText(updateRequest.getMeta_title()) ?
                    updateRequest.getMeta_title().trim() : null);
        }

        if (updateRequest.getMeta_description() != null) {
            category.setMetaDescription(StringUtils.hasText(updateRequest.getMeta_description()) ?
                    updateRequest.getMeta_description().trim() : null);
        }

        category.setUpdatedAt(LocalDateTime.now());

        Category updatedCategory = categoryRepository.save(category);

        log.info("Category updated successfully with ID: {}", updatedCategory.getId());
        return buildCategoryResponse(updatedCategory);
    }

    @Override
    public void deleteCategory(String categoryId) {
        log.debug("Deleting category with ID: {}", categoryId);

        Category category = findCategoryById(categoryId);

        Long brandCount = categoryRepository.countBrandsByCategoryId(category.getId());
        if (brandCount > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete category. It has " + brandCount + " associated brands"
            );
        }

        categoryRepository.delete(category);

        log.info("Category deleted successfully with ID: {}", categoryId);
    }

    @Override
    public void activateCategory(String categoryId) {
        log.debug("Activating category with ID: {}", categoryId);

        Category category = findCategoryById(categoryId);
        category.setIsActive(true);
        category.setUpdatedAt(LocalDateTime.now());

        categoryRepository.save(category);

        log.info("Category activated successfully with ID: {}", categoryId);
    }

    @Override
    public void deactivateCategory(String categoryId) {
        log.debug("Deactivating category with ID: {}", categoryId);

        Category category = findCategoryById(categoryId);
        category.setIsActive(false);
        category.setUpdatedAt(LocalDateTime.now());

        categoryRepository.save(category);

        log.info("Category deactivated successfully with ID: {}", categoryId);
    }

    private void validateCreateRequest(CreateCategoryRequest request) {
        if (!StringUtils.hasText(request.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name is required");
        }

        String trimmedName = request.getName().trim();
        if (trimmedName.length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name must be at least 2 characters long");
        }

        if (categoryRepository.existsByName(trimmedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category with this name already exists");
        }
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private void ensureUniqueSlug(String baseSlug, UUID excludeCategoryId) {
        String slug = baseSlug;
        int counter = 1;

        while (true) {
            boolean exists = excludeCategoryId != null ?
                    categoryRepository.existsBySlugAndIdNot(slug, excludeCategoryId) :
                    categoryRepository.existsBySlug(slug);

            if (!exists) {
                break;
            }

            slug = baseSlug + "-" + counter;
            counter++;
        }
    }

    private CategoryResponse buildCategoryResponse(Category category) {
        Long brandCount = categoryRepository.countBrandsByCategoryId(category.getId());

        return CategoryResponse.builder()
                .category_id(category.getId().toString())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .display_order(category.getDisplayOrder())
                .is_active(category.getIsActive())
                .meta_title(category.getMetaTitle())
                .meta_description(category.getMetaDescription())
                .created_at(category.getCreatedAt().toString())
                .updated_at(category.getUpdatedAt().toString())
                .brand_count(brandCount)
                .build();
    }

    private Integer getNextDisplayOrder() {
        Optional<Category> lastCategory = categoryRepository.findTopByOrderByDisplayOrderDesc();
        return lastCategory.map(category -> category.getDisplayOrder() + 1).orElse(1);
    }

    private Category saveCategoryData(CreateCategoryRequest createRequest, String slug) {
        Category category = new Category();
        category.setName(createRequest.getName().trim());
        category.setSlug(slug);
        category.setDescription(StringUtils.hasText(createRequest.getDescription()) ?
                createRequest.getDescription().trim() : null);
        category.setDisplayOrder(createRequest.getDisplay_order() != null ?
                createRequest.getDisplay_order() : getNextDisplayOrder());
        category.setIsActive(true);
        category.setMetaTitle(StringUtils.hasText(createRequest.getMeta_title()) ?
                createRequest.getMeta_title().trim() : null);
        category.setMetaDescription(StringUtils.hasText(createRequest.getMeta_description()) ?
                createRequest.getMeta_description().trim() : null);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(category);
    }

    private Category findCategoryById(String categoryId) {
        try {
            UUID uuid = UUID.fromString(categoryId);
            Optional<Category> categoryOptional = categoryRepository.findById(uuid);

            if (categoryOptional.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + categoryId);
            }

            return categoryOptional.get();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category ID format");
        }
    }

    private void validateUpdateRequest(UpdateCategoryRequest request, Category existingCategory) {
        if (StringUtils.hasText(request.getName())) {
            String trimmedName = request.getName().trim();
            if (trimmedName.length() < 2) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name must be at least 2 characters long");
            }

            if (categoryRepository.existsByNameAndIdNot(trimmedName, existingCategory.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Category with this name already exists");
            }
        }
    }
}
