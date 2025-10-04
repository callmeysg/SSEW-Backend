package com.singhtwenty2.commerce_service.data.dto.auth;

import lombok.*;

import java.util.List;

public class ProfileDTO {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProfileResponse {
        private String userId;
        private String name;
        private String phone;
        private String email;
        private String role;
        private Boolean isEmailVerified;
        private Boolean isPhoneVerified;
        private String lastLoginTime;
        private String createdAt;
        private String updatedAt;
        private List<ActiveSessionDTO> activeSessions;
        private Integer totalActiveSessions;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateProfileRequest {
        private String name;
        private String phone;
        private String email;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActiveSessionDTO {
        private String sessionId;
        private String deviceInfo;
        private String ipAddress;
        private String userAgent;
        private String createdAt;
        private String expiresAt;
        private String lastUsedAt;
    }
}