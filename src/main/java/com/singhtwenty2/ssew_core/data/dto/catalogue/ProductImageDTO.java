package com.singhtwenty2.ssew_core.data.dto.catalogue;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ProductImageDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageInfo {
        private String imageId;
        private String objectKey;
        private String accessUrl;
        private String altText;
        private Integer displayOrder;
        private Long fileSize;
        private String fileFormat;
        private Integer width;
        private Integer height;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddProductImageRequest {
        @NotNull(message = "Image file is required")
        private MultipartFile imageFile;

        @Size(max = 200, message = "Alt text cannot exceed 200 characters")
        private String altText;

        private Boolean isThumbnail = false;

        @Min(value = 0, message = "Display order cannot be negative")
        private Integer displayOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddMultipleProductImagesRequest {
        @NotEmpty(message = "At least one image file is required")
        @Size(max = 10, message = "Cannot upload more than 10 images at once")
        private List<MultipartFile> imageFiles;

        @Size(max = 10, message = "Cannot have more than 10 alt texts")
        private List<String> altTexts;

        private Boolean generateThumbnail = false;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProductImageRequest {
        private MultipartFile newImageFile;

        @Size(max = 200, message = "Alt text cannot exceed 200 characters")
        private String altText;

        private Boolean isThumbnail;

        @Min(value = 0, message = "Display order cannot be negative")
        private Integer displayOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SetThumbnailRequest {
        @NotBlank(message = "Image ID is required")
        private String imageId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReorderImagesRequest {
        @NotEmpty(message = "Image orders list cannot be empty")
        private List<ImageOrderItem> imageOrders;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ImageOrderItem {
            @NotBlank(message = "Image ID is required")
            private String imageId;

            @NotNull(message = "Display order is required")
            @Min(value = 0, message = "Display order cannot be negative")
            private Integer displayOrder;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductImageResponse {
        private String imageId;
        private String productId;
        private ImageInfo imageInfo;
        private String altText;
        private Boolean isThumbnail;
        private Integer displayOrder;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductImagesResponse {
        private String productId;
        private String productName;
        private Long totalImages;
        private ProductImageResponse thumbnailImage;
        private List<ProductImageResponse> catalogImages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkImageUploadResponse {
        private String productId;
        private Integer totalUploaded;
        private Integer successCount;
        private Integer failureCount;
        private List<ProductImageResponse> uploadedImages;
        private List<String> errors;
    }
}