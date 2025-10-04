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
package com.singhtwenty2.commerce_service.service.aux;

import com.singhtwenty2.commerce_service.data.enums.UserRole;
import org.springframework.stereotype.Service;

@Service
public interface JwtService {
    public String generateToken(
            String userId,
            String role,
            String type,
            Long expiry
    );

    public String generateAccessToken(
            String userId,
            String role
    );

    public String generateRefreshToken(
            String userId,
            String role
    );

    public boolean validateAccessToken(
            String token
    );

    public String getUserIdFromAccessToken(
            String token
    );

    public UserRole getUserRoleFromAccessToken(String token);
}
