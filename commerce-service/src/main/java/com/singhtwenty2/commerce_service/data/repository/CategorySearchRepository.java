/**
 * Copyright 2025 Aryan Singh
 * Developer: Aryan Singh (@singhtwenty2)
 * Portfolio: https://singhtwenty2.pages.dev/
 * This file is part of SSEW E-commerce Backend System
 * Licensed under MIT License
 * For commercial use and inquiries: aryansingh.corp@gmail.com
 * @author Aryan Singh (@singhtwenty2)
 * @project SSEW E-commerce Backend System
 * @since 2025
 */
package com.singhtwenty2.commerce_service.data.repository;

import com.singhtwenty2.commerce_service.data.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategorySearchRepository extends JpaRepository<Category, UUID> {

    @Query("""
            SELECT c FROM Category c
            WHERE c.isActive = true
            AND (
                LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            )
            ORDER BY
                CASE WHEN LOWER(c.name) = LOWER(:searchTerm) THEN 1
                     WHEN LOWER(c.name) LIKE LOWER(CONCAT(:searchTerm, '%')) THEN 2
                     ELSE 3
                END,
                c.displayOrder ASC,
                c.name ASC
            """)
    List<Category> searchCategories(@Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(DISTINCT m) FROM Manufacturer m JOIN m.categories c WHERE c.id = :categoryId AND m.isActive = true")
    Long countActiveManufacturersByCategoryId(@Param("categoryId") UUID categoryId);
}