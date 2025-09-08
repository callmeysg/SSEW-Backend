package com.singhtwenty2.ssew_core.service.catalogue;

import com.singhtwenty2.ssew_core.data.dto.catalogue.CategoryDTO.CategoryResponse;
import com.singhtwenty2.ssew_core.data.dto.catalogue.CategoryDTO.CreateCategoryRequest;
import com.singhtwenty2.ssew_core.data.dto.catalogue.CategoryDTO.UpdateCategoryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest createRequest);

    CategoryResponse getCategoryById(String categoryId);

    CategoryResponse getCategoryBySlug(String slug);

    Page<CategoryResponse> getAllCategories(Pageable pageable);

    Page<CategoryResponse> getActiveCategories(Pageable pageable);

    Page<CategoryResponse> searchCategories(String searchTerm, Boolean isActive, Pageable pageable);

    List<CategoryResponse> getActiveCategoriesOrderedByDisplayOrder();

    CategoryResponse updateCategory(String categoryId, UpdateCategoryRequest updateRequest);

    void deleteCategory(String categoryId);

    void toggleCategoryStatus(String categoryId);
}