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
package com.singhtwenty2.ssew_core.service.catalogue;

import org.springframework.web.multipart.MultipartFile;

import static com.singhtwenty2.ssew_core.data.dto.catalogue.ManufacturerImage.ManufacturerImageResult;

public interface ManufacturerImageService {

    ManufacturerImageResult processManufacturerLogo(MultipartFile logoFile, String manufacturerSlug);

    void deleteManufacturerLogo(String manufacturerSlug, String objectKey);

    ManufacturerImageResult updateManufacturerLogo(MultipartFile logoFile, String manufacturerSlug, String existingObjectKey);

    String generateLogoAccessUrl(String objectKey, Integer expirationMinutes);

    boolean validateManufacturerLogoFile(MultipartFile file);
}