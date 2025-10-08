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

import com.singhtwenty2.commerce_service.data.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductSearchRepository extends JpaRepository<Product, UUID> {

    @Query("""
            SELECT p FROM Product p
            WHERE p.isActive = true
            AND p.variantType != 'VARIANT'
            AND (
                LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                OR LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                OR LOWER(p.searchTags) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                OR LOWER(p.manufacturer.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            )
            ORDER BY
                CASE WHEN LOWER(p.name) = LOWER(:searchTerm) THEN 1
                     WHEN LOWER(p.name) LIKE LOWER(CONCAT(:searchTerm, '%')) THEN 2
                     WHEN LOWER(p.sku) = LOWER(:searchTerm) THEN 3
                     ELSE 4
                END,
                p.isFeatured DESC,
                p.displayOrder ASC,
                p.name ASC
            """)
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.manufacturer WHERE p.id IN :ids")
    List<Product> findByIdsWithManufacturer(@Param("ids") List<UUID> ids);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.variants WHERE p.id IN :ids")
    List<Product> findByIdsWithVariants(@Param("ids") List<UUID> ids);
}