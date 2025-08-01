package com.singhtwenty2.ssew_core.data.dto.catalog_management;

import com.singhtwenty2.ssew_core.data.enums.ProductStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

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

        @NotBlank(message = "SKU is required")
        @Size(max = 50, message = "SKU must not exceed 50 characters")
        private String sku;

        private String description;

        @Size(max = 500, message = "Short description must not exceed 500 characters")
        private String shortDescription;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal digits")
        private BigDecimal price;

        @DecimalMin(value = "0.0", message = "Compare price must be greater than or equal to 0")
        @Digits(integer = 8, fraction = 2, message = "Compare price must have at most 8 integer digits and 2 decimal digits")
        private BigDecimal comparePrice;

        @DecimalMin(value = "0.0", message = "Cost price must be greater than or equal to 0")
        @Digits(integer = 8, fraction = 2, message = "Cost price must have at most 8 integer digits and 2 decimal digits")
        private BigDecimal costPrice;

        @DecimalMin(value = "0.0", message = "Weight must be greater than or equal to 0")
        @Digits(integer = 5, fraction = 3, message = "Weight must have at most 5 integer digits and 3 decimal digits")
        private BigDecimal weight;

        @Size(max = 100, message = "Dimensions must not exceed 100 characters")
        private String dimensions;

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
        private Integer stockQuantity;

        @Min(value = 0, message = "Minimum stock level must be greater than or equal to 0")
        private Integer minStockLevel = 0;

        private Boolean trackInventory = true;

        private ProductStatus status = ProductStatus.DRAFT;

        private Boolean isFeatured = false;

        @Size(max = 150, message = "Meta title must not exceed 150 characters")
        private String metaTitle;

        @Size(max = 300, message = "Meta description must not exceed 300 characters")
        private String metaDescription;

        @Size(max = 500, message = "Tags must not exceed 500 characters")
        private String tags;

        @NotNull(message = "Brand ID is required")
        private String brandId;
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

        @Size(max = 50, message = "SKU must not exceed 50 characters")
        private String sku;

        private String description;

        @Size(max = 500, message = "Short description must not exceed 500 characters")
        private String shortDescription;

        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal digits")
        private BigDecimal price;

        @DecimalMin(value = "0.0", message = "Compare price must be greater than or equal to 0")
        @Digits(integer = 8, fraction = 2, message = "Compare price must have at most 8 integer digits and 2 decimal digits")
        private BigDecimal comparePrice;

        @DecimalMin(value = "0.0", message = "Cost price must be greater than or equal to 0")
        @Digits(integer = 8, fraction = 2, message = "Cost price must have at most 8 integer digits and 2 decimal digits")
        private BigDecimal costPrice;

        @DecimalMin(value = "0.0", message = "Weight must be greater than or equal to 0")
        @Digits(integer = 5, fraction = 3, message = "Weight must have at most 5 integer digits and 3 decimal digits")
        private BigDecimal weight;

        @Size(max = 100, message = "Dimensions must not exceed 100 characters")
        private String dimensions;

        @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
        private Integer stockQuantity;

        @Min(value = 0, message = "Minimum stock level must be greater than or equal to 0")
        private Integer minStockLevel;

        private Boolean trackInventory;

        private ProductStatus status;

        private Boolean isFeatured;

        @Size(max = 150, message = "Meta title must not exceed 150 characters")
        private String metaTitle;

        @Size(max = 300, message = "Meta description must not exceed 300 characters")
        private String metaDescription;

        @Size(max = 500, message = "Tags must not exceed 500 characters")
        private String tags;

        private String brandId;
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
        private String description;
        private String shortDescription;
        private BigDecimal price;
        private BigDecimal comparePrice;
        private BigDecimal costPrice;
        private BigDecimal weight;
        private String dimensions;
        private Integer stockQuantity;
        private Integer minStockLevel;
        private Boolean trackInventory;
        private ProductStatus status;
        private Boolean isFeatured;
        private String metaTitle;
        private String metaDescription;
        private String tags;
        private String createdAt;
        private String updatedAt;
        private String brandId;
        private String brandName;
        private String categoryId;
        private String categoryName;
        private Long imageCount;
        private Boolean isInStock;
        private Boolean isLowStock;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductInventoryUpdateRequest {
        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
        private Integer stockQuantity;

        @Min(value = 0, message = "Minimum stock level must be greater than or equal to 0")
        private Integer minStockLevel;

        private Boolean trackInventory;
    }
}