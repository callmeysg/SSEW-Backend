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
package com.singhtwenty2.commerce_service.config;

import com.singhtwenty2.commerce_service.service.file_handeling.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppInitializer implements ApplicationRunner {

    private final S3Service s3Service;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Initializing application...");
        try {
            s3Service.initializeS3Bucket();
            log.info("S3 storage initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize application: {}", e.getMessage(), e);
        }
    }
}
