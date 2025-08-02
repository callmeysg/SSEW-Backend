package com.singhtwenty2.ssew_core.service;

import org.springframework.stereotype.Service;

import static com.singhtwenty2.ssew_core.data.dto.catalog_management.ProductImageDTO.*;

@Service
public interface ProductImageService {

    ProductImageResponse addProductImage(String productId, AddProductImageRequest request);

    BulkImageUploadResponse addMultipleProductImages(String productId, AddMultipleProductImagesRequest request);

    ProductImagesResponse getProductImages(String productId);

    ProductImageResponse getProductImageById(String productId, String imageId);

    ProductImageResponse getThumbnailImage(String productId);

    ProductImageResponse updateProductImage(String productId, String imageId, UpdateProductImageRequest request);

    ProductImageResponse setThumbnailImage(String productId, SetThumbnailRequest request);

    ProductImagesResponse reorderProductImages(String productId, ReorderImagesRequest request);

    void deleteProductImage(String productId, String imageId);

    void deleteAllProductImages(String productId);

    String generateImageAccessUrl(String productId, String imageId, Integer expirationMinutes);

    boolean validateProductImageFile(org.springframework.web.multipart.MultipartFile file);
}