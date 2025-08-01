package com.singhtwenty2.ssew_core.data.dto.catalog_management;

import lombok.*;

public class ImageDTO {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProcessedImageResult {
        private byte[] imageData;
        private String contentType;
        private String fileExtension;
        private ImageMetadata metadata;
        private long fileSizeBytes;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImageMetadata {
        private int width;
        private int height;
        private String originalFormat;
        private long originalSize;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImageProcessingConfig {
        private int maxWidth;
        private int maxHeight;
        private float quality;
        private String outputFormat;
        private boolean maintainAspectRatio;
        private String configType;

        public static ImageProcessingConfig brandLogo() {
            return ImageProcessingConfig.builder()
                    .maxWidth(500)
                    .maxHeight(500)
                    .quality(0.95f) // High quality for brand logos
                    .outputFormat("webp")
                    .maintainAspectRatio(true)
                    .configType("brand_logo")
                    .build();
        }

        public static ImageProcessingConfig productCatalog() {
            return ImageProcessingConfig.builder()
                    .maxWidth(1200)
                    .maxHeight(1200)
                    .quality(0.92f) // High quality for product images
                    .outputFormat("webp")
                    .maintainAspectRatio(true)
                    .configType("product_catalog")
                    .build();
        }

        public static ImageProcessingConfig productThumbnail() {
            return ImageProcessingConfig.builder()
                    .maxWidth(400) // Increased from 300 for better quality
                    .maxHeight(400)
                    .quality(0.88f) // Good quality for thumbnails
                    .outputFormat("webp")
                    .maintainAspectRatio(true)
                    .configType("product_thumbnail")
                    .build();
        }

        // New config for high-quality brand logos
        public static ImageProcessingConfig brandLogoHighQuality() {
            return ImageProcessingConfig.builder()
                    .maxWidth(800)
                    .maxHeight(800)
                    .quality(0.98f) // Very high quality
                    .outputFormat("webp")
                    .maintainAspectRatio(true)
                    .configType("brand_logo_hq")
                    .build();
        }
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImageUploadResult {
        private String objectKey;
        private String s3Url;
        private String publicUrl;
        private long fileSize;
        private String contentType;
        private boolean isTaskExecuted;
        private String errorMessage;

        public static ImageUploadResult success(String objectKey, String s3Url, String publicUrl,
                                                long fileSize, String contentType) {
            return new ImageUploadResult(objectKey, s3Url, publicUrl, fileSize, contentType, true, null);
        }

        public static ImageUploadResult failure(String errorMessage) {
            return new ImageUploadResult(null, null, null, 0, null, false, errorMessage);
        }
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImageRequirements {
        private int maxWidth;
        private int maxHeight;
        private int minWidth;
        private int minHeight;
        private long maxFileSizeBytes;
        private String[] supportedFormats;
        private String recommendedFormat;
        private float qualityRange;
        private boolean maintainAspectRatio;
        private String description;

        public static ImageRequirements forBrandLogo() {
            return ImageRequirements.builder()
                    .maxWidth(800)
                    .maxHeight(800)
                    .minWidth(50)
                    .minHeight(50)
                    .maxFileSizeBytes(10 * 1024 * 1024)
                    .supportedFormats(new String[]{"jpg", "jpeg", "png", "webp", "gif", "bmp"})
                    .recommendedFormat("png")
                    .qualityRange(0.95f)
                    .maintainAspectRatio(true)
                    .description("Brand logo should be square or rectangular with good contrast")
                    .build();
        }

        public static ImageRequirements forProductImage() {
            return ImageRequirements.builder()
                    .maxWidth(1200)
                    .maxHeight(1200)
                    .minWidth(50)
                    .minHeight(50)
                    .maxFileSizeBytes(10 * 1024 * 1024)
                    .supportedFormats(new String[]{"jpg", "jpeg", "png", "webp", "gif", "bmp"})
                    .recommendedFormat("jpg")
                    .qualityRange(0.92f)
                    .maintainAspectRatio(true)
                    .description("Product images should be clear and well-lit with good resolution")
                    .build();
        }

        public static ImageRequirements forProductThumbnail() {
            return ImageRequirements.builder()
                    .maxWidth(400)
                    .maxHeight(400)
                    .minWidth(50)
                    .minHeight(50)
                    .maxFileSizeBytes(10 * 1024 * 1024)
                    .supportedFormats(new String[]{"jpg", "jpeg", "png", "webp", "gif", "bmp"})
                    .recommendedFormat("jpg")
                    .qualityRange(0.88f)
                    .maintainAspectRatio(true)
                    .description("Product thumbnails should be square format for consistent display")
                    .build();
        }
    }
}