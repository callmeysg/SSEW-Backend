package com.singhtwenty2.ssew_core.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.singhtwenty2.ssew_core.data.dto.catalog_management.PreSignedUrlDTO.PresignedUrlResponse;
import static com.singhtwenty2.ssew_core.data.dto.catalog_management.ProductDTO.*;

public interface ProductService {

    // Core CRUD operations
    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse createVariant(String parentProductId, CreateVariantRequest request);

    ProductResponse getProductById(String productId);

    ProductResponse getProductBySlug(String slug);

    ProductResponse getProductBySku(String sku);

    ProductResponse updateProduct(String productId, UpdateProductRequest request);

    void deleteProduct(String productId);

    // Product listing and search

    /**
     * Get all products excluding variants (for frontend/customer facing API)
     * Returns only PARENT and STANDALONE products
     */
    Page<ProductSummary> getAllProducts(ProductSearchFilters filters, Pageable pageable);

    /**
     * Get all products including variants (for admin purposes)
     * Returns PARENT, STANDALONE, and VARIANT products
     */
    Page<ProductSummary> getAllProductsIncludingVariants(ProductSearchFilters filters, Pageable pageable);

    List<ProductVariantInfo> getProductVariants(String productId);

    // Image management
    String uploadProductThumbnail(String productId, MultipartFile file);

    List<String> uploadProductImages(String productId, List<MultipartFile> files);

    /**
     * Delete product image by image ID
     */
    void deleteProductImage(String productId, String imageId);

    /**
     * Delete product image by object key (supports both thumbnail and catalog images)
     */
    void deleteProductImageByObjectKey(String productId, String objectKey);

    PresignedUrlResponse getProductImageUrl(String objectKey);

    // Statistics and analytics
    ProductStatsResponse getProductStats();
}