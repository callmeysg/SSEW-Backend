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
package com.singhtwenty2.commerce_service.service.file_handeling;

import org.springframework.stereotype.Service;

import java.util.List;

import static com.singhtwenty2.commerce_service.data.dto.catalogue.ImageDTO.ImageUploadResult;
import static com.singhtwenty2.commerce_service.data.dto.catalogue.ImageDTO.ProcessedImageResult;
import static com.singhtwenty2.commerce_service.data.dto.catalogue.PreSignedUrlDTO.PresignedUrlResponse;

@Service
public interface S3Service {

    void initializeS3Bucket();

    ImageUploadResult uploadManufacturerLogo(ProcessedImageResult processedImage, String brandSlug);

    ImageUploadResult uploadProductImage(ProcessedImageResult processedImage, String productId, boolean isThumbnail);

    List<ImageUploadResult> uploadProductImages(List<ProcessedImageResult> processedImages, String productId);

    PresignedUrlResponse generateReadPresignedUrl(String objectKey, Integer expirationMinutes);

    PresignedUrlResponse generateDownloadPresignedUrl(String objectKey, Integer expirationMinutes);

    boolean deleteImage(String objectKey);

    List<String> deleteImages(List<String> objectKeys);

    void moveFromTempToPermanent(String tempObjectKey);

    boolean imageExists(String objectKey);
}
