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
package com.singhtwenty2.commerce_service.service.catalogue.helper;

import com.singhtwenty2.commerce_service.data.entity.Product;
import com.singhtwenty2.commerce_service.data.entity.ProductImage;
import com.singhtwenty2.commerce_service.data.repository.ProductImageRepository;
import com.singhtwenty2.commerce_service.data.repository.ProductRepository;
import com.singhtwenty2.commerce_service.service.file_handeling.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final S3Service s3Service;

    public boolean isImageOwnedByProduct(String objectKey, Product product) {
        String productIdFromKey = extractProductIdFromObjectKey(objectKey);
        return productIdFromKey != null && productIdFromKey.equals(product.getId().toString());
    }

    public String extractProductIdFromObjectKey(String objectKey) {
        if (objectKey == null || !objectKey.startsWith("products/")) {
            return null;
        }
        String[] parts = objectKey.split("/");
        if (parts.length >= 2) {
            return parts[1];
        }
        return null;
    }

    public void deleteProductOwnedImages(Product product) {
        List<String> objectKeys = new ArrayList<>();

        if (product.getThumbnailObjectKey() != null && isImageOwnedByProduct(product.getThumbnailObjectKey(), product)) {
            objectKeys.add(product.getThumbnailObjectKey());
        }

        objectKeys.addAll(product.getProductImages().stream()
                .map(ProductImage::getObjectKey)
                .filter(key -> isImageOwnedByProduct(key, product))
                .toList());

        if (!objectKeys.isEmpty()) {
            s3Service.deleteImages(objectKeys);
        }
    }

    public void deleteVariantSpecificImages(Product variant) {
        List<String> objectKeysToDelete = new ArrayList<>();

        if (variant.getThumbnailObjectKey() != null && isImageOwnedByProduct(variant.getThumbnailObjectKey(), variant)) {
            objectKeysToDelete.add(variant.getThumbnailObjectKey());
        }

        objectKeysToDelete.addAll(variant.getProductImages().stream()
                .map(ProductImage::getObjectKey)
                .filter(key -> isImageOwnedByProduct(key, variant))
                .toList());

        if (!objectKeysToDelete.isEmpty()) {
            s3Service.deleteImages(objectKeysToDelete);
        }
    }

    public void inheritParentImages(Product parentProduct, Product variant) {
        log.info("Inheriting images from parent {} to variant {}",
                parentProduct.getId(), variant.getId());

        if (parentProduct.getThumbnailObjectKey() != null &&
            s3Service.imageExists(parentProduct.getThumbnailObjectKey())) {

            variant.setThumbnailObjectKey(parentProduct.getThumbnailObjectKey());
            variant.setThumbnailFileSize(parentProduct.getThumbnailFileSize());
            variant.setThumbnailContentType(parentProduct.getThumbnailContentType());
            variant.setThumbnailWidth(parentProduct.getThumbnailWidth());
            variant.setThumbnailHeight(parentProduct.getThumbnailHeight());
            log.info("Inherited thumbnail: {}", parentProduct.getThumbnailObjectKey());
        }

        List<ProductImage> parentImages = productImageRepository
                .findByProductIdOrderByDisplayOrderAsc(parentProduct.getId());

        if (!parentImages.isEmpty()) {
            List<ProductImage> variantImages = parentImages.stream()
                    .filter(img -> s3Service.imageExists(img.getObjectKey()))
                    .map(parentImage -> {
                        ProductImage variantImage = new ProductImage();
                        variantImage.setObjectKey(parentImage.getObjectKey());
                        variantImage.setFileSize(parentImage.getFileSize());
                        variantImage.setContentType(parentImage.getContentType());
                        variantImage.setWidth(parentImage.getWidth());
                        variantImage.setHeight(parentImage.getHeight());
                        variantImage.setAltText(parentImage.getAltText());
                        variantImage.setDisplayOrder(parentImage.getDisplayOrder());
                        variantImage.setIsPrimary(parentImage.getIsPrimary());
                        variantImage.setProduct(variant);
                        return variantImage;
                    })
                    .collect(Collectors.toList());

            if (!variantImages.isEmpty()) {
                productImageRepository.saveAll(variantImages);
                log.info("Inherited {} catalog images for variant {}",
                        variantImages.size(), variant.getId());
            }
        }
    }

    public void clearThumbnailFromVariants(Product parentProduct, String objectKey) {
        List<Product> variants = parentProduct.getVariants();
        for (Product variant : variants) {
            if (objectKey.equals(variant.getThumbnailObjectKey())) {
                variant.setThumbnailObjectKey(null);
                variant.setThumbnailFileSize(null);
                variant.setThumbnailContentType(null);
                variant.setThumbnailWidth(null);
                variant.setThumbnailHeight(null);
            }
        }
        productRepository.saveAll(variants);
    }

    public void removeImageFromVariants(Product parentProduct, String objectKey) {
        List<Product> variants = parentProduct.getVariants();
        for (Product variant : variants) {
            Optional<ProductImage> variantImage = productImageRepository
                    .findByObjectKeyAndProductId(objectKey, variant.getId());
            variantImage.ifPresent(productImageRepository::delete);
        }
    }
}