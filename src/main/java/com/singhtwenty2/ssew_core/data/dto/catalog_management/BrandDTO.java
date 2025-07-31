package com.singhtwenty2.ssew_core.data.dto.catalog_management;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

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

        @Size(max = 300, message = "Website URL must not exceed 300 characters")
        private String websiteUrl;

        private Integer displayOrder = 0;

        @NotNull(message = "Category ID is required")
        private String categoryId;

        private MultipartFile logoFile;
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

        @Size(max = 300, message = "Website URL must not exceed 300 characters")
        private String websiteUrl;

        private Integer displayOrder;

        private Boolean isActive;

        private String categoryId;

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
    public static class BrandResponse {
        private String brandId;
        private String name;
        private String slug;
        private String description;
        private LogoInfo logoInfo;
        private String websiteUrl;
        private Integer displayOrder;
        private Boolean isActive;
        private String createdAt;
        private String updatedAt;
        private String categoryId;
        private String categoryName;
        private Long productCount;
    }
}
