package com.singhtwenty2.commerce_service.data.dto.catalogue;

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
