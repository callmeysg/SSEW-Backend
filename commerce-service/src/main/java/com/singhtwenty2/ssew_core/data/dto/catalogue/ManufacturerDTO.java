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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ManufacturerDTO {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateManufacturerRequest {

        @NotBlank(message = "Manufacturer name is required")
        @Size(max = 100, message = "Manufacturer name must not exceed 100 characters")
        private String name;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        @Size(max = 300, message = "Website URL must not exceed 300 characters")
        private String websiteUrl;

        private Integer displayOrder = 0;

        @NotNull(message = "Category IDs are required")
        private List<String> categoryIds;

        private MultipartFile logoFile;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateManufacturerRequest {

        @Size(max = 100, message = "Manufacturer name must not exceed 100 characters")
        private String name;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        @Size(max = 300, message = "Website URL must not exceed 300 characters")
        private String websiteUrl;

        private Integer displayOrder;

        private Boolean isActive;

        private List<String> categoryIds;

        private MultipartFile logoFile;

        private Boolean removeLogo = false;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LogoInfo {
        private String objectKey;
        private String accessUrl;
        private long fileSize;
        private String contentType;
        private int width;
        private int height;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryInfo {
        private String categoryId;
        private String categoryName;
        private String categorySlug;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ManufacturerResponse {
        private String manufacturerId;
        private String name;
        private String slug;
        private String description;
        private LogoInfo logoInfo;
        private String websiteUrl;
        private Integer displayOrder;
        private Boolean isActive;
        private String createdAt;
        private String updatedAt;
        private List<CategoryInfo> categories;
        private Long productCount;
    }
}