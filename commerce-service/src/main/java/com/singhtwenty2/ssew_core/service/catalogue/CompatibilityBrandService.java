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

import com.singhtwenty2.ssew_core.data.dto.catalogue.CompatibilityBrandDTO.CompatibilityBrandResponse;
import com.singhtwenty2.ssew_core.data.dto.catalogue.CompatibilityBrandDTO.CreateCompatibilityBrandRequest;
import com.singhtwenty2.ssew_core.data.dto.catalogue.CompatibilityBrandDTO.UpdateCompatibilityBrandRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface CompatibilityBrandService {

    CompatibilityBrandResponse createCompatibilityBrand(CreateCompatibilityBrandRequest createRequest);

    CompatibilityBrandResponse getCompatibilityBrandById(String compatibilityBrandId);

    CompatibilityBrandResponse getCompatibilityBrandBySlug(String slug);

    Page<CompatibilityBrandResponse> getAllCompatibilityBrands(Pageable pageable);

    Page<CompatibilityBrandResponse> searchCompatibilityBrands(String searchTerm, Pageable pageable);

    CompatibilityBrandResponse updateCompatibilityBrand(String compatibilityBrandId, UpdateCompatibilityBrandRequest updateRequest);

    void deleteCompatibilityBrand(String compatibilityBrandId);
}