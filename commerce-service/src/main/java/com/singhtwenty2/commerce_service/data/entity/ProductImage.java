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
package com.singhtwenty2.commerce_service.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "product_images",
        indexes = {
                @Index(name = "idx_product_image_product", columnList = "product_id"),
                @Index(name = "idx_product_image_order", columnList = "display_order"),
                @Index(name = "idx_product_image_alt", columnList = "alt_text")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage extends BaseEntity {

    @Column(name = "object_key", nullable = false, length = 500)
    private String objectKey;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "width", nullable = false)
    private Integer width;

    @Column(name = "height", nullable = false)
    private Integer height;

    @Column(name = "alt_text", length = 200)
    private String altText;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 1;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public ProductImage(String objectKey, Long fileSize, String contentType,
                        Integer width, Integer height, String altText, Product product) {
        this.objectKey = objectKey;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.width = width;
        this.height = height;
        this.altText = altText;
        this.product = product;
    }
}