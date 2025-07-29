package com.singhtwenty2.ssew_core.data.dto.catalog_management;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

public class BrandDTO {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateBrandRequest {

        @NotBlank(message = "Brand name is required")
        @Size(max = 100, message = "Brand name must not exceed 100 characters")
        private String name;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        @Size(max = 500, message = "Logo URL must not exceed 500 characters")
        private String logo_url;

        @Size(max = 300, message = "Website URL must not exceed 300 characters")
        private String website_url;

        private Integer display_order = 0;

        @NotNull(message = "Category ID is required")
        private String category_id;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateBrandRequest {

        @Size(max = 100, message = "Brand name must not exceed 100 characters")
        private String name;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        @Size(max = 500, message = "Logo URL must not exceed 500 characters")
        private String logo_url;

        @Size(max = 300, message = "Website URL must not exceed 300 characters")
        private String website_url;

        private Integer display_order;

        private Boolean is_active;

        private String category_id;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BrandResponse {
        private String brand_id;
        private String name;
        private String slug;
        private String description;
        private String logo_url;
        private String website_url;
        private Integer display_order;
        private Boolean is_active;
        private String created_at;
        private String updated_at;
        private String category_id;
        private String category_name;
        private Long product_count;
    }
}
