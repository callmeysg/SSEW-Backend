package com.singhtwenty2.ssew_core.data.dto.catalog_management;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public class CategoryDTO {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateCategoryRequest {

        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name must not exceed 100 characters")
        private String name;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        private Integer displayOrder = 0;

        @Size(max = 150, message = "Meta title must not exceed 150 characters")
        private String metaTitle;

        @Size(max = 300, message = "Meta description must not exceed 300 characters")
        private String metaDescription;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateCategoryRequest {

        @Size(max = 100, message = "Category name must not exceed 100 characters")
        private String name;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        private Integer displayOrder;

        private Boolean isActive;

        @Size(max = 150, message = "Meta title must not exceed 150 characters")
        private String metaTitle;

        @Size(max = 300, message = "Meta description must not exceed 300 characters")
        private String metaDescription;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryResponse {
        private String categoryId;
        private String name;
        private String slug;
        private String description;
        private Integer display_order;
        private Boolean isActive;
        private String metaTitle;
        private String metaDescription;
        private String createdAt;
        private String updatedAt;
        private Long brandCount;
    }
}
