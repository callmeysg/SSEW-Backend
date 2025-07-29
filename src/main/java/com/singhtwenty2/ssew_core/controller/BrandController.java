package com.singhtwenty2.ssew_core.controller;

import com.singhtwenty2.ssew_core.data.dto.catalog_management.BrandDTO;
import com.singhtwenty2.ssew_core.data.dto.catalog_management.BrandDTO.BrandResponse;
import com.singhtwenty2.ssew_core.data.dto.catalog_management.BrandDTO.UpdateBrandRequest;
import com.singhtwenty2.ssew_core.data.dto.common.GlobalApiResponse;
import com.singhtwenty2.ssew_core.data.dto.common.PageResponse;
import com.singhtwenty2.ssew_core.service.BrandService;
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

import java.util.List;
import java.util.Map;

import static com.singhtwenty2.ssew_core.util.io.NetworkUtils.getClientIP;

@RestController
@RequestMapping("/v1/brands")
@RequiredArgsConstructor
@Slf4j
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<BrandResponse>> createBrand(
            @Valid @RequestBody BrandDTO.CreateBrandRequest createRequest,
            HttpServletRequest request
    ) {
        log.info("Brand creation attempt from IP: {} for name: {}",
                getClientIP(request), createRequest.getName());

        BrandResponse response = brandService.createBrand(createRequest);

        log.info("Brand created successfully with ID: {}", response.getBrand_id());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                GlobalApiResponse.<BrandResponse>builder()
                        .success(true)
                        .message("Brand created successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{brandId}")
    public ResponseEntity<GlobalApiResponse<BrandResponse>> getBrandById(
            @PathVariable String brandId,
            HttpServletRequest request
    ) {
        log.debug("Fetching brand by ID: {} from IP: {}", brandId, getClientIP(request));

        BrandResponse response = brandService.getBrandById(brandId);

        return ResponseEntity.ok(
                GlobalApiResponse.<BrandResponse>builder()
                        .success(true)
                        .message("Brand retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<GlobalApiResponse<BrandResponse>> getBrandBySlug(
            @PathVariable String slug,
            HttpServletRequest request
    ) {
        log.debug("Fetching brand by slug: {} from IP: {}", slug, getClientIP(request));

        BrandResponse response = brandService.getBrandBySlug(slug);

        return ResponseEntity.ok(
                GlobalApiResponse.<BrandResponse>builder()
                        .success(true)
                        .message("Brand retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<GlobalApiResponse<PageResponse<BrandResponse>>> getAllBrands(
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Fetching all brands from IP: {}", getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<BrandResponse> brandPage = brandService.getAllBrands(pageable);
        PageResponse<BrandResponse> response = PageResponse.from(brandPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<BrandResponse>>builder()
                        .success(true)
                        .message("Brands retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/active")
    public ResponseEntity<GlobalApiResponse<PageResponse<BrandResponse>>> getActiveBrands(
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Fetching active brands from IP: {}", getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<BrandResponse> brandPage = brandService.getActiveBrands(pageable);
        PageResponse<BrandResponse> response = PageResponse.from(brandPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<BrandResponse>>builder()
                        .success(true)
                        .message("Active brands retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<GlobalApiResponse<PageResponse<BrandResponse>>> getBrandsByCategory(
            @PathVariable String categoryId,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Fetching brands by category: {} from IP: {}", categoryId, getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<BrandResponse> brandPage = brandService.getBrandsByCategory(categoryId, pageable);
        PageResponse<BrandResponse> response = PageResponse.from(brandPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<BrandResponse>>builder()
                        .success(true)
                        .message("Brands retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/category/{categoryId}/active")
    public ResponseEntity<GlobalApiResponse<PageResponse<BrandResponse>>> getActiveBrandsByCategory(
            @PathVariable String categoryId,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Fetching active brands by category: {} from IP: {}", categoryId, getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<BrandResponse> brandPage = brandService.getActiveBrandsByCategory(categoryId, pageable);
        PageResponse<BrandResponse> response = PageResponse.from(brandPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<BrandResponse>>builder()
                        .success(true)
                        .message("Active brands retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<GlobalApiResponse<PageResponse<BrandResponse>>> searchBrands(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Searching brands with name: {} from IP: {}", name, getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<BrandResponse> brandPage = brandService.searchBrands(name, categoryId, isActive, pageable);
        PageResponse<BrandResponse> response = PageResponse.from(brandPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<BrandResponse>>builder()
                        .success(true)
                        .message("Brands search completed successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/category/{categoryId}/ordered")
    public ResponseEntity<GlobalApiResponse<List<BrandResponse>>> getBrandsByCategoryOrderedByDisplayOrder(
            @PathVariable String categoryId,
            HttpServletRequest request
    ) {
        log.debug("Fetching brands by category ordered by display order: {} from IP: {}", categoryId, getClientIP(request));

        List<BrandResponse> response = brandService.getBrandsByCategoryOrderByDisplayOrder(categoryId);

        return ResponseEntity.ok(
                GlobalApiResponse.<List<BrandResponse>>builder()
                        .success(true)
                        .message("Brands retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{brandId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<BrandResponse>> updateBrand(
            @PathVariable String brandId,
            @Valid @RequestBody UpdateBrandRequest updateRequest,
            HttpServletRequest request
    ) {
        log.info("Brand update attempt from IP: {} for ID: {}", getClientIP(request), brandId);

        BrandResponse response = brandService.updateBrand(brandId, updateRequest);

        log.info("Brand updated successfully with ID: {}", brandId);

        return ResponseEntity.ok(
                GlobalApiResponse.<BrandResponse>builder()
                        .success(true)
                        .message("Brand updated successfully")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/{brandId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<Map<String, Object>>> deleteBrand(
            @PathVariable String brandId,
            HttpServletRequest request
    ) {
        log.info("Brand deletion attempt from IP: {} for ID: {}", getClientIP(request), brandId);

        brandService.deleteBrand(brandId);

        log.info("Brand deleted successfully with ID: {}", brandId);

        return ResponseEntity.ok(
                GlobalApiResponse.<Map<String, Object>>builder()
                        .success(true)
                        .message("Brand deleted successfully")
                        .data(null)
                        .build()
        );
    }

    @PatchMapping("/{brandId}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<Map<String, Object>>> toggleBrandStatus(
            @PathVariable String brandId,
            HttpServletRequest request
    ) {
        log.info("Brand status toggle attempt from IP: {} for ID: {}", getClientIP(request), brandId);

        brandService.toggleBrandStatus(brandId);

        log.info("Brand status toggled successfully with ID: {}", brandId);

        return ResponseEntity.ok(
                GlobalApiResponse.<Map<String, Object>>builder()
                        .success(true)
                        .message("Brand status toggled successfully")
                        .data(null)
                        .build()
        );
    }
}
