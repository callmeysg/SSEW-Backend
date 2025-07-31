package com.singhtwenty2.ssew_core.service.impls;

import com.singhtwenty2.ssew_core.service.BrandImageService;
import com.singhtwenty2.ssew_core.service.ImageProcessingService;
import com.singhtwenty2.ssew_core.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static com.singhtwenty2.ssew_core.data.dto.catalog_management.BrandImage.BrandImageResult;
import static com.singhtwenty2.ssew_core.data.dto.catalog_management.ImageDTO.*;
import static com.singhtwenty2.ssew_core.data.dto.catalog_management.PreSignedUrlDTO.PresignedUrlResponse;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class BrandImageServiceImpl implements BrandImageService {

    private final ImageProcessingService imageProcessingService;
    private final S3Service s3Service;

    @Value("${app.image.brand-logo.expiration-minutes:60}")
    private Integer defaultExpirationMinutes;

    @Override
    public BrandImageResult processBrandLogo(MultipartFile logoFile, String brandSlug) {
        try {
            if (!validateBrandLogoFile(logoFile)) {
                return BrandImageResult.failure("Invalid brand logo file");
            }

            log.debug("Processing brand logo for brand slug: {}", brandSlug);

            ProcessedImageResult processedImage =
                    imageProcessingService.processBrandLogo(logoFile);

            ImageUploadResult uploadResult =
                    s3Service.uploadBrandLogo(processedImage, brandSlug);

            if (!uploadResult.isTaskExecuted()) {
                return BrandImageResult.failure(uploadResult.getErrorMessage());
            }

            log.info("Successfully processed and uploaded brand logo for brand slug: {}", brandSlug);

            return BrandImageResult.success(
                    uploadResult.getObjectKey(),
                    uploadResult.getPublicUrl(),
                    uploadResult.getFileSize(),
                    uploadResult.getContentType(),
                    processedImage.getMetadata().getWidth(),
                    processedImage.getMetadata().getHeight()
            );

        } catch (Exception e) {
            log.error("Failed to process brand logo for brand slug {}: {}", brandSlug, e.getMessage(), e);
            return BrandImageResult.failure("Failed to process brand logo: " + e.getMessage());
        }
    }

    @Override
    public void deleteBrandLogo(String brandSlug, String objectKey) {
        try {
            log.debug("Deleting brand logo for brand slug: {}, objectKey: {}", brandSlug, objectKey);

            boolean deleted = s3Service.deleteImage(objectKey);
            if (!deleted) {
                log.warn("Failed to delete brand logo from S3: {}", objectKey);
            } else {
                log.info("Successfully deleted brand logo for brand slug: {}", brandSlug);
            }

        } catch (Exception e) {
            log.error("Error deleting brand logo for brand slug {}: {}", brandSlug, e.getMessage(), e);
        }
    }

    @Override
    public BrandImageResult updateBrandLogo(MultipartFile logoFile, String brandSlug, String existingObjectKey) {
        try {
            log.debug("Updating brand logo for brand slug: {}", brandSlug);

            BrandImageResult newLogoResult = processBrandLogo(logoFile, brandSlug);

            if (newLogoResult.isTaskExecuted() && existingObjectKey != null) {
                deleteBrandLogo(brandSlug, existingObjectKey);
            }

            return newLogoResult;

        } catch (Exception e) {
            log.error("Failed to update brand logo for brand slug {}: {}", brandSlug, e.getMessage(), e);
            return BrandImageResult.failure("Failed to update brand logo: " + e.getMessage());
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
    public boolean validateBrandLogoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.debug("Brand logo file is null or empty");
            return false;
        }

        if (!imageProcessingService.isValidImageFormat(file)) {
            log.debug("Brand logo file has invalid format: {}", file.getContentType());
            return false;
        }

        try {
            ImageMetadata metadata = imageProcessingService.extractImageMetadata(file);

            if (metadata.getWidth() < 50 || metadata.getHeight() < 50) {
                log.debug("Brand logo dimensions too small: {}x{}", metadata.getWidth(), metadata.getHeight());
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("Error validating brand logo file: {}", e.getMessage(), e);
            return false;
        }
    }
}