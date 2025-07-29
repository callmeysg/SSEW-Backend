package com.singhtwenty2.ssew_core.data.dto.catalog_management;

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
        private String original_object_key;
        private String new_object_key;
        private String new_s3_url;
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
        private String entity_type;
        private String entity_id;
        private List<String> object_keys;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DocumentDeleteResponse {
        private String entity_type;
        private String entity_id;
        private List<DocumentStatusInfo> deleted_documents;
    }
}
