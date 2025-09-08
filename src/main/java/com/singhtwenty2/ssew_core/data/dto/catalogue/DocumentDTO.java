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
