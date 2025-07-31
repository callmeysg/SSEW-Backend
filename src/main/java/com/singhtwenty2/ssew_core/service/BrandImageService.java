package com.singhtwenty2.ssew_core.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.singhtwenty2.ssew_core.data.dto.catalog_management.BrandImage.BrandImageResult;

@Service
public interface BrandImageService {

    BrandImageResult processBrandLogo(MultipartFile logoFile, String brandId);

    void deleteBrandLogo(String brandId, String objectKey);

    BrandImageResult updateBrandLogo(MultipartFile logoFile, String brandId, String existingObjectKey);

    String generateLogoAccessUrl(String objectKey, Integer expirationMinutes);

    boolean validateBrandLogoFile(MultipartFile file);
}
