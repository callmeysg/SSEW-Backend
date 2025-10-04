package com.singhtwenty2.commerce_service.controller.catalogue;

import com.singhtwenty2.commerce_service.data.dto.catalogue.CategoryDTO.CategoryResponse;
import com.singhtwenty2.commerce_service.data.dto.catalogue.CategoryDTO.CreateCategoryRequest;
import com.singhtwenty2.commerce_service.data.dto.catalogue.CategoryDTO.UpdateCategoryRequest;
import com.singhtwenty2.commerce_service.data.dto.common.GlobalApiResponse;
import com.singhtwenty2.commerce_service.data.dto.common.PageResponse;
import com.singhtwenty2.commerce_service.service.catalogue.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.singhtwenty2.commerce_service.util.io.NetworkUtils.getClientIP;

@RestController
@RequestMapping("/v1/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest createRequest,
            HttpServletRequest request
    ) {
        log.info("Category creation attempt from IP: {} for name: {}",
                getClientIP(request), createRequest.getName());

        CategoryResponse response = categoryService.createCategory(createRequest);

        log.info("Category created successfully with ID: {}", response.getCategoryId());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                GlobalApiResponse.<CategoryResponse>builder()
                        .success(true)
                        .message("Category created successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<GlobalApiResponse<CategoryResponse>> getCategoryById(
            @PathVariable String categoryId,
            HttpServletRequest request
    ) {
        log.debug("Fetching category by ID: {} from IP: {}", categoryId, getClientIP(request));

        CategoryResponse response = categoryService.getCategoryById(categoryId);

        return ResponseEntity.ok(
                GlobalApiResponse.<CategoryResponse>builder()
                        .success(true)
                        .message("Category retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<GlobalApiResponse<CategoryResponse>> getCategoryBySlug(
            @PathVariable String slug,
            HttpServletRequest request
    ) {
        log.debug("Fetching category by slug: {} from IP: {}", slug, getClientIP(request));

        CategoryResponse response = categoryService.getCategoryBySlug(slug);

        return ResponseEntity.ok(
                GlobalApiResponse.<CategoryResponse>builder()
                        .success(true)
                        .message("Category retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<GlobalApiResponse<PageResponse<CategoryResponse>>> getAllCategories(
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Fetching all categories from IP: {}", getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<CategoryResponse> categoryPage = categoryService.getAllCategories(pageable);
        PageResponse<CategoryResponse> response = PageResponse.from(categoryPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<CategoryResponse>>builder()
                        .success(true)
                        .message("Categories retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/active")
    public ResponseEntity<GlobalApiResponse<PageResponse<CategoryResponse>>> getActiveCategories(
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Fetching active categories from IP: {}", getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<CategoryResponse> categoryPage = categoryService.getActiveCategories(pageable);
        PageResponse<CategoryResponse> response = PageResponse.from(categoryPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<CategoryResponse>>builder()
                        .success(true)
                        .message("Active categories retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<GlobalApiResponse<PageResponse<CategoryResponse>>> searchCategories(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Searching categories with term: {} from IP: {}", searchTerm, getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<CategoryResponse> categoryPage = categoryService.searchCategories(searchTerm, isActive, pageable);
        PageResponse<CategoryResponse> response = PageResponse.from(categoryPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<CategoryResponse>>builder()
                        .success(true)
                        .message("Categories search completed successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/active/ordered")
    public ResponseEntity<GlobalApiResponse<List<CategoryResponse>>> getActiveCategoriesOrderedByDisplayOrder(
            HttpServletRequest request
    ) {
        log.debug("Fetching active categories ordered by display order from IP: {}", getClientIP(request));

        List<CategoryResponse> response = categoryService.getActiveCategoriesOrderedByDisplayOrder();

        return ResponseEntity.ok(
                GlobalApiResponse.<List<CategoryResponse>>builder()
                        .success(true)
                        .message("Active categories retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<CategoryResponse>> updateCategory(
            @PathVariable String categoryId,
            @Valid @RequestBody UpdateCategoryRequest updateRequest,
            HttpServletRequest request
    ) {
        log.info("Category update attempt from IP: {} for ID: {}", getClientIP(request), categoryId);

        CategoryResponse response = categoryService.updateCategory(categoryId, updateRequest);

        log.info("Category updated successfully with ID: {}", categoryId);

        return ResponseEntity.ok(
                GlobalApiResponse.<CategoryResponse>builder()
                        .success(true)
                        .message("Category updated successfully")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<Map<String, Object>>> deleteCategory(
            @PathVariable String categoryId,
            HttpServletRequest request
    ) {
        log.info("Category deletion attempt from IP: {} for ID: {}", getClientIP(request), categoryId);

        categoryService.deleteCategory(categoryId);

        log.info("Category deleted successfully with ID: {}", categoryId);

        return ResponseEntity.ok(
                GlobalApiResponse.<Map<String, Object>>builder()
                        .success(true)
                        .message("Category deleted successfully")
                        .data(null)
                        .build()
        );
    }

    @PatchMapping("/{categoryId}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<Map<String, Object>>> toggleCategoryStatus(
            @PathVariable String categoryId,
            HttpServletRequest request
    ) {
        log.info("Category status toggle attempt from IP: {} for ID: {}", getClientIP(request), categoryId);

        categoryService.toggleCategoryStatus(categoryId);

        log.info("Category status toggled successfully with ID: {}", categoryId);

        return ResponseEntity.ok(
                GlobalApiResponse.<Map<String, Object>>builder()
                        .success(true)
                        .message("Category status toggled successfully")
                        .data(null)
                        .build()
        );
    }
}