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

        public static ImageProcessingConfig brandLogo() {
            return ImageProcessingConfig.builder()
                    .maxWidth(500)
                    .maxHeight(500)
                    .quality(0.95f)
                    .outputFormat("webp")
                    .maintainAspectRatio(true)
                    .build();
        }

        public static ImageProcessingConfig productCatalog() {
            return ImageProcessingConfig.builder()
                    .maxWidth(1200)
                    .maxHeight(1200)
                    .quality(0.90f)
                    .outputFormat("webp")
                    .maintainAspectRatio(true)
                    .build();
        }

        public static ImageProcessingConfig productThumbnail() {
            return ImageProcessingConfig.builder()
                    .maxWidth(300)
                    .maxHeight(300)
                    .quality(0.85f)
                    .outputFormat("webp")
                    .maintainAspectRatio(true)
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
}
