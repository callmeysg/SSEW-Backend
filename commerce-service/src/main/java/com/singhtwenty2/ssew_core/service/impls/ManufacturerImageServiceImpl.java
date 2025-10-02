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
package com.singhtwenty2.ssew_core.service.impls;

import com.singhtwenty2.ssew_core.service.catalogue.ManufacturerImageService;
import com.singhtwenty2.ssew_core.service.file_handeling.ImageProcessingService;
import com.singhtwenty2.ssew_core.service.file_handeling.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static com.singhtwenty2.ssew_core.data.dto.catalogue.ImageDTO.*;
import static com.singhtwenty2.ssew_core.data.dto.catalogue.ManufacturerImage.ManufacturerImageResult;
import static com.singhtwenty2.ssew_core.data.dto.catalogue.PreSignedUrlDTO.PresignedUrlResponse;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ManufacturerImageServiceImpl implements ManufacturerImageService {

    private final ImageProcessingService imageProcessingService;
    private final S3Service s3Service;

    @Value("${app.image.manufacturer-logo.expiration-minutes:60}")
    private Integer defaultExpirationMinutes;

    @Override
    public ManufacturerImageResult processManufacturerLogo(MultipartFile logoFile, String manufacturerSlug) {
        try {
            if (!validateManufacturerLogoFile(logoFile)) {
                return ManufacturerImageResult.failure("Invalid manufacturer logo file");
            }

            log.debug("Processing manufacturer logo for manufacturer slug: {}", manufacturerSlug);

            ProcessedImageResult processedImage =
                    imageProcessingService.processManufacturerLogo(logoFile);

            ImageUploadResult uploadResult =
                    s3Service.uploadManufacturerLogo(processedImage, manufacturerSlug);

            if (!uploadResult.isTaskExecuted()) {
                return ManufacturerImageResult.failure(uploadResult.getErrorMessage());
            }

            log.info("Successfully processed and uploaded manufacturer logo for manufacturer slug: {}", manufacturerSlug);

            return ManufacturerImageResult.success(
                    uploadResult.getObjectKey(),
                    uploadResult.getPublicUrl(),
                    uploadResult.getFileSize(),
                    uploadResult.getContentType(),
                    processedImage.getMetadata().getWidth(),
                    processedImage.getMetadata().getHeight()
            );

        } catch (Exception e) {
            log.error("Failed to process manufacturer logo for manufacturer slug {}: {}", manufacturerSlug, e.getMessage(), e);
            return ManufacturerImageResult.failure("Failed to process manufacturer logo: " + e.getMessage());
        }
    }

    @Override
    public void deleteManufacturerLogo(String manufacturerSlug, String objectKey) {
        try {
            log.debug("Deleting manufacturer logo for manufacturer slug: {}, objectKey: {}", manufacturerSlug, objectKey);

            boolean deleted = s3Service.deleteImage(objectKey);
            if (!deleted) {
                log.warn("Failed to delete manufacturer logo from S3: {}", objectKey);
            } else {
                log.info("Successfully deleted manufacturer logo for manufacturer slug: {}", manufacturerSlug);
            }

        } catch (Exception e) {
            log.error("Error deleting manufacturer logo for manufacturer slug {}: {}", manufacturerSlug, e.getMessage(), e);
        }
    }

    @Override
    public ManufacturerImageResult updateManufacturerLogo(MultipartFile logoFile, String manufacturerSlug, String existingObjectKey) {
        try {
            log.debug("Updating manufacturer logo for manufacturer slug: {}", manufacturerSlug);

            ManufacturerImageResult newLogoResult = processManufacturerLogo(logoFile, manufacturerSlug);

            if (newLogoResult.isTaskExecuted() && existingObjectKey != null) {
                deleteManufacturerLogo(manufacturerSlug, existingObjectKey);
            }

            return newLogoResult;

        } catch (Exception e) {
            log.error("Failed to update manufacturer logo for manufacturer slug {}: {}", manufacturerSlug, e.getMessage(), e);
            return ManufacturerImageResult.failure("Failed to update manufacturer logo: " + e.getMessage());
        }
    }

    @Override
    public String generateLogoAccessUrl(String objectKey, Integer expirationMinutes) {
        try {
            Integer expiration = expirationMinutes != null ? expirationMinutes : defaultExpirationMinutes;

            PresignedUrlResponse urlResponse = s3Service.generateReadPresignedUrl(objectKey, expiration);
            return urlResponse.getPresignedUrl();

        } catch (Exception e) {
            log.error("Failed to generate logo access URL for objectKey {}: {}", objectKey, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean validateManufacturerLogoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.debug("Manufacturer logo file is null or empty");
            return false;
        }

        if (!imageProcessingService.isValidImageFormat(file)) {
            log.debug("Manufacturer logo file has invalid format: {}", file.getContentType());
            return false;
        }

        try {
            ImageMetadata metadata = imageProcessingService.extractImageMetadata(file);

            if (metadata.getWidth() < 50 || metadata.getHeight() < 50) {
                log.debug("Manufacturer logo dimensions too small: {}x{}", metadata.getWidth(), metadata.getHeight());
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("Error validating manufacturer logo file: {}", e.getMessage(), e);
            return false;
        }
    }
}