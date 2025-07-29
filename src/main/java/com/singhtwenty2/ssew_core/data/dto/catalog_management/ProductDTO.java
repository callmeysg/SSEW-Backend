package com.singhtwenty2.ssew_core.data.dto.catalog_management;

import com.singhtwenty2.ssew_core.data.dto.catalog_management.ProductImageDTO.ProductImageResponse;
import com.singhtwenty2.ssew_core.data.enums.ProductStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

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
        private String short_description;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
        private BigDecimal price;

        @DecimalMin(value = "0.0", message = "Compare price must be greater than or equal to 0")
        @Digits(integer = 8, fraction = 2, message = "Compare price format is invalid")
        private BigDecimal compare_price;

        @DecimalMin(value = "0.0", message = "Cost price must be greater than or equal to 0")
        @Digits(integer = 8, fraction = 2, message = "Cost price format is invalid")
        private BigDecimal cost_price;

        @DecimalMin(value = "0.0", message = "Weight must be greater than or equal to 0")
        private BigDecimal weight;

        @Size(max = 100, message = "Dimensions must not exceed 100 characters")
        private String dimensions;

        @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
        private Integer stock_quantity = 0;

        @Min(value = 0, message = "Min stock level must be greater than or equal to 0")
        private Integer min_stock_level = 0;

        private Boolean track_inventory = true;

        private ProductStatus status = ProductStatus.DRAFT;

        private Boolean is_featured = false;

        @Size(max = 150, message = "Meta title must not exceed 150 characters")
        private String meta_title;

        @Size(max = 300, message = "Meta description must not exceed 300 characters")
        private String meta_description;

        @Size(max = 500, message = "Tags must not exceed 500 characters")
        private String tags;

        @NotNull(message = "Brand ID is required")
        private String brand_id;
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

        private String description;

        @Size(max = 500, message = "Short description must not exceed 500 characters")
        private String short_description;

        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
        private BigDecimal price;

        @DecimalMin(value = "0.0", message = "Compare price must be greater than or equal to 0")
        @Digits(integer = 8, fraction = 2, message = "Compare price format is invalid")
        private BigDecimal compare_price;

        @DecimalMin(value = "0.0", message = "Cost price must be greater than or equal to 0")
        @Digits(integer = 8, fraction = 2, message = "Cost price format is invalid")
        private BigDecimal cost_price;

        @DecimalMin(value = "0.0", message = "Weight must be greater than or equal to 0")
        private BigDecimal weight;

        @Size(max = 100, message = "Dimensions must not exceed 100 characters")
        private String dimensions;

        @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
        private Integer stock_quantity;

        @Min(value = 0, message = "Min stock level must be greater than or equal to 0")
        private Integer min_stock_level;

        private Boolean track_inventory;

        private ProductStatus status;

        private Boolean is_featured;

        @Size(max = 150, message = "Meta title must not exceed 150 characters")
        private String meta_title;

        @Size(max = 300, message = "Meta description must not exceed 300 characters")
        private String meta_description;

        @Size(max = 500, message = "Tags must not exceed 500 characters")
        private String tags;

        private String brand_id;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductResponse {
        private String product_id;
        private String name;
        private String slug;
        private String sku;
        private String description;
        private String short_description;
        private BigDecimal price;
        private BigDecimal compare_price;
        private BigDecimal cost_price;
        private BigDecimal weight;
        private String dimensions;
        private Integer stock_quantity;
        private Integer min_stock_level;
        private Boolean track_inventory;
        private ProductStatus status;
        private Boolean is_featured;
        private String meta_title;
        private String meta_description;
        private String tags;
        private String created_at;
        private String updated_at;
        private String brand_id;
        private String brand_name;
        private String category_id;
        private String category_name;
        private List<ProductImageResponse> images;
        private ProductImageResponse thumbnail_image;
        private Boolean is_in_stock;
        private Boolean is_low_stock;
    }
}
