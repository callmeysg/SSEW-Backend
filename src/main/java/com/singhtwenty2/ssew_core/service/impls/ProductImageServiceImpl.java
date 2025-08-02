package com.singhtwenty2.ssew_core.service.impls;

import com.singhtwenty2.ssew_core.data.entity.Product;
import com.singhtwenty2.ssew_core.data.entity.ProductImage;
import com.singhtwenty2.ssew_core.data.repository.ProductImageRepository;
import com.singhtwenty2.ssew_core.data.repository.ProductRepository;
import com.singhtwenty2.ssew_core.service.ImageProcessingService;
import com.singhtwenty2.ssew_core.service.ProductImageService;
import com.singhtwenty2.ssew_core.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.singhtwenty2.ssew_core.data.dto.catalog_management.ImageDTO.ImageUploadResult;
import static com.singhtwenty2.ssew_core.data.dto.catalog_management.ImageDTO.ProcessedImageResult;
import static com.singhtwenty2.ssew_core.data.dto.catalog_management.ProductImageDTO.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final ImageProcessingService imageProcessingService;
    private final S3Service s3Service;

    @Override
    public ProductImageResponse addProductImage(String productId, AddProductImageRequest request) {
        log.debug("Adding image to product: {}", productId);

        Product product = findProductById(productId);

        if (!validateProductImageFile(request.getImageFile())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image file");
        }

        ProcessedImageResult processedImage = imageProcessingService.processProductImage(
                request.getImageFile(), request.getIsThumbnail());

        ImageUploadResult uploadResult = s3Service.uploadProductImage(
                processedImage, productId, request.getIsThumbnail());

        if (request.getIsThumbnail() != null && request.getIsThumbnail()) {
            productImageRepository.clearThumbnailStatusForProduct(UUID.fromString(productId));
        }

        Integer displayOrder = request.getDisplayOrder();
        if (displayOrder == null) {
            displayOrder = getNextDisplayOrder(UUID.fromString(productId));
        } else {
            productImageRepository.incrementDisplayOrderFrom(UUID.fromString(productId), displayOrder);
        }

        ProductImage productImage = createProductImageFromRequest(
                request, product, uploadResult, processedImage, displayOrder);

        ProductImage savedImage = productImageRepository.save(productImage);

        log.info("Product image added successfully with ID: {}", savedImage.getId());

        return buildProductImageResponse(savedImage);
    }

    @Override
    public BulkImageUploadResponse addMultipleProductImages(String productId, AddMultipleProductImagesRequest request) {
        log.debug("Adding multiple images to product: {}", productId);

        Product product = findProductById(productId);

        List<MultipartFile> validFiles = request.getImageFiles().stream()
                .filter(this::validateProductImageFile)
                .toList();

        if (validFiles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No valid image files provided");
        }

        List<ProcessedImageResult> processedImages = validFiles.stream()
                .map(file -> imageProcessingService.processProductImage(file, false))
                .toList();

        List<ImageUploadResult> uploadResults = s3Service.uploadProductImages(processedImages, productId);

        List<ProductImageResponse> uploadedImages = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        boolean thumbnailSet = productImageRepository.existsByProductIdAndIsThumbnailTrue(UUID.fromString(productId));

        for (int i = 0; i < uploadResults.size(); i++) {
            try {
                ImageUploadResult uploadResult = uploadResults.get(i);
                ProcessedImageResult processedImage = processedImages.get(i);

                boolean shouldBeThumbnail = request.getGenerateThumbnail() && !thumbnailSet && i == 0;

                Integer displayOrder = getNextDisplayOrder(UUID.fromString(productId));

                String altText = null;
                if (request.getAltTexts() != null && i < request.getAltTexts().size()) {
                    altText = request.getAltTexts().get(i);
                }

                ProductImage productImage = new ProductImage();
                productImage.setProduct(product);
                productImage.setImageUrl(uploadResult.getObjectKey());
                productImage.setAltText(altText);
                productImage.setIsThumbnail(shouldBeThumbnail);
                productImage.setDisplayOrder(displayOrder);
                productImage.setFileSize(processedImage.getFileSizeBytes());
                productImage.setOriginalFileFormat(processedImage.getMetadata().getOriginalFormat());
                productImage.setWidth(processedImage.getMetadata().getWidth());
                productImage.setHeight(processedImage.getMetadata().getHeight());
                productImage.setCreatedAt(LocalDateTime.now());
                productImage.setUpdatedAt(LocalDateTime.now());

                ProductImage savedImage = productImageRepository.save(productImage);
                uploadedImages.add(buildProductImageResponse(savedImage));

                if (shouldBeThumbnail) {
                    thumbnailSet = true;
                }

                successCount++;
            } catch (Exception e) {
                log.error("Failed to process image at index {}: {}", i, e.getMessage());
                errors.add("Image " + (i + 1) + ": " + e.getMessage());
            }
        }

        log.info("Bulk upload completed for product: {}. Success: {}, Failures: {}",
                productId, successCount, errors.size());

        return BulkImageUploadResponse.builder()
                .productId(productId)
                .totalUploaded(validFiles.size())
                .successCount(successCount)
                .failureCount(errors.size())
                .uploadedImages(uploadedImages)
                .errors(errors)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductImagesResponse getProductImages(String productId) {
        log.debug("Fetching images for product: {}", productId);

        Product product = findProductById(productId);

        Sort sort = Sort.by(Sort.Direction.ASC, "displayOrder");
        List<ProductImage> images = productImageRepository.findByProductId(UUID.fromString(productId), sort);

        ProductImageResponse thumbnailImage = images.stream()
                .filter(ProductImage::getIsThumbnail)
                .findFirst()
                .map(this::buildProductImageResponse)
                .orElse(null);

        List<ProductImageResponse> catalogImages = images.stream()
                .filter(img -> !img.getIsThumbnail())
                .map(this::buildProductImageResponse)
                .toList();

        return ProductImagesResponse.builder()
                .productId(productId)
                .productName(product.getName())
                .totalImages((long) images.size())
                .thumbnailImage(thumbnailImage)
                .catalogImages(catalogImages)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductImageResponse getProductImageById(String productId, String imageId) {
        log.debug("Fetching product image: {} for product: {}", imageId, productId);

        ProductImage productImage = findProductImageById(productId, imageId);
        return buildProductImageResponse(productImage);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductImageResponse getThumbnailImage(String productId) {
        log.debug("Fetching thumbnail image for product: {}", productId);

        Optional<ProductImage> thumbnailOptional = productImageRepository
                .findByProductIdAndIsThumbnailTrue(UUID.fromString(productId));

        if (thumbnailOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No thumbnail image found for product: " + productId);
        }

        return buildProductImageResponse(thumbnailOptional.get());
    }

    @Override
    public ProductImageResponse updateProductImage(String productId, String imageId, UpdateProductImageRequest request) {
        log.debug("Updating product image: {} for product: {}", imageId, productId);

        ProductImage existingImage = findProductImageById(productId, imageId);

        boolean updated = false;

        if (request.getNewImageFile() != null && !request.getNewImageFile().isEmpty()) {
            if (!validateProductImageFile(request.getNewImageFile())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image file");
            }

            s3Service.deleteImage(existingImage.getImageUrl());

            ProcessedImageResult processedImage = imageProcessingService.processProductImage(
                    request.getNewImageFile(), existingImage.getIsThumbnail());

            ImageUploadResult uploadResult = s3Service.uploadProductImage(
                    processedImage, productId, existingImage.getIsThumbnail());

            existingImage.setImageUrl(uploadResult.getObjectKey());
            existingImage.setFileSize(processedImage.getFileSizeBytes());
            existingImage.setOriginalFileFormat(processedImage.getMetadata().getOriginalFormat());
            existingImage.setWidth(processedImage.getMetadata().getWidth());
            existingImage.setHeight(processedImage.getMetadata().getHeight());
            updated = true;
        }

        if (request.getAltText() != null) {
            existingImage.setAltText(StringUtils.hasText(request.getAltText()) ?
                    request.getAltText().trim() : null);
            updated = true;
        }

        if (request.getIsThumbnail() != null && !request.getIsThumbnail().equals(existingImage.getIsThumbnail())) {
            if (request.getIsThumbnail()) {
                productImageRepository.clearThumbnailStatusForProduct(UUID.fromString(productId));
            }
            existingImage.setIsThumbnail(request.getIsThumbnail());
            updated = true;
        }

        if (request.getDisplayOrder() != null && !request.getDisplayOrder().equals(existingImage.getDisplayOrder())) {
            updateImageDisplayOrder(existingImage, request.getDisplayOrder());
            updated = true;
        }

        if (updated) {
            existingImage.setUpdatedAt(LocalDateTime.now());
            ProductImage updatedImage = productImageRepository.save(existingImage);
            log.info("Product image updated successfully: {}", imageId);
            return buildProductImageResponse(updatedImage);
        }

        return buildProductImageResponse(existingImage);
    }

    @Override
    public ProductImageResponse setThumbnailImage(String productId, SetThumbnailRequest request) {
        log.debug("Setting thumbnail image: {} for product: {}", request.getImageId(), productId);

        ProductImage productImage = findProductImageById(productId, request.getImageId());

        productImageRepository.clearThumbnailStatusForProduct(UUID.fromString(productId));

        productImage.setIsThumbnail(true);
        productImage.setUpdatedAt(LocalDateTime.now());

        ProductImage updatedImage = productImageRepository.save(productImage);

        log.info("Thumbnail set successfully for product: {}, image: {}", productId, request.getImageId());

        return buildProductImageResponse(updatedImage);
    }

    @Override
    public ProductImagesResponse reorderProductImages(String productId, ReorderImagesRequest request) {
        log.debug("Reordering images for product: {}", productId);

        findProductById(productId);

        for (ReorderImagesRequest.ImageOrderItem orderItem : request.getImageOrders()) {
            ProductImage image = findProductImageById(productId, orderItem.getImageId());
            image.setDisplayOrder(orderItem.getDisplayOrder());
            image.setUpdatedAt(LocalDateTime.now());
            productImageRepository.save(image);
        }

        log.info("Images reordered successfully for product: {}", productId);

        return getProductImages(productId);
    }

    @Override
    public void deleteProductImage(String productId, String imageId) {
        log.debug("Deleting product image: {} for product: {}", imageId, productId);

        ProductImage productImage = findProductImageById(productId, imageId);

        s3Service.deleteImage(productImage.getImageUrl());

        productImageRepository.decrementDisplayOrderAfter(UUID.fromString(productId), productImage.getDisplayOrder());

        productImageRepository.delete(productImage);

        log.info("Product image deleted successfully: {}", imageId);
    }

    @Override
    public void deleteAllProductImages(String productId) {
        log.debug("Deleting all images for product: {}", productId);

        findProductById(productId);

        List<ProductImage> images = productImageRepository.findByProductId(UUID.fromString(productId), Sort.unsorted());

        List<String> objectKeys = images.stream()
                .map(ProductImage::getImageUrl)
                .toList();

        if (!objectKeys.isEmpty()) {
            s3Service.deleteImages(objectKeys);
        }

        productImageRepository.deleteByProductId(UUID.fromString(productId));

        log.info("All product images deleted successfully for product: {}", productId);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateImageAccessUrl(String productId, String imageId, Integer expirationMinutes) {
        log.debug("Generating access URL for image: {} in product: {}", imageId, productId);

        ProductImage productImage = findProductImageById(productId, imageId);

        return s3Service.generateReadPresignedUrl(productImage.getImageUrl(), expirationMinutes).getPresignedUrl();
    }

    @Override
    public boolean validateProductImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        ImageProcessingService.ValidationResult validationResult =
                imageProcessingService.validateImage(file, "product-image");

        return validationResult.isValid();
    }

    private Product findProductById(String productId) {
        try {
            UUID productUuid = UUID.fromString(productId);
            Optional<Product> productOptional = productRepository.findById(productUuid);

            if (productOptional.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + productId);
            }

            return productOptional.get();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid product ID format");
        }
    }

    private ProductImage findProductImageById(String productId, String imageId) {
        try {
            UUID productUuid = UUID.fromString(productId);
            UUID imageUuid = UUID.fromString(imageId);

            Optional<ProductImage> imageOptional = productImageRepository.findByIdAndProductId(imageUuid, productUuid);

            if (imageOptional.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Product image not found with ID: " + imageId + " for product: " + productId);
            }

            return imageOptional.get();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format");
        }
    }

    private Integer getNextDisplayOrder(UUID productId) {
        return productImageRepository.findMaxDisplayOrderByProductId(productId)
                .map(max -> max + 1)
                .orElse(0);
    }

    private void updateImageDisplayOrder(ProductImage image, Integer newDisplayOrder) {
        UUID productId = image.getProduct().getId();
        Integer currentOrder = image.getDisplayOrder();

        if (newDisplayOrder > currentOrder) {
            productImageRepository.decrementDisplayOrderAfter(productId, currentOrder);
            productImageRepository.incrementDisplayOrderFrom(productId, newDisplayOrder);
        } else if (newDisplayOrder < currentOrder) {
            productImageRepository.incrementDisplayOrderFrom(productId, newDisplayOrder);
            productImageRepository.decrementDisplayOrderAfter(productId, currentOrder);
        }

        image.setDisplayOrder(newDisplayOrder);
    }

    private ProductImage createProductImageFromRequest(AddProductImageRequest request, Product product,
                                                       ImageUploadResult uploadResult, ProcessedImageResult processedImage,
                                                       Integer displayOrder) {
        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setImageUrl(uploadResult.getObjectKey());
        productImage.setAltText(StringUtils.hasText(request.getAltText()) ? request.getAltText().trim() : null);
        productImage.setIsThumbnail(request.getIsThumbnail() != null ? request.getIsThumbnail() : false);
        productImage.setDisplayOrder(displayOrder);
        productImage.setFileSize(processedImage.getFileSizeBytes());
        productImage.setOriginalFileFormat(processedImage.getMetadata().getOriginalFormat());
        productImage.setWidth(processedImage.getMetadata().getWidth());
        productImage.setHeight(processedImage.getMetadata().getHeight());
        productImage.setCreatedAt(LocalDateTime.now());
        productImage.setUpdatedAt(LocalDateTime.now());
        return productImage;
    }

    private ProductImageResponse buildProductImageResponse(ProductImage productImage) {
        ImageInfo imageInfo = ImageInfo.builder()
                .objectKey(productImage.getImageUrl())
                .accessUrl(s3Service.generateReadPresignedUrl(productImage.getImageUrl(), 60).getPresignedUrl())
                .fileSize(productImage.getFileSize())
                .fileFormat(productImage.getOriginalFileFormat())
                .width(productImage.getWidth())
                .height(productImage.getHeight())
                .build();

        return ProductImageResponse.builder()
                .imageId(productImage.getId().toString())
                .productId(productImage.getProduct().getId().toString())
                .imageInfo(imageInfo)
                .altText(productImage.getAltText())
                .isThumbnail(productImage.getIsThumbnail())
                .displayOrder(productImage.getDisplayOrder())
                .createdAt(productImage.getCreatedAt().toString())
                .updatedAt(productImage.getUpdatedAt().toString())
                .build();
    }
}