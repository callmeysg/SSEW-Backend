package com.singhtwenty2.ssew_core.service.catalogue;

import org.springframework.web.multipart.MultipartFile;

import static com.singhtwenty2.ssew_core.data.dto.catalogue.ManufacturerImage.ManufacturerImageResult;

public interface ManufacturerImageService {

    ManufacturerImageResult processManufacturerLogo(MultipartFile logoFile, String manufacturerSlug);

    void deleteManufacturerLogo(String manufacturerSlug, String objectKey);

    ManufacturerImageResult updateManufacturerLogo(MultipartFile logoFile, String manufacturerSlug, String existingObjectKey);

    String generateLogoAccessUrl(String objectKey, Integer expirationMinutes);

    boolean validateManufacturerLogoFile(MultipartFile file);
}