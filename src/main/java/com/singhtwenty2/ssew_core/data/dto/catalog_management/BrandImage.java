package com.singhtwenty2.ssew_core.data.dto.catalog_management;

import lombok.*;

public class BrandImage {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BrandImageResult {
        private String objectKey;
        private String logoUrl;
        private long fileSize;
        private String contentType;
        private int width;
        private int height;
        private boolean isTaskExecuted;
        private String errorMessage;

        public static BrandImageResult success(String objectKey, String logoUrl, long fileSize,
                                               String contentType, int width, int height) {
            return new BrandImageResult(objectKey, logoUrl, fileSize, contentType, width, height, true, null);
        }

        public static BrandImageResult failure(String errorMessage) {
            return new BrandImageResult(null, null, 0, null, 0, 0, false, errorMessage);
        }
    }
}
