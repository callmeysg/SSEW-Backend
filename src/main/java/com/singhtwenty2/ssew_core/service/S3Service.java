package com.singhtwenty2.ssew_core.service;

import org.springframework.stereotype.Service;

import java.util.List;

import static com.singhtwenty2.ssew_core.data.dto.catalog_management.ImageDTO.ImageUploadResult;
import static com.singhtwenty2.ssew_core.data.dto.catalog_management.ImageDTO.ProcessedImageResult;
import static com.singhtwenty2.ssew_core.data.dto.catalog_management.PreSignedUrlDTO.PresignedUrlResponse;

@Service
public interface S3Service {

    void initializeS3Bucket();

    ImageUploadResult uploadBrandLogo(ProcessedImageResult processedImage, String brandSlug);

    ImageUploadResult uploadProductImage(ProcessedImageResult processedImage, String productId, boolean isThumbnail);

    List<ImageUploadResult> uploadProductImages(List<ProcessedImageResult> processedImages, String productId);

    PresignedUrlResponse generateReadPresignedUrl(String objectKey, Integer expirationMinutes);

    PresignedUrlResponse generateDownloadPresignedUrl(String objectKey, Integer expirationMinutes);

    boolean deleteImage(String objectKey);

    List<String> deleteImages(List<String> objectKeys);

    void moveFromTempToPermanent(String tempObjectKey);

    boolean imageExists(String objectKey);
}
