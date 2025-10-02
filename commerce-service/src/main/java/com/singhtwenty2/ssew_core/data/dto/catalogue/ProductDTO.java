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
package com.singhtwenty2.ssew_core.data.dto.catalogue;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ProductDTO {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateProductRequest {

        @NotBlank(message = "Product name is required")
        @Size(max = 200, message = "Product name must not exceed 200 characters")
        private String name;

        @Size(max = 100, message = "Model number must not exceed 100 characters")
        private String modelNumber;

        private String description;

        @Size(max = 500, message = "Short description must not exceed 500 characters")
        private String shortDescription;

        private Map<String, String> specifications;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Price must have maximum 8 integer and 2 decimal places")
        private BigDecimal price;

        @DecimalMin(value = "0.0", message = "Compare at price must be non-negative")
        @Digits(integer = 8, fraction = 2, message = "Compare at price must have maximum 8 integer and 2 decimal places")
        private BigDecimal compareAtPrice;

        @DecimalMin(value = "0.0", message = "Cost price must be non-negative")
        @Digits(integer = 8, fraction = 2, message = "Cost price must have maximum 8 integer and 2 decimal places")
        private BigDecimal costPrice;

        private Boolean isFeatured = false;

        private Integer displayOrder = 0;

        @Size(max = 150, message = "Meta title must not exceed 150 characters")
        private String metaTitle;

        @Size(max = 300, message = "Meta description must not exceed 300 characters")
        private String metaDescription;

        @Size(max = 500, message = "Meta keywords must not exceed 500 characters")
        private String metaKeywords;

        @Size(max = 1000, message = "Search tags must not exceed 1000 characters")
        private String searchTags;

        @NotBlank(message = "Manufacturer ID is required")
        private String manufacturerId;

        private String parentProductId;

        private List<String> compatibilityBrandIds;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateProductRequest {

        @Size(max = 200, message = "Product name must not exceed 200 characters")
        private String name;

        @Size(max = 100, message = "Model number must not exceed 100 characters")
        private String modelNumber;

        private String description;

        @Size(max = 500, message = "Short description must not exceed 500 characters")
        private String shortDescription;

        private Map<String, String> specifications;

        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Price must have maximum 8 integer and 2 decimal places")
        private BigDecimal price;

        @DecimalMin(value = "0.0", message = "Compare at price must be non-negative")
        @Digits(integer = 8, fraction = 2, message = "Compare at price must have maximum 8 integer and 2 decimal places")
        private BigDecimal compareAtPrice;

        @DecimalMin(value = "0.0", message = "Cost price must be non-negative")
        @Digits(integer = 8, fraction = 2, message = "Cost price must have maximum 8 integer and 2 decimal places")
        private BigDecimal costPrice;

        private Boolean isActive;

        private Boolean isFeatured;

        private Integer displayOrder;

        @Size(max = 150, message = "Meta title must not exceed 150 characters")
        private String metaTitle;

        @Size(max = 300, message = "Meta description must not exceed 300 characters")
        private String metaDescription;

        @Size(max = 500, message = "Meta keywords must not exceed 500 characters")
        private String metaKeywords;

        @Size(max = 1000, message = "Search tags must not exceed 1000 characters")
        private String searchTags;

        private String manufacturerId;

        private List<String> compatibilityBrandIds;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateVariantRequest {

        @NotBlank(message = "Variant name is required")
        @Size(max = 200, message = "Variant name must not exceed 200 characters")
        private String name;

        @Size(max = 100, message = "Model number must not exceed 100 characters")
        private String modelNumber;

        private String description;

        @Size(max = 500, message = "Short description must not exceed 500 characters")
        private String shortDescription;

        private Map<String, String> specifications;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Price must have maximum 8 integer and 2 decimal places")
        private BigDecimal price;

        @DecimalMin(value = "0.0", message = "Compare at price must be non-negative")
        @Digits(integer = 8, fraction = 2, message = "Compare at price must have maximum 8 integer and 2 decimal places")
        private BigDecimal compareAtPrice;

        @DecimalMin(value = "0.0", message = "Cost price must be non-negative")
        @Digits(integer = 8, fraction = 2, message = "Cost price must have maximum 8 integer and 2 decimal places")
        private BigDecimal costPrice;

        @Size(max = 150, message = "Meta title must not exceed 150 characters")
        private String metaTitle;

        @Size(max = 300, message = "Meta description must not exceed 300 characters")
        private String metaDescription;

        @Size(max = 500, message = "Meta keywords must not exceed 500 characters")
        private String metaKeywords;

        @Size(max = 1000, message = "Search tags must not exceed 1000 characters")
        private String searchTags;

        private List<String> compatibilityBrandIds;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductImageInfo {
        private String imageId;
        private String objectKey;
        private String accessUrl;
        private Long fileSize;
        private String contentType;
        private Integer width;
        private Integer height;
        private String altText;
        private Integer displayOrder;
        private Boolean isPrimary;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ThumbnailInfo {
        private String objectKey;
        private String accessUrl;
        private Long fileSize;
        private String contentType;
        private Integer width;
        private Integer height;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompatibilityBrandInfo {
        private String compatibilityBrandId;
        private String name;
        private String slug;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductVariantInfo {
        private String variantId;
        private String name;
        private String slug;
        private String sku;
        private String modelNumber;
        private BigDecimal price;
        private BigDecimal compareAtPrice;
        private Boolean isActive;
        private Integer variantPosition;
        private ThumbnailInfo thumbnailInfo;
        private List<ProductImageInfo> images;
        private Map<String, String> specifications;
        private List<CompatibilityBrandInfo> compatibilityBrands;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductResponse {
        private String productId;
        private String name;
        private String slug;
        private String sku;
        private String modelNumber;
        private String description;
        private String shortDescription;
        private Map<String, String> specifications;
        private BigDecimal price;
        private BigDecimal compareAtPrice;
        private BigDecimal costPrice;
        private Boolean isActive;
        private Boolean isFeatured;
        private Integer displayOrder;
        private String metaTitle;
        private String metaDescription;
        private String metaKeywords;
        private String searchTags;
        private String variantType;
        private Integer variantPosition;
        private String createdAt;
        private String updatedAt;
        private String manufacturerId;
        private String manufacturerName;
        private List<String> categoryIds;
        private List<String> categoryNames;
        private String parentProductId;
        private ThumbnailInfo thumbnailInfo;
        private List<ProductImageInfo> catalogImages;
        private List<ProductVariantInfo> variants;
        private Long totalVariants;
        private List<CompatibilityBrandInfo> compatibilityBrands;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductSummary {
        private String productId;
        private String name;
        private String slug;
        private String sku;
        private String modelNumber;
        private String shortDescription;
        private BigDecimal price;
        private BigDecimal compareAtPrice;
        private Boolean isActive;
        private Boolean isFeatured;
        private String variantType;
        private String manufacturerName;
        private List<String> categoryNames;
        private ThumbnailInfo thumbnailInfo;
        private Long totalVariants;
        private String createdAt;
        private List<CompatibilityBrandInfo> compatibilityBrands;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductSearchFilters {
        private String keyword;
        private String categoryId;
        private String manufacturerId;
        private List<String> compatibilityBrandIds;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private Boolean isFeatured;
        private Boolean inStock;
        private String sortBy = "created_at";
        private String sortDirection = "desc";
        private Integer page = 0;
        private Integer size = 20;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BulkOperationRequest {
        private List<String> productIds;
        private String operation;
        private Map<String, Object> parameters;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductValidationResult {
        private Boolean isValid;
        private String errorMessage;
        private Map<String, String> fieldErrors;

        public static ProductValidationResult success() {
            return ProductValidationResult.builder()
                    .isValid(true)
                    .build();
        }

        public static ProductValidationResult failure(String errorMessage) {
            return ProductValidationResult.builder()
                    .isValid(false)
                    .errorMessage(errorMessage)
                    .build();
        }

        public static ProductValidationResult failure(Map<String, String> fieldErrors) {
            return ProductValidationResult.builder()
                    .isValid(false)
                    .fieldErrors(fieldErrors)
                    .build();
        }
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductInventoryInfo {
        private String productId;
        private String sku;
        private String name;
        private String status;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductStatsResponse {
        private Long totalProducts;
        private Long activeProducts;
        private Long featuredProducts;
        private Long productsWithVariants;
        private BigDecimal averagePrice;
        private BigDecimal totalInventoryValue;
    }
}