package com.singhtwenty2.ssew_core.service;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.singhtwenty2.ssew_core.data.dto.catalog_management.ImageDTO.*;

public interface ImageProcessingService {

    /**
     * Process an image with the given configuration
     */
    ProcessedImageResult processImage(MultipartFile file, ImageProcessingConfig config);

    /**
     * Process a brand logo with optimized settings
     */
    ProcessedImageResult processBrandLogo(MultipartFile file);

    /**
     * Process a product image (catalog or thumbnail)
     */
    ProcessedImageResult processProductImage(MultipartFile file, boolean isThumbnail);

    /**
     * Process multiple images for a product
     */
    default List<ProcessedImageResult> processProductImages(List<MultipartFile> files, boolean generateThumbnails) {
        return files.stream()
                .map(file -> processProductImage(file, false))
                .toList();
    }

    /**
     * Validate if the file is a supported image format
     */
    boolean isValidImageFormat(MultipartFile file);

    /**
     * Extract metadata from an image file
     */
    ImageMetadata extractImageMetadata(MultipartFile file);

    /**
     * Get supported image formats
     */
    default String[] getSupportedFormats() {
        return new String[]{"jpg", "jpeg", "png", "webp", "gif", "bmp"};
    }

    /**
     * Get the maximum allowed file size in bytes
     */
    default long getMaxFileSize() {
        return 10 * 1024 * 1024;
    }

    /**
     * Validate image dimensions
     */
    default boolean validateDimensions(int width, int height, int minWidth, int minHeight, int maxWidth, int maxHeight) {
        return width >= minWidth && height >= minHeight && width <= maxWidth && height <= maxHeight;
    }

    /**
     * Get image requirements for different types
     */
    default ImageRequirements getImageRequirements(String imageType) {
        return switch (imageType.toLowerCase()) {
            case "brand-logo", "brand_logo" -> ImageRequirements.forBrandLogo();
            case "product-image", "product_image" -> ImageRequirements.forProductImage();
            case "product-thumbnail", "product_thumbnail" -> ImageRequirements.forProductThumbnail();
            default -> throw new IllegalArgumentException("Unsupported image type: " + imageType);
        };
    }

    /**
     * Comprehensive validation for any image type
     */
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

    /**
     * Validation result helper class
     */
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