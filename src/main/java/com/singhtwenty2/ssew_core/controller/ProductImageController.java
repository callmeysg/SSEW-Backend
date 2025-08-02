package com.singhtwenty2.ssew_core.controller;

import com.singhtwenty2.ssew_core.service.ProductImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.singhtwenty2.ssew_core.data.dto.catalog_management.ProductImageDTO.*;

@RestController
@RequestMapping("/v1/product-media")
@RequiredArgsConstructor
@Slf4j
public class ProductImageController {

    private final ProductImageService productImageService;

    @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductImageResponse> addProductImage(
            @PathVariable String productId,
            @Valid @ModelAttribute AddProductImageRequest request) {

        log.info("Adding image to product: {}", productId);
        ProductImageResponse response = productImageService.addProductImage(productId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping(value = "/{productId}/images/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkImageUploadResponse> addMultipleProductImages(
            @PathVariable String productId,
            @Valid @ModelAttribute AddMultipleProductImagesRequest request) {

        log.info("Adding multiple images to product: {}", productId);
        BulkImageUploadResponse response = productImageService.addMultipleProductImages(productId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{productId}/images")
    public ResponseEntity<ProductImagesResponse> getProductImages(@PathVariable String productId) {
        log.info("Fetching images for product: {}", productId);
        ProductImagesResponse response = productImageService.getProductImages(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}/images/{imageId}")
    public ResponseEntity<ProductImageResponse> getProductImageById(
            @PathVariable String productId,
            @PathVariable String imageId) {

        log.info("Fetching image: {} for product: {}", imageId, productId);
        ProductImageResponse response = productImageService.getProductImageById(productId, imageId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}/images/thumbnail")
    public ResponseEntity<ProductImageResponse> getThumbnailImage(@PathVariable String productId) {
        log.info("Fetching thumbnail image for product: {}", productId);
        ProductImageResponse response = productImageService.getThumbnailImage(productId);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{productId}/images/{imageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductImageResponse> updateProductImage(
            @PathVariable String productId,
            @PathVariable String imageId,
            @Valid @ModelAttribute UpdateProductImageRequest request) {

        log.info("Updating image: {} for product: {}", imageId, productId);
        ProductImageResponse response = productImageService.updateProductImage(productId, imageId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}/images/thumbnail")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductImageResponse> setThumbnailImage(
            @PathVariable String productId,
            @Valid @RequestBody SetThumbnailRequest request) {

        log.info("Setting thumbnail image for product: {}", productId);
        ProductImageResponse response = productImageService.setThumbnailImage(productId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}/images/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductImagesResponse> reorderProductImages(
            @PathVariable String productId,
            @Valid @RequestBody ReorderImagesRequest request) {

        log.info("Reordering images for product: {}", productId);
        ProductImagesResponse response = productImageService.reorderProductImages(productId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProductImage(
            @PathVariable String productId,
            @PathVariable String imageId) {

        log.info("Deleting image: {} for product: {}", imageId, productId);
        productImageService.deleteProductImage(productId, imageId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{productId}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAllProductImages(@PathVariable String productId) {
        log.info("Deleting all images for product: {}", productId);
        productImageService.deleteAllProductImages(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}/images/{imageId}/access-url")
    public ResponseEntity<String> generateImageAccessUrl(
            @PathVariable String productId,
            @PathVariable String imageId,
            @RequestParam(defaultValue = "60") Integer expirationMinutes) {

        log.info("Generating access URL for image: {} in product: {}", imageId, productId);
        String accessUrl = productImageService.generateImageAccessUrl(productId, imageId, expirationMinutes);
        return ResponseEntity.ok(accessUrl);
    }
}