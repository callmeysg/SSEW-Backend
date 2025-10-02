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
package com.singhtwenty2.ssew_core.data.repository;

import com.singhtwenty2.ssew_core.data.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByNameAndIdNot(String name, UUID id);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    List<Category> findByIsActiveOrderByDisplayOrderAsc(Boolean isActive);

    Page<Category> findByIsActive(Boolean isActive, Pageable pageable);

    @Query("SELECT c FROM Category c WHERE " +
           "(:isActive IS NULL OR c.isActive = :isActive) AND " +
           "(:searchTerm IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Category> findCategoriesWithFilters(
            @Param("isActive") Boolean isActive,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    @Query("SELECT COUNT(DISTINCT m) FROM Manufacturer m JOIN m.categories c WHERE c.id = :categoryId")
    Long countManufacturersByCategoryId(@Param("categoryId") UUID categoryId);

    Optional<Category> findTopByOrderByDisplayOrderDesc();

    @Query("SELECT c FROM Category c JOIN FETCH c.manufacturers WHERE c.id = :categoryId")
    Optional<Category> findByIdWithManufacturers(@Param("categoryId") UUID categoryId);

    @Query("SELECT c FROM Category c JOIN FETCH c.manufacturers m WHERE c.isActive = :isActive ORDER BY c.displayOrder ASC")
    List<Category> findByIsActiveWithManufacturers(@Param("isActive") Boolean isActive);

    @Query("SELECT DISTINCT c FROM Category c JOIN c.manufacturers m WHERE m.id = :manufacturerId")
    List<Category> findCategoriesByManufacturerId(@Param("manufacturerId") UUID manufacturerId);
}