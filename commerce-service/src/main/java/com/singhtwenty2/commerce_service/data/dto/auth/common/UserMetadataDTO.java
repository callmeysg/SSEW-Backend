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
package com.singhtwenty2.commerce_service.data.dto.auth.common;

import lombok.*;

@Setter
@Getter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserMetadataDTO {
    private String userId;
    private String name;
    private String phone;
    private String email;
    private String role;
    private Boolean isEmailVerified;
    private Boolean isPhoneVerified;
    private Integer failedLoginAttempts;
    private String lastLoginTime;
    private String createdAt;
    private String updatedAt;
    private Long version;
}
