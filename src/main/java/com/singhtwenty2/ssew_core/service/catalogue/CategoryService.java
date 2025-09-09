/**
 * Copyright 2025 SSEW Core Service
 * Developer: Aryan Singh (@singhtwenty2)
 * Portfolio: https://singhtwenty2.pages.dev/
 * This file is part of SSEW E-commerce Backend System
 * Licensed under MIT License
 * For commercial use and inquiries: aryansingh.corp@gmail.com
 * @author Aryan Singh (@singhtwenty2)
 * @project SSEW E-commerce Backend System
 * @since 2025
 */
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