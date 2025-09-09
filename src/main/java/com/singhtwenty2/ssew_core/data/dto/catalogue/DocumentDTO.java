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

public class DocumentDTO {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DocumentStatusInfo {
        private String originalObjectKey;
        private String newObjectKey;
        private String newS3Url;
        private String status;
        private String message;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DocumentDeleteRequest {
        private String entityType;
        private String entityId;
        private List<String> objectKeys;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DocumentDeleteResponse {
        private String entityType;
        private String entityId;
        private List<DocumentStatusInfo> deletedDocuments;
    }
}
