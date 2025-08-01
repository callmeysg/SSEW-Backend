package com.singhtwenty2.ssew_core.controller;

import com.singhtwenty2.ssew_core.data.dto.catalog_management.ProductDTO.CreateProductRequest;
import com.singhtwenty2.ssew_core.data.dto.catalog_management.ProductDTO.ProductInventoryUpdateRequest;
import com.singhtwenty2.ssew_core.data.dto.catalog_management.ProductDTO.ProductResponse;
import com.singhtwenty2.ssew_core.data.dto.catalog_management.ProductDTO.UpdateProductRequest;
import com.singhtwenty2.ssew_core.data.dto.common.GlobalApiResponse;
import com.singhtwenty2.ssew_core.data.dto.common.PageResponse;
import com.singhtwenty2.ssew_core.data.enums.ProductStatus;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.singhtwenty2.ssew_core.util.io.NetworkUtils.getClientIP;

@RestController
@RequestMapping("/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

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

    @GetMapping
    public ResponseEntity<GlobalApiResponse<PageResponse<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Fetching all products from IP: {}", getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<ProductResponse> productPage = productService.getAllProducts(pageable);
        PageResponse<ProductResponse> response = PageResponse.from(productPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<ProductResponse>>builder()
                        .success(true)
                        .message("Products retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<GlobalApiResponse<PageResponse<ProductResponse>>> getProductsByStatus(
            @PathVariable ProductStatus status,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Fetching products by status: {} from IP: {}", status, getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<ProductResponse> productPage = productService.getProductsByStatus(status, pageable);
        PageResponse<ProductResponse> response = PageResponse.from(productPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<ProductResponse>>builder()
                        .success(true)
                        .message("Products retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/featured")
    public ResponseEntity<GlobalApiResponse<PageResponse<ProductResponse>>> getFeaturedProducts(
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Fetching featured products from IP: {}", getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<ProductResponse> productPage = productService.getFeaturedProducts(pageable);
        PageResponse<ProductResponse> response = PageResponse.from(productPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<ProductResponse>>builder()
                        .success(true)
                        .message("Featured products retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/featured/status/{status}")
    public ResponseEntity<GlobalApiResponse<PageResponse<ProductResponse>>> getFeaturedProductsByStatus(
            @PathVariable ProductStatus status,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Fetching featured products by status: {} from IP: {}", status, getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<ProductResponse> productPage = productService.getFeaturedProductsByStatus(status, pageable);
        PageResponse<ProductResponse> response = PageResponse.from(productPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<ProductResponse>>builder()
                        .success(true)
                        .message("Featured products retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/brand/{brandId}")
    public ResponseEntity<GlobalApiResponse<PageResponse<ProductResponse>>> getProductsByBrand(
            @PathVariable String brandId,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Fetching products by brand: {} from IP: {}", brandId, getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<ProductResponse> productPage = productService.getProductsByBrand(brandId, pageable);
        PageResponse<ProductResponse> response = PageResponse.from(productPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<ProductResponse>>builder()
                        .success(true)
                        .message("Products retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/brand/{brandId}/status/{status}")
    public ResponseEntity<GlobalApiResponse<PageResponse<ProductResponse>>> getProductsByBrandAndStatus(
            @PathVariable String brandId,
            @PathVariable ProductStatus status,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Fetching products by brand: {} and status: {} from IP: {}", brandId, status, getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<ProductResponse> productPage = productService.getProductsByBrandAndStatus(brandId, status, pageable);
        PageResponse<ProductResponse> response = PageResponse.from(productPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<ProductResponse>>builder()
                        .success(true)
                        .message("Products retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<GlobalApiResponse<PageResponse<ProductResponse>>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String brandId,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) Boolean isFeatured,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Searching products from IP: {}", getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<ProductResponse> productPage = productService.searchProducts(
                name, sku, brandId, status, isFeatured, minPrice, maxPrice, pageable);
        PageResponse<ProductResponse> response = PageResponse.from(productPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<ProductResponse>>builder()
                        .success(true)
                        .message("Products search completed successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/inventory/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<List<ProductResponse>>> getLowStockProducts(
            HttpServletRequest request
    ) {
        log.debug("Fetching low stock products from IP: {}", getClientIP(request));

        List<ProductResponse> response = productService.getLowStockProducts();

        return ResponseEntity.ok(
                GlobalApiResponse.<List<ProductResponse>>builder()
                        .success(true)
                        .message("Low stock products retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/inventory/out-of-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<List<ProductResponse>>> getOutOfStockProducts(
            HttpServletRequest request
    ) {
        log.debug("Fetching out of stock products from IP: {}", getClientIP(request));

        List<ProductResponse> response = productService.getOutOfStockProducts();

        return ResponseEntity.ok(
                GlobalApiResponse.<List<ProductResponse>>builder()
                        .success(true)
                        .message("Out of stock products retrieved successfully")
                        .data(response)
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
        log.info("Product update attempt from IP: {} for ID: {}", getClientIP(request), productId);

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

    @PatchMapping("/{productId}/inventory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<ProductResponse>> updateProductInventory(
            @PathVariable String productId,
            @Valid @RequestBody ProductInventoryUpdateRequest inventoryRequest,
            HttpServletRequest request
    ) {
        log.info("Product inventory update attempt from IP: {} for ID: {}", getClientIP(request), productId);

        ProductResponse response = productService.updateProductInventory(productId, inventoryRequest);

        log.info("Product inventory updated successfully with ID: {}", productId);

        return ResponseEntity.ok(
                GlobalApiResponse.<ProductResponse>builder()
                        .success(true)
                        .message("Product inventory updated successfully")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{productId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<ProductResponse>> updateProductStatus(
            @PathVariable String productId,
            @RequestParam ProductStatus status,
            HttpServletRequest request
    ) {
        log.info("Product status update attempt from IP: {} for ID: {} to status: {}",
                getClientIP(request), productId, status);

        ProductResponse response = productService.updateProductStatus(productId, status);

        log.info("Product status updated successfully with ID: {}", productId);

        return ResponseEntity.ok(
                GlobalApiResponse.<ProductResponse>builder()
                        .success(true)
                        .message("Product status updated successfully")
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{productId}/toggle-featured")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<ProductResponse>> toggleFeaturedStatus(
            @PathVariable String productId,
            HttpServletRequest request
    ) {
        log.info("Product featured status toggle attempt from IP: {} for ID: {}", getClientIP(request), productId);

        ProductResponse response = productService.toggleFeaturedStatus(productId);

        log.info("Product featured status toggled successfully with ID: {}", productId);

        return ResponseEntity.ok(
                GlobalApiResponse.<ProductResponse>builder()
                        .success(true)
                        .message("Product featured status toggled successfully")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<Map<String, Object>>> deleteProduct(
            @PathVariable String productId,
            HttpServletRequest request
    ) {
        log.info("Product deletion attempt from IP: {} for ID: {}", getClientIP(request), productId);

        productService.deleteProduct(productId);

        log.info("Product deleted successfully with ID: {}", productId);

        return ResponseEntity.ok(
                GlobalApiResponse.<Map<String, Object>>builder()
                        .success(true)
                        .message("Product deleted successfully")
                        .data(null)
                        .build()
        );
    }
}