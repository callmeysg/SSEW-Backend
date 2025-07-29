package com.singhtwenty2.ssew_core.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.singhtwenty2.ssew_core.data.dto.catalog_management.BrandDTO.*;

@Service
public interface BrandService {

    BrandResponse createBrand(CreateBrandRequest createBrandRequest);

    BrandResponse getBrandById(String brandId);

    BrandResponse getBrandBySlug(String slug);

    Page<BrandResponse> getAllBrands(Pageable pageable);

    List<BrandResponse> getActiveBrands();

    Page<BrandResponse> getActiveBrands(Pageable pageable);

    Page<BrandResponse> getBrandsByCategory(String categoryId, Pageable pageable);

    Page<BrandResponse> getActiveBrandsByCategory(String categoryId, Pageable pageable);

    Page<BrandResponse> searchBrands(String name, String categoryId, Boolean isActive, Pageable pageable);

    BrandResponse updateBrand(String brandId, UpdateBrandRequest updateBrandRequest);

    void deleteBrand(String brandId);

    void toggleBrandStatus(String brandId);

    List<BrandResponse> getBrandsByCategoryOrderByDisplayOrder(String categoryId);
}