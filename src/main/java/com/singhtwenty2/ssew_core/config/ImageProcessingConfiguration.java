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
package com.singhtwenty2.ssew_core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.image-processing")
@Data
public class ImageProcessingConfiguration {

    private long maxFileSizeBytes = 10 * 1024 * 1024;
    private List<String> supportedFormats = List.of("jpg", "jpeg", "png", "webp", "gif", "bmp");
    private int minDimension = 50;
    private int maxDimension = 5000;

    private BrandLogoConfig brandLogo = new BrandLogoConfig();
    private ProductImageConfig productImage = new ProductImageConfig();
    private ThumbnailConfig thumbnail = new ThumbnailConfig();

    @Data
    public static class BrandLogoConfig {
        private int maxWidth = 800;
        private int maxHeight = 800;
        private int minWidth = 50;
        private int minHeight = 50;
        private float quality = 0.95f;
        private String outputFormat = "webp";
        private boolean maintainAspectRatio = true;
        private String description = "Brand logo should be square or rectangular with good contrast";
    }

    @Data
    public static class ProductImageConfig {
        private int maxWidth = 1200;
        private int maxHeight = 1200;
        private int minWidth = 50;
        private int minHeight = 50;
        private float quality = 0.92f;
        private String outputFormat = "webp";
        private boolean maintainAspectRatio = true;
        private String description = "Product images should be clear and well-lit with good resolution";
    }

    @Data
    public static class ThumbnailConfig {
        private int maxWidth = 400;
        private int maxHeight = 400;
        private int minWidth = 50;
        private int minHeight = 50;
        private float quality = 0.88f;
        private String outputFormat = "webp";
        private boolean maintainAspectRatio = true;
        private String description = "Product thumbnails should be square format for consistent display";
    }
}