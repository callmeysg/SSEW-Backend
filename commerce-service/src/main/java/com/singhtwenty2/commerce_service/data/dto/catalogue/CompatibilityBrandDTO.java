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
package com.singhtwenty2.commerce_service.data.dto.catalogue;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public class CompatibilityBrandDTO {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateCompatibilityBrandRequest {

        @NotBlank(message = "Compatibility brand name is required")
        @Size(max = 100, message = "Compatibility brand name must not exceed 100 characters")
        private String name;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateCompatibilityBrandRequest {

        @Size(max = 100, message = "Compatibility brand name must not exceed 100 characters")
        private String name;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompatibilityBrandResponse {
        private String compatibilityBrandId;
        private String name;
        private String slug;
        private String createdAt;
        private String updatedAt;
        private Long productCount;
    }
}