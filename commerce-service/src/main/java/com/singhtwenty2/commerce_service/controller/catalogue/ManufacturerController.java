package com.singhtwenty2.commerce_service.controller.catalogue;

import com.singhtwenty2.commerce_service.data.dto.catalogue.ManufacturerDTO.ManufacturerResponse;
import com.singhtwenty2.commerce_service.data.dto.catalogue.ManufacturerDTO.UpdateManufacturerRequest;
import com.singhtwenty2.commerce_service.data.dto.common.GlobalApiResponse;
import com.singhtwenty2.commerce_service.data.dto.common.PageResponse;
import com.singhtwenty2.commerce_service.service.catalogue.ManufacturerImageService;
import com.singhtwenty2.commerce_service.service.catalogue.ManufacturerService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static com.singhtwenty2.commerce_service.data.dto.catalogue.ManufacturerDTO.*;
import static com.singhtwenty2.commerce_service.util.io.NetworkUtils.getClientIP;

@RestController
@RequestMapping("/v1/manufacturers")
@RequiredArgsConstructor
@Slf4j
public class ManufacturerController {

    private final ManufacturerService manufacturerService;
    private final ManufacturerImageService manufacturerImageService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<ManufacturerResponse>> createManufacturer(
            @Valid @RequestBody CreateManufacturerRequest createRequest,
            HttpServletRequest request
    ) {
        log.info("Manufacturer creation attempt from IP: {} for name: {}",
                getClientIP(request), createRequest.getName());

        ManufacturerResponse response = manufacturerService.createManufacturer(createRequest);

        log.info("Manufacturer created successfully with ID: {}", response.getManufacturerId());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                GlobalApiResponse.<ManufacturerResponse>builder()
                        .success(true)
                        .message("Manufacturer created successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{manufacturerId}")
    public ResponseEntity<GlobalApiResponse<ManufacturerResponse>> getManufacturerById(
            @PathVariable String manufacturerId,
            HttpServletRequest request
    ) {
        log.debug("Fetching manufacturer by ID: {} from IP: {}", manufacturerId, getClientIP(request));

        ManufacturerResponse response = manufacturerService.getManufacturerById(manufacturerId);

        return ResponseEntity.ok(
                GlobalApiResponse.<ManufacturerResponse>builder()
                        .success(true)
                        .message("Manufacturer retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<GlobalApiResponse<ManufacturerResponse>> getManufacturerBySlug(
            @PathVariable String slug,
            HttpServletRequest request
    ) {
        log.debug("Fetching manufacturer by slug: {} from IP: {}", slug, getClientIP(request));

        ManufacturerResponse response = manufacturerService.getManufacturerBySlug(slug);

        return ResponseEntity.ok(
                GlobalApiResponse.<ManufacturerResponse>builder()
                        .success(true)
                        .message("Manufacturer retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<GlobalApiResponse<PageResponse<ManufacturerResponse>>> getAllManufacturers(
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Fetching all manufacturers from IP: {}", getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<ManufacturerResponse> manufacturerPage = manufacturerService.getAllManufacturers(pageable);
        PageResponse<ManufacturerResponse> response = PageResponse.from(manufacturerPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<ManufacturerResponse>>builder()
                        .success(true)
                        .message("Manufacturers retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/active")
    public ResponseEntity<GlobalApiResponse<PageResponse<ManufacturerResponse>>> getActiveManufacturers(
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Fetching active manufacturers from IP: {}", getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<ManufacturerResponse> manufacturerPage = manufacturerService.getActiveManufacturers(pageable);
        PageResponse<ManufacturerResponse> response = PageResponse.from(manufacturerPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<ManufacturerResponse>>builder()
                        .success(true)
                        .message("Active manufacturers retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/categories")
    public ResponseEntity<GlobalApiResponse<PageResponse<ManufacturerResponse>>> getManufacturersByCategories(
            @RequestParam List<String> categoryIds,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Fetching manufacturers by category IDs: {} from IP: {}", categoryIds, getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<ManufacturerResponse> manufacturerPage = manufacturerService.getManufacturersByCategories(categoryIds, pageable);
        PageResponse<ManufacturerResponse> response = PageResponse.from(manufacturerPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<ManufacturerResponse>>builder()
                        .success(true)
                        .message("Manufacturers retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/categories/active")
    public ResponseEntity<GlobalApiResponse<PageResponse<ManufacturerResponse>>> getActiveManufacturersByCategories(
            @RequestParam List<String> categoryIds,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Fetching active manufacturers by category IDs: {} from IP: {}", categoryIds, getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<ManufacturerResponse> manufacturerPage = manufacturerService.getActiveManufacturersByCategories(categoryIds, pageable);
        PageResponse<ManufacturerResponse> response = PageResponse.from(manufacturerPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<ManufacturerResponse>>builder()
                        .success(true)
                        .message("Active manufacturers retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<GlobalApiResponse<PageResponse<ManufacturerResponse>>> searchManufacturers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Searching manufacturers with name: {} from IP: {}", name, getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<ManufacturerResponse> manufacturerPage = manufacturerService.searchManufacturers(name, categoryId, isActive, pageable);
        PageResponse<ManufacturerResponse> response = PageResponse.from(manufacturerPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<ManufacturerResponse>>builder()
                        .success(true)
                        .message("Manufacturers search completed successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/categories/ordered")
    public ResponseEntity<GlobalApiResponse<List<ManufacturerResponse>>> getManufacturersByCategoriesOrderedByDisplayOrder(
            @RequestParam List<String> categoryIds,
            HttpServletRequest request
    ) {
        log.debug("Fetching manufacturers by categories ordered by display order: {} from IP: {}", categoryIds, getClientIP(request));

        List<ManufacturerResponse> response = manufacturerService.getManufacturersByCategoriesOrderByDisplayOrder(categoryIds);

        return ResponseEntity.ok(
                GlobalApiResponse.<List<ManufacturerResponse>>builder()
                        .success(true)
                        .message("Manufacturers retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{manufacturerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<ManufacturerResponse>> updateManufacturer(
            @PathVariable String manufacturerId,
            @Valid @RequestBody UpdateManufacturerRequest updateRequest,
            HttpServletRequest request
    ) {
        log.info("Manufacturer update attempt from IP: {} for ID: {}", getClientIP(request), manufacturerId);

        ManufacturerResponse response = manufacturerService.updateManufacturer(manufacturerId, updateRequest);

        log.info("Manufacturer updated successfully with ID: {}", manufacturerId);

        return ResponseEntity.ok(
                GlobalApiResponse.<ManufacturerResponse>builder()
                        .success(true)
                        .message("Manufacturer updated successfully")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/{manufacturerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<Map<String, Object>>> deleteManufacturer(
            @PathVariable String manufacturerId,
            HttpServletRequest request
    ) {
        log.info("Manufacturer deletion attempt from IP: {} for ID: {}", getClientIP(request), manufacturerId);

        manufacturerService.deleteManufacturer(manufacturerId);

        log.info("Manufacturer deleted successfully with ID: {}", manufacturerId);

        return ResponseEntity.ok(
                GlobalApiResponse.<Map<String, Object>>builder()
                        .success(true)
                        .message("Manufacturer deleted successfully")
                        .data(null)
                        .build()
        );
    }

    @PatchMapping("/{manufacturerId}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<Map<String, Object>>> toggleManufacturerStatus(
            @PathVariable String manufacturerId,
            HttpServletRequest request
    ) {
        log.info("Manufacturer status toggle attempt from IP: {} for ID: {}", getClientIP(request), manufacturerId);

        manufacturerService.toggleManufacturerStatus(manufacturerId);

        log.info("Manufacturer status toggled successfully with ID: {}", manufacturerId);

        return ResponseEntity.ok(
                GlobalApiResponse.<Map<String, Object>>builder()
                        .success(true)
                        .message("Manufacturer status toggled successfully")
                        .data(null)
                        .build()
        );
    }

    @PutMapping("/{manufacturerId}/logo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<ManufacturerResponse>> manageManufacturerLogo(
            @PathVariable String manufacturerId,
            @RequestParam("logoFile") MultipartFile logoFile,
            HttpServletRequest request
    ) {
        log.info("Manufacturer logo management attempt from IP: {} for manufacturer ID: {}", getClientIP(request), manufacturerId);

        if (!manufacturerImageService.validateManufacturerLogoFile(logoFile)) {
            return ResponseEntity.badRequest().body(
                    GlobalApiResponse.<ManufacturerResponse>builder()
                            .success(false)
                            .message("Invalid logo file format or size")
                            .data(null)
                            .build()
            );
        }

        ManufacturerResponse existingManufacturer = manufacturerService.getManufacturerById(manufacturerId);
        String existingObjectKey = existingManufacturer.getLogoInfo() != null ? existingManufacturer.getLogoInfo().getObjectKey() : null;

        UpdateManufacturerRequest updateRequest = new UpdateManufacturerRequest();
        updateRequest.setLogoFile(logoFile);

        if (existingObjectKey != null) {
            updateRequest.setRemoveLogo(false);
        }

        ManufacturerResponse updatedManufacturer = manufacturerService.updateManufacturer(manufacturerId, updateRequest);

        if (updatedManufacturer.getLogoInfo() == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    GlobalApiResponse.<ManufacturerResponse>builder()
                            .success(false)
                            .message("Failed to process manufacturer logo")
                            .data(null)
                            .build()
            );
        }

        log.info("Manufacturer logo managed successfully for manufacturer ID: {}", manufacturerId);

        return ResponseEntity.ok(
                GlobalApiResponse.<ManufacturerResponse>builder()
                        .success(true)
                        .message("Manufacturer logo managed successfully")
                        .data(updatedManufacturer)
                        .build()
        );
    }
}