package com.singhtwenty2.ssew_core.service;

import com.singhtwenty2.ssew_core.data.dto.catalog_management.ProductDTO.CreateProductRequest;
import com.singhtwenty2.ssew_core.data.dto.catalog_management.ProductDTO.ProductInventoryUpdateRequest;
import com.singhtwenty2.ssew_core.data.dto.catalog_management.ProductDTO.ProductResponse;
import com.singhtwenty2.ssew_core.data.dto.catalog_management.ProductDTO.UpdateProductRequest;
import com.singhtwenty2.ssew_core.data.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public interface ProductService {

    ProductResponse createProduct(CreateProductRequest createProductRequest);

    ProductResponse getProductById(String productId);

    ProductResponse getProductBySlug(String slug);

    ProductResponse getProductBySku(String sku);

    Page<ProductResponse> getAllProducts(Pageable pageable);

    Page<ProductResponse> getProductsByStatus(ProductStatus status, Pageable pageable);

    Page<ProductResponse> getFeaturedProducts(Pageable pageable);

    Page<ProductResponse> getProductsByBrand(String brandId, Pageable pageable);

    Page<ProductResponse> getProductsByBrandAndStatus(String brandId, ProductStatus status, Pageable pageable);

    Page<ProductResponse> getFeaturedProductsByStatus(ProductStatus status, Pageable pageable);

    Page<ProductResponse> searchProducts(
            String name,
            String sku,
            String brandId,
            ProductStatus status,
            Boolean isFeatured,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    );

    List<ProductResponse> getLowStockProducts();

    List<ProductResponse> getOutOfStockProducts();

    ProductResponse updateProduct(String productId, UpdateProductRequest updateProductRequest);

    ProductResponse updateProductInventory(String productId, ProductInventoryUpdateRequest inventoryUpdateRequest);

    ProductResponse updateProductStatus(String productId, ProductStatus status);

    ProductResponse toggleFeaturedStatus(String productId);

    void deleteProduct(String productId);
}