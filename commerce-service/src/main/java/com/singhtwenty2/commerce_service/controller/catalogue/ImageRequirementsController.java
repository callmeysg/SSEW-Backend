package com.singhtwenty2.commerce_service.controller.catalogue;

import com.singhtwenty2.commerce_service.data.dto.catalogue.ImageDTO.ImageRequirements;
import com.singhtwenty2.commerce_service.data.dto.common.GlobalApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/images/requirements")
@RequiredArgsConstructor
@Slf4j
public class ImageRequirementsController {

    @GetMapping
    public ResponseEntity<GlobalApiResponse<Map<String, ImageRequirements>>> getImageRequirements() {
        log.debug("Fetching image requirements for frontend");

        Map<String, ImageRequirements> requirements = Map.of(
                "brand_logo", ImageRequirements.forBrandLogo(),
                "product_image", ImageRequirements.forProductImage(),
                "product_thumbnail", ImageRequirements.forProductThumbnail()
        );

        return ResponseEntity.ok(
                GlobalApiResponse.<Map<String, ImageRequirements>>builder()
                        .success(true)
                        .message("Image requirements retrieved successfully")
                        .data(requirements)
                        .build()
        );
    }

    @GetMapping("/{type}")
    public ResponseEntity<GlobalApiResponse<ImageRequirements>> getImageRequirementsByType(
            @PathVariable String type) {
        log.debug("Fetching image requirements for type: {}", type);

        ImageRequirements requirements;
        switch (type.toLowerCase()) {
            case "brand-logo":
            case "brand_logo":
                requirements = ImageRequirements.forBrandLogo();
                break;
            case "product-image":
            case "product_image":
                requirements = ImageRequirements.forProductImage();
                break;
            case "product-thumbnail":
            case "product_thumbnail":
                requirements = ImageRequirements.forProductThumbnail();
                break;
            default:
                return ResponseEntity.badRequest().body(
                        GlobalApiResponse.<ImageRequirements>builder()
                                .success(false)
                                .message("Invalid image type. Supported types: brand-logo, product-image, product-thumbnail")
                                .data(null)
                                .build()
                );
        }

        return ResponseEntity.ok(
                GlobalApiResponse.<ImageRequirements>builder()
                        .success(true)
                        .message("Image requirements for " + type + " retrieved successfully")
                        .data(requirements)
                        .build()
        );
    }
}