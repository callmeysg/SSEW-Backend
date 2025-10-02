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
package com.singhtwenty2.ssew_core.service.file_handeling;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.singhtwenty2.ssew_core.data.dto.catalogue.ImageDTO.*;

public interface ImageProcessingService {

    ProcessedImageResult processImage(MultipartFile file, ImageProcessingConfig config);

    ProcessedImageResult processImage(MultipartFile file, ImageProcessingConfig config, String watermarkText);

    ProcessedImageResult processManufacturerLogo(MultipartFile file);

    ProcessedImageResult processProductImage(MultipartFile file, boolean isThumbnail);

    ProcessedImageResult processProductImage(MultipartFile file, boolean isThumbnail, String watermarkText);

    default List<ProcessedImageResult> processProductImages(List<MultipartFile> files, boolean generateThumbnails) {
        return files.stream()
                .map(file -> processProductImage(file, false))
                .toList();
    }

    default List<ProcessedImageResult> processProductImages(List<MultipartFile> files, boolean generateThumbnails, String watermarkText) {
        return files.stream()
                .map(file -> processProductImage(file, false, watermarkText))
                .toList();
    }

    boolean isValidImageFormat(MultipartFile file);

    ImageMetadata extractImageMetadata(MultipartFile file);

    default String[] getSupportedFormats() {
        return new String[]{"jpg", "jpeg", "png", "webp", "gif", "bmp"};
    }

    default long getMaxFileSize() {
        return 10 * 1024 * 1024;
    }

    default boolean validateDimensions(int width, int height, int minWidth, int minHeight, int maxWidth, int maxHeight) {
        return width >= minWidth && height >= minHeight && width <= maxWidth && height <= maxHeight;
    }

    default ImageRequirements getImageRequirements(String imageType) {
        return switch (imageType.toLowerCase()) {
            case "brand-logo", "brand_logo", "manufacturer-logo", "manufacturer_logo" ->
                    ImageRequirements.forBrandLogo();
            case "product-image", "product_image" -> ImageRequirements.forProductImage();
            case "product-thumbnail", "product_thumbnail" -> ImageRequirements.forProductThumbnail();
            default -> throw new IllegalArgumentException("Unsupported image type: " + imageType);
        };
    }

    default ValidationResult validateImage(MultipartFile file, String imageType) {
        if (file == null || file.isEmpty()) {
            return ValidationResult.failure("Image file is required");
        }

        if (file.getSize() > getMaxFileSize()) {
            return ValidationResult.failure(String.format("File size exceeds maximum limit of %d MB",
                    getMaxFileSize() / (1024 * 1024)));
        }

        if (!isValidImageFormat(file)) {
            return ValidationResult.failure("Unsupported image format. Supported formats: " +
                                            String.join(", ", getSupportedFormats()));
        }

        try {
            ImageMetadata metadata = extractImageMetadata(file);
            ImageRequirements requirements = getImageRequirements(imageType);

            if (!validateDimensions(metadata.getWidth(), metadata.getHeight(),
                    requirements.getMinWidth(), requirements.getMinHeight(),
                    requirements.getMaxWidth(), requirements.getMaxHeight())) {
                return ValidationResult.failure(String.format(
                        "Image dimensions (%dx%d) don't meet requirements. Expected: %dx%d to %dx%d",
                        metadata.getWidth(), metadata.getHeight(),
                        requirements.getMinWidth(), requirements.getMinHeight(),
                        requirements.getMaxWidth(), requirements.getMaxHeight()));
            }

            return ValidationResult.success();

        } catch (Exception e) {
            return ValidationResult.failure("Unable to process image: " + e.getMessage());
        }
    }

    @Setter
    @Getter
    @Data
    @Builder
    class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

    }
}