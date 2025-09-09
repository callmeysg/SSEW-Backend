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
import com.singhtwenty2.ssew_core.data.entity.Manufacturer;
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
public interface ManufacturerRepository extends JpaRepository<Manufacturer, UUID> {

    Optional<Manufacturer> findBySlug(String slug);

    Optional<Manufacturer> findByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    boolean existsByNameAndIdNot(String name, UUID id);

    List<Manufacturer> findByIsActiveTrue();

    Page<Manufacturer> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT DISTINCT m FROM Manufacturer m JOIN m.categories c WHERE c IN :categories AND m.isActive = true")
    List<Manufacturer> findByCategoriesInAndIsActiveTrue(@Param("categories") List<Category> categories);

    @Query("SELECT DISTINCT m FROM Manufacturer m JOIN m.categories c WHERE c IN :categories")
    Page<Manufacturer> findByCategoriesIn(@Param("categories") List<Category> categories, Pageable pageable);

    @Query("SELECT DISTINCT m FROM Manufacturer m JOIN m.categories c WHERE c IN :categories AND m.isActive = true")
    Page<Manufacturer> findByCategoriesInAndIsActiveTrue(@Param("categories") List<Category> categories, Pageable pageable);

    @Query("SELECT DISTINCT m FROM Manufacturer m JOIN m.categories c WHERE " +
           "(:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:categoryId IS NULL OR c.id = :categoryId) AND " +
           "(:isActive IS NULL OR m.isActive = :isActive)")
    Page<Manufacturer> findManufacturersWithFilters(
            @Param("name") String name,
            @Param("categoryId") UUID categoryId,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    @Query("SELECT COUNT(p) FROM Product p WHERE p.manufacturer.id = :manufacturerId")
    Long countProductsByManufacturerId(@Param("manufacturerId") UUID manufacturerId);

    @Query("SELECT DISTINCT m FROM Manufacturer m JOIN m.categories c WHERE c IN :categories ORDER BY m.displayOrder ASC")
    List<Manufacturer> findByCategoriesInOrderByDisplayOrderAsc(@Param("categories") List<Category> categories);

    @Query("SELECT MAX(m.displayOrder) FROM Manufacturer m")
    Integer findMaxDisplayOrder();
}