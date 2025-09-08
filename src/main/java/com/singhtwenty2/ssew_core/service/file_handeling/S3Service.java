package com.singhtwenty2.ssew_core.service.file_handeling;

import org.springframework.stereotype.Service;

import java.util.List;

import static com.singhtwenty2.ssew_core.data.dto.catalogue.ImageDTO.ImageUploadResult;
import static com.singhtwenty2.ssew_core.data.dto.catalogue.ImageDTO.ProcessedImageResult;
import static com.singhtwenty2.ssew_core.data.dto.catalogue.PreSignedUrlDTO.PresignedUrlResponse;

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
