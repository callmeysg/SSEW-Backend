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

import com.singhtwenty2.commerce_service.data.entity.Manufacturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ManufacturerSearchRepository extends JpaRepository<Manufacturer, UUID> {

    @Query("""
            SELECT m FROM Manufacturer m
            WHERE m.isActive = true
            AND (
                LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                OR LOWER(m.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            )
            ORDER BY
                CASE WHEN LOWER(m.name) = LOWER(:searchTerm) THEN 1
                     WHEN LOWER(m.name) LIKE LOWER(CONCAT(:searchTerm, '%')) THEN 2
                     ELSE 3
                END,
                m.displayOrder ASC,
                m.name ASC
            """)
    List<Manufacturer> searchManufacturers(@Param("searchTerm") String searchTerm);

    @Query("SELECT m FROM Manufacturer m LEFT JOIN FETCH m.categories WHERE m.id IN :ids")
    List<Manufacturer> findByIdsWithCategories(@Param("ids") List<UUID> ids);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.manufacturer.id = :manufacturerId AND p.isActive = true")
    Long countActiveProductsByManufacturerId(@Param("manufacturerId") UUID manufacturerId);
}