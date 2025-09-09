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
package com.singhtwenty2.ssew_core.service.catalogue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.singhtwenty2.ssew_core.data.dto.catalogue.PreSignedUrlDTO.PresignedUrlResponse;
import static com.singhtwenty2.ssew_core.data.dto.catalogue.ProductDTO.*;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse createVariant(String parentProductId, CreateVariantRequest request);

    ProductResponse getProductById(String productId);

    ProductResponse getProductBySlug(String slug);

    ProductResponse getProductBySku(String sku);

    ProductResponse updateProduct(String productId, UpdateProductRequest request);

    void deleteProduct(String productId);

    void deleteVariant(String variantId);

    Page<ProductSummary> getAllProducts(ProductSearchFilters filters, Pageable pageable);

    Page<ProductSummary> getAllProductsIncludingVariants(ProductSearchFilters filters, Pageable pageable);

    List<ProductVariantInfo> getProductVariants(String productId);

    String uploadProductThumbnail(String productId, MultipartFile file);

    List<String> uploadProductImages(String productId, List<MultipartFile> files);

    void deleteProductImage(String productId, String imageId);

    void deleteProductImageByObjectKey(String productId, String objectKey);

    PresignedUrlResponse getProductImageUrl(String objectKey);

    ProductStatsResponse getProductStats();
}