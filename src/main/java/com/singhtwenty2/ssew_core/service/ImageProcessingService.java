package com.singhtwenty2.ssew_core.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.singhtwenty2.ssew_core.data.dto.catalog_management.ImageDTO.*;

@Service
public interface ImageProcessingService {

    ProcessedImageResult processImage(
            MultipartFile file,
            ImageProcessingConfig config);

    ProcessedImageResult processBrandLogo(MultipartFile file);

    ProcessedImageResult processProductImage(MultipartFile file, boolean isThumbnail);

    boolean isValidImageFormat(MultipartFile file);

    ImageMetadata extractImageMetadata(MultipartFile file);
}
