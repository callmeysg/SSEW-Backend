package com.singhtwenty2.ssew_core.data.dto.catalog_management;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

public class ProductImageDTO {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateProductImageRequest {

        @NotBlank(message = "Image URL is required")
        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        private String imageUrl;

        @Size(max = 200, message = "Alt text must not exceed 200 characters")
        private String altText;

        private Boolean isThumbnail = false;

        private Integer displayOrder = 0;

        private Long fileSize;

        @Size(max = 10, message = "File format must not exceed 10 characters")
        private String fileFormat;

        @Min(value = 1, message = "Width must be greater than 0")
        private Integer width;

        @Min(value = 1, message = "Height must be greater than 0")
        private Integer height;

        @NotNull(message = "Product ID is required")
        private String productId;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateProductImageRequest {

        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        private String imageUrl;

        @Size(max = 200, message = "Alt text must not exceed 200 characters")
        private String altText;

        private Boolean isThumbnail;

        private Integer displayOrder;

        private Long fileSize;

        @Size(max = 10, message = "File format must not exceed 10 characters")
        private String fileFormat;

        @Min(value = 1, message = "Width must be greater than 0")
        private Integer width;

        @Min(value = 1, message = "Height must be greater than 0")
        private Integer height;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductImageResponse {
        private String imageId;
        private String imageUrl;
        private String altText;
        private Boolean isThumbnail;
        private Integer displayOrder;
        private Long fileSize;
        private String fileFormat;
        private Integer width;
        private Integer height;
        private String createdAt;
        private String updatedAt;
        private String productId;
    }
}
