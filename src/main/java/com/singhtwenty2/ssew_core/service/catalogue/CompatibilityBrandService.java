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