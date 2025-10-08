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

import org.springframework.stereotype.Service;

@Service
public interface TokenBlacklistService {

    void blacklistToken(String token, long expirationMs);

    boolean isBlacklisted(String token);

    void removeFromBlacklist(String token);
}