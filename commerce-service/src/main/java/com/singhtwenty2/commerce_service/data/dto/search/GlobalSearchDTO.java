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
package com.singhtwenty2.commerce_service.data.dto.search;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

public class GlobalSearchDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GlobalSearchResponse {
        private List<ProductSearchResult> products;
        private List<ManufacturerSearchResult> manufacturers;
        private List<CategorySearchResult> categories;
        private SearchMetadata metadata;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductSearchResult {
        private String productId;
        private String name;
        private String slug;
        private String sku;
        private String shortDescription;
        private BigDecimal price;
        private BigDecimal compareAtPrice;
        private String thumbnailUrl;
        private String manufacturerName;
        private List<String> categoryNames;
        private Boolean isActive;
        private Boolean isFeatured;
        private String variantType;
        private Integer variantCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ManufacturerSearchResult {
        private String manufacturerId;
        private String name;
        private String slug;
        private String description;
        private String logoUrl;
        private List<String> categoryNames;
        private Long productCount;
        private Boolean isActive;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategorySearchResult {
        private String categoryId;
        private String name;
        private String slug;
        private String description;
        private Long manufacturerCount;
        private Boolean isActive;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchMetadata {
        private Integer totalProducts;
        private Integer totalManufacturers;
        private Integer totalCategories;
        private Integer totalResults;
        private Long searchTimeMs;
        private String searchTerm;
        private Boolean fromCache;
    }
}