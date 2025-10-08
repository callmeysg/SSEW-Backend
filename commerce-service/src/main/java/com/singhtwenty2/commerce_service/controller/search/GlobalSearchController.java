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
package com.singhtwenty2.commerce_service.controller.search;

import com.singhtwenty2.commerce_service.data.dto.common.GlobalApiResponse;
import com.singhtwenty2.commerce_service.data.dto.search.GlobalSearchDTO.GlobalSearchResponse;
import com.singhtwenty2.commerce_service.service.search.GlobalSearchService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.singhtwenty2.commerce_service.util.io.NetworkUtils.getClientIP;

@RestController
@RequestMapping("/v1/search")
@RequiredArgsConstructor
@Slf4j
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    @GetMapping
    public ResponseEntity<GlobalApiResponse<GlobalSearchResponse>> globalSearch(
            @RequestParam String q,
            @RequestParam(required = false) Integer productLimit,
            @RequestParam(required = false) Integer manufacturerLimit,
            @RequestParam(required = false) Integer categoryLimit,
            HttpServletRequest request
    ) {
        log.debug("Global search request from IP: {} with query: {}", getClientIP(request), q);

        GlobalSearchResponse response = globalSearchService.globalSearch(
                q,
                productLimit,
                manufacturerLimit,
                categoryLimit
        );

        log.debug("Global search completed - Products: {}, Manufacturers: {}, Categories: {}, Time: {}ms",
                response.getMetadata().getTotalProducts(),
                response.getMetadata().getTotalManufacturers(),
                response.getMetadata().getTotalCategories(),
                response.getMetadata().getSearchTimeMs());

        return ResponseEntity.ok(
                GlobalApiResponse.<GlobalSearchResponse>builder()
                        .success(true)
                        .message("Search completed successfully")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/cache")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<Map<String, Object>>> clearSearchCache(
            HttpServletRequest request
    ) {
        log.info("Search cache clear request from IP: {}", getClientIP(request));

        globalSearchService.clearSearchCache();

        log.info("Search cache cleared successfully");

        return ResponseEntity.ok(
                GlobalApiResponse.<Map<String, Object>>builder()
                        .success(true)
                        .message("Search cache cleared successfully")
                        .data(null)
                        .build()
        );
    }

    // As per the current req, this feature is not needed, therefore this endpoint will not be available
//    @DeleteMapping("/cache/{searchTerm}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<GlobalApiResponse<Map<String, Object>>> clearSearchCacheForTerm(
//            @PathVariable String searchTerm,
//            HttpServletRequest request
//    ) {
//        log.info("Search cache clear request for term: {} from IP: {}", searchTerm, getClientIP(request));
//
//        globalSearchService.clearSearchCacheForTerm(searchTerm);
//
//        log.info("Search cache cleared for term: {}", searchTerm);
//
//        return ResponseEntity.ok(
//                GlobalApiResponse.<Map<String, Object>>builder()
//                        .success(true)
//                        .message("Search cache cleared for term: " + searchTerm)
//                        .data(null)
//                        .build()
//        );
//    }
}