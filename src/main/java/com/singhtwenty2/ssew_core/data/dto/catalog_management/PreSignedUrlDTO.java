package com.singhtwenty2.ssew_core.data.dto.catalog_management;

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
        private String file_name;
        private String content_type;
        private String document_type;
        private String entity_type;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PresignedUrlResponse {
        private String presigned_url;
        private String object_key;
        private String s3_url;
        private String document_type;
        private long expires_in;
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
        private List<PresignedUrlResponse> presigned_urls;
    }
}
