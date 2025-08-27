package com.singhtwenty2.ssew_core.controller;

import com.singhtwenty2.ssew_core.data.dto.common.GlobalApiResponse;
import com.singhtwenty2.ssew_core.data.dto.common.PageResponse;
import com.singhtwenty2.ssew_core.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.singhtwenty2.ssew_core.data.dto.catalog_management.PreSignedUrlDTO.PresignedUrlResponse;
import static com.singhtwenty2.ssew_core.data.dto.catalog_management.ProductDTO.*;
import static com.singhtwenty2.ssew_core.util.io.NetworkUtils.getClientIP;

@RestController
@RequestMapping("/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    private static final Map<String, String> SORT_FIELD_MAPPING = Map.of(
            "created_at", "createdAt",
            "updated_at", "updatedAt",
            "name", "name",
            "price", "price",
            "sku", "sku",
            "model_number", "modelNumber",
            "display_order", "displayOrder"
    );

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest createRequest,
            HttpServletRequest request
    ) {
        log.info("Product creation attempt from IP: {} for name: {}",
                getClientIP(request), createRequest.getName());

        ProductResponse response = productService.createProduct(createRequest);

        log.info("Product created successfully with ID: {}", response.getProductId());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                GlobalApiResponse.<ProductResponse>builder()
                        .success(true)
                        .message("Product created successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/{productId}/variants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<ProductResponse>> createVariant(
            @PathVariable String productId,
            @Valid @RequestBody CreateVariantRequest createRequest,
            HttpServletRequest request
    ) {
        log.info("Variant creation attempt from IP: {} for parent product: {}",
                getClientIP(request), productId);

        ProductResponse response = productService.createVariant(productId, createRequest);

        log.info("Variant created successfully with ID: {}", response.getProductId());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                GlobalApiResponse.<ProductResponse>builder()
                        .success(true)
                        .message("Product variant created successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<GlobalApiResponse<ProductResponse>> getProductById(
            @PathVariable String productId,
            HttpServletRequest request
    ) {
        log.debug("Fetching product by ID: {} from IP: {}", productId, getClientIP(request));

        ProductResponse response = productService.getProductById(productId);

        return ResponseEntity.ok(
                GlobalApiResponse.<ProductResponse>builder()
                        .success(true)
                        .message("Product retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<GlobalApiResponse<ProductResponse>> getProductBySlug(
            @PathVariable String slug,
            HttpServletRequest request
    ) {
        log.debug("Fetching product by slug: {} from IP: {}", slug, getClientIP(request));

        ProductResponse response = productService.getProductBySlug(slug);

        return ResponseEntity.ok(
                GlobalApiResponse.<ProductResponse>builder()
                        .success(true)
                        .message("Product retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<GlobalApiResponse<ProductResponse>> getProductBySku(
            @PathVariable String sku,
            HttpServletRequest request
    ) {
        log.debug("Fetching product by SKU: {} from IP: {}", sku, getClientIP(request));

        ProductResponse response = productService.getProductBySku(sku);

        return ResponseEntity.ok(
                GlobalApiResponse.<ProductResponse>builder()
                        .success(true)
                        .message("Product retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    // Main endpoint for frontend - excludes variants
    @GetMapping("/search")
    public ResponseEntity<GlobalApiResponse<PageResponse<ProductSummary>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean isFeatured,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "created_at") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest request
    ) {
        log.debug("Searching products with filters from IP: {}", getClientIP(request));

        ProductSearchFilters filters = ProductSearchFilters.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .brandId(brandId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .isFeatured(isFeatured)
                .inStock(inStock)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(index)
                .size(limit)
                .build();

        String entitySortField = SORT_FIELD_MAPPING.getOrDefault(sortBy, "createdAt");

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(entitySortField).descending() : Sort.by(entitySortField).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<ProductSummary> productPage = productService.getAllProducts(filters, pageable);
        PageResponse<ProductSummary> response = PageResponse.from(productPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<ProductSummary>>builder()
                        .success(true)
                        .message("Products retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    // Admin endpoint - includes all products including variants
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<PageResponse<ProductSummary>>> getAllProductsIncludingVariants(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean isFeatured,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "created_at") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest request
    ) {
        log.debug("Admin fetching all products including variants from IP: {}", getClientIP(request));

        ProductSearchFilters filters = ProductSearchFilters.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .brandId(brandId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .isFeatured(isFeatured)
                .inStock(inStock)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(index)
                .size(limit)
                .build();

        String entitySortField = SORT_FIELD_MAPPING.getOrDefault(sortBy, "createdAt");

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(entitySortField).descending() : Sort.by(entitySortField).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<ProductSummary> productPage = productService.getAllProductsIncludingVariants(filters, pageable);
        PageResponse<ProductSummary> response = PageResponse.from(productPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<ProductSummary>>builder()
                        .success(true)
                        .message("All products retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{productId}/variants")
    public ResponseEntity<GlobalApiResponse<List<ProductVariantInfo>>> getProductVariants(
            @PathVariable String productId,
            HttpServletRequest request
    ) {
        log.debug("Fetching variants for product: {} from IP: {}", productId, getClientIP(request));

        List<ProductVariantInfo> variants = productService.getProductVariants(productId);

        return ResponseEntity.ok(
                GlobalApiResponse.<List<ProductVariantInfo>>builder()
                        .success(true)
                        .message("Product variants retrieved successfully")
                        .data(variants)
                        .build()
        );
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<ProductResponse>> updateProduct(
            @PathVariable String productId,
            @Valid @RequestBody UpdateProductRequest updateRequest,
            HttpServletRequest request
    ) {
        log.info("Product update attempt from IP: {} for ID: {}",
                getClientIP(request), productId);

        ProductResponse response = productService.updateProduct(productId, updateRequest);

        log.info("Product updated successfully with ID: {}", productId);

        return ResponseEntity.ok(
                GlobalApiResponse.<ProductResponse>builder()
                        .success(true)
                        .message("Product updated successfully")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<Void>> deleteProduct(
            @PathVariable String productId,
            HttpServletRequest request
    ) {
        log.info("Product deletion attempt from IP: {} for ID: {}",
                getClientIP(request), productId);

        productService.deleteProduct(productId);

        log.info("Product deleted successfully with ID: {}", productId);

        return ResponseEntity.ok(
                GlobalApiResponse.<Void>builder()
                        .success(true)
                        .message("Product deleted successfully")
                        .build()
        );
    }

    @PostMapping(value = "/{productId}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<String>> uploadProductThumbnail(
            @PathVariable String productId,
            @RequestParam("thumbnail") MultipartFile file,
            HttpServletRequest request
    ) {
        log.info("Thumbnail upload attempt from IP: {} for product: {}",
                getClientIP(request), productId);

        String objectKey = productService.uploadProductThumbnail(productId, file);

        log.info("Thumbnail uploaded successfully for product: {}", productId);

        return ResponseEntity.ok(
                GlobalApiResponse.<String>builder()
                        .success(true)
                        .message("Product thumbnail uploaded successfully")
                        .data(objectKey)
                        .build()
        );
    }

    @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<List<String>>> uploadProductImages(
            @PathVariable String productId,
            @RequestParam("images") List<MultipartFile> files,
            HttpServletRequest request
    ) {
        log.info("Images upload attempt from IP: {} for product: {} (count: {})",
                getClientIP(request), productId, files.size());

        List<String> objectKeys = productService.uploadProductImages(productId, files);

        log.info("Images uploaded successfully for product: {} (count: {})", productId, objectKeys.size());

        return ResponseEntity.ok(
                GlobalApiResponse.<List<String>>builder()
                        .success(true)
                        .message("Product images uploaded successfully")
                        .data(objectKeys)
                        .build()
        );
    }

    @DeleteMapping("/{productId}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<Void>> deleteProductImageByQuery(
            @PathVariable String productId,
            @RequestParam String objectKey,
            HttpServletRequest request
    ) {
        log.info("Image deletion attempt from IP: {} for product: {}, objectKey: {}",
                getClientIP(request), productId, objectKey);

        productService.deleteProductImageByObjectKey(productId, objectKey);

        log.info("Image deleted successfully for product: {}, objectKey: {}", productId, objectKey);

        return ResponseEntity.ok(
                GlobalApiResponse.<Void>builder()
                        .success(true)
                        .message("Product image deleted successfully")
                        .build()
        );
    }

    @GetMapping("/images")
    public ResponseEntity<GlobalApiResponse<PresignedUrlResponse>> getProductImageUrlByQuery(
            @RequestParam String objectKey,
            HttpServletRequest request
    ) {
        log.debug("Generating presigned URL for image: {} from IP: {}", objectKey, getClientIP(request));

        PresignedUrlResponse response = productService.getProductImageUrl(objectKey);

        return ResponseEntity.ok(
                GlobalApiResponse.<PresignedUrlResponse>builder()
                        .success(true)
                        .message("Presigned URL generated successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<ProductStatsResponse>> getProductStats(
            HttpServletRequest request
    ) {
        log.debug("Fetching product stats from IP: {}", getClientIP(request));

        ProductStatsResponse stats = productService.getProductStats();

        return ResponseEntity.ok(
                GlobalApiResponse.<ProductStatsResponse>builder()
                        .success(true)
                        .message("Product statistics retrieved successfully")
                        .data(stats)
                        .build()
        );
    }
}