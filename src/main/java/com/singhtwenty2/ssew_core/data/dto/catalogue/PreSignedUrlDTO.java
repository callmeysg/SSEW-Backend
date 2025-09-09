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

import lombok.*;

import java.util.List;

public class PreSignedUrlDTO {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PresignedUrlRequest {
        private String fileName;
        private String contentType;
        private String documentType;
        private String entityType;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PresignedUrlResponse {
        private String presignedUrl;
        private String objectKey;
        private String s3Url;
        private String documentType;
        private long expiresIn;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BatchPresignedUrlRequest {
        private List<PresignedUrlRequest> files;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BatchPresignedUrlResponse {
        private List<PresignedUrlResponse> presignedUrls;
    }
}
