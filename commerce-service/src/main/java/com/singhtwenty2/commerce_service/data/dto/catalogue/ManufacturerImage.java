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

import lombok.*;

public class ManufacturerImage {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ManufacturerImageResult {
        private String objectKey;
        private String logoUrl;
        private long fileSize;
        private String contentType;
        private int width;
        private int height;
        private boolean isTaskExecuted;
        private String errorMessage;

        public static ManufacturerImageResult success(String objectKey, String logoUrl, long fileSize,
                                                      String contentType, int width, int height) {
            return new ManufacturerImageResult(objectKey, logoUrl, fileSize, contentType, width, height, true, null);
        }

        public static ManufacturerImageResult failure(String errorMessage) {
            return new ManufacturerImageResult(null, null, 0, null, 0, 0, false, errorMessage);
        }
    }
}