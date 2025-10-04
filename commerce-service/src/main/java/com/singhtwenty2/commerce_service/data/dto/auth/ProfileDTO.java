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