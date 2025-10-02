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

import com.singhtwenty2.commerce_service.data.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    /**
     * Find product image by ID and product ID
     */
    Optional<ProductImage> findByIdAndProductId(UUID imageId, UUID productId);

    /**
     * Find product image by object key and product ID
     */
    @Query("SELECT pi FROM ProductImage pi WHERE pi.objectKey = :objectKey AND pi.product.id = :productId")
    Optional<ProductImage> findByObjectKeyAndProductId(@Param("objectKey") String objectKey, @Param("productId") UUID productId);

    /**
     * Check if product image exists by object key
     */
    boolean existsByObjectKey(String objectKey);

    /**
     * Count images for a specific product
     */
    long countByProductId(UUID productId);

    /**
     * Find all images for a specific product ordered by display order
     */
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId ORDER BY pi.displayOrder ASC")
    java.util.List<ProductImage> findByProductIdOrderByDisplayOrderAsc(@Param("productId") UUID productId);

    /**
     * Find primary image for a product
     */
    Optional<ProductImage> findByProductIdAndIsPrimaryTrue(UUID productId);

    /**
     * Find all product images for a specific product ordered by display order
     */
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId ORDER BY pi.displayOrder ASC")
    List<ProductImage> findByProductIdOrderByDisplayOrder(@Param("productId") UUID productId);
}