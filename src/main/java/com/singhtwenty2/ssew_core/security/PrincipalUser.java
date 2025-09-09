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
package com.singhtwenty2.ssew_core.security;

import com.singhtwenty2.ssew_core.data.enums.UserRole;
import lombok.*;

import java.util.UUID;

@Getter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrincipalUser {
    private UUID userId;
    private UserRole role;
}

