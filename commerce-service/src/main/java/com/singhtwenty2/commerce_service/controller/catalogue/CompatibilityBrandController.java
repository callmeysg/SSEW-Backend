package com.singhtwenty2.commerce_service.controller.catalogue;

import com.singhtwenty2.commerce_service.data.dto.catalogue.CompatibilityBrandDTO.CompatibilityBrandResponse;
import com.singhtwenty2.commerce_service.data.dto.catalogue.CompatibilityBrandDTO.CreateCompatibilityBrandRequest;
import com.singhtwenty2.commerce_service.data.dto.catalogue.CompatibilityBrandDTO.UpdateCompatibilityBrandRequest;
import com.singhtwenty2.commerce_service.data.dto.common.GlobalApiResponse;
import com.singhtwenty2.commerce_service.data.dto.common.PageResponse;
import com.singhtwenty2.commerce_service.service.catalogue.CompatibilityBrandService;
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

import java.util.Map;

import static com.singhtwenty2.commerce_service.util.io.NetworkUtils.getClientIP;

@RestController
@RequestMapping("/v1/compatibility-brands")
@RequiredArgsConstructor
@Slf4j
public class CompatibilityBrandController {

    private final CompatibilityBrandService compatibilityBrandService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<CompatibilityBrandResponse>> createCompatibilityBrand(
            @Valid @RequestBody CreateCompatibilityBrandRequest createRequest,
            HttpServletRequest request
    ) {
        log.info("Compatibility brand creation attempt from IP: {} for name: {}",
                getClientIP(request), createRequest.getName());

        CompatibilityBrandResponse response = compatibilityBrandService.createCompatibilityBrand(createRequest);

        log.info("Compatibility brand created successfully with ID: {}", response.getCompatibilityBrandId());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                GlobalApiResponse.<CompatibilityBrandResponse>builder()
                        .success(true)
                        .message("Compatibility brand created successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{compatibilityBrandId}")
    public ResponseEntity<GlobalApiResponse<CompatibilityBrandResponse>> getCompatibilityBrandById(
            @PathVariable String compatibilityBrandId,
            HttpServletRequest request
    ) {
        log.debug("Fetching compatibility brand by ID: {} from IP: {}", compatibilityBrandId, getClientIP(request));

        CompatibilityBrandResponse response = compatibilityBrandService.getCompatibilityBrandById(compatibilityBrandId);

        return ResponseEntity.ok(
                GlobalApiResponse.<CompatibilityBrandResponse>builder()
                        .success(true)
                        .message("Compatibility brand retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<GlobalApiResponse<CompatibilityBrandResponse>> getCompatibilityBrandBySlug(
            @PathVariable String slug,
            HttpServletRequest request
    ) {
        log.debug("Fetching compatibility brand by slug: {} from IP: {}", slug, getClientIP(request));

        CompatibilityBrandResponse response = compatibilityBrandService.getCompatibilityBrandBySlug(slug);

        return ResponseEntity.ok(
                GlobalApiResponse.<CompatibilityBrandResponse>builder()
                        .success(true)
                        .message("Compatibility brand retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<GlobalApiResponse<PageResponse<CompatibilityBrandResponse>>> getAllCompatibilityBrands(
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Fetching all compatibility brands from IP: {}", getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<CompatibilityBrandResponse> compatibilityBrandPage = compatibilityBrandService.getAllCompatibilityBrands(pageable);
        PageResponse<CompatibilityBrandResponse> response = PageResponse.from(compatibilityBrandPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<CompatibilityBrandResponse>>builder()
                        .success(true)
                        .message("Compatibility brands retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<GlobalApiResponse<PageResponse<CompatibilityBrandResponse>>> searchCompatibilityBrands(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Searching compatibility brands with term: {} from IP: {}", searchTerm, getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<CompatibilityBrandResponse> compatibilityBrandPage = compatibilityBrandService.searchCompatibilityBrands(searchTerm, pageable);
        PageResponse<CompatibilityBrandResponse> response = PageResponse.from(compatibilityBrandPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<CompatibilityBrandResponse>>builder()
                        .success(true)
                        .message("Compatibility brands search completed successfully")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{compatibilityBrandId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<CompatibilityBrandResponse>> updateCompatibilityBrand(
            @PathVariable String compatibilityBrandId,
            @Valid @RequestBody UpdateCompatibilityBrandRequest updateRequest,
            HttpServletRequest request
    ) {
        log.info("Compatibility brand update attempt from IP: {} for ID: {}", getClientIP(request), compatibilityBrandId);

        CompatibilityBrandResponse response = compatibilityBrandService.updateCompatibilityBrand(compatibilityBrandId, updateRequest);

        log.info("Compatibility brand updated successfully with ID: {}", compatibilityBrandId);

        return ResponseEntity.ok(
                GlobalApiResponse.<CompatibilityBrandResponse>builder()
                        .success(true)
                        .message("Compatibility brand updated successfully")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/{compatibilityBrandId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<Map<String, Object>>> deleteCompatibilityBrand(
            @PathVariable String compatibilityBrandId,
            HttpServletRequest request
    ) {
        log.info("Compatibility brand deletion attempt from IP: {} for ID: {}", getClientIP(request), compatibilityBrandId);

        compatibilityBrandService.deleteCompatibilityBrand(compatibilityBrandId);

        log.info("Compatibility brand deleted successfully with ID: {}", compatibilityBrandId);

        return ResponseEntity.ok(
                GlobalApiResponse.<Map<String, Object>>builder()
                        .success(true)
                        .message("Compatibility brand deleted successfully")
                        .data(null)
                        .build()
        );
    }
}