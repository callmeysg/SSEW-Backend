package com.singhtwenty2.commerce_service.service.catalogue.helper;

import com.singhtwenty2.commerce_service.data.entity.Manufacturer;
import com.singhtwenty2.commerce_service.data.entity.Product;
import com.singhtwenty2.commerce_service.data.entity.ProductImage;
import com.singhtwenty2.commerce_service.data.enums.VariantType;
import com.singhtwenty2.commerce_service.service.file_handeling.S3Service;
import com.singhtwenty2.commerce_service.util.sanitizer.SpecificationSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.singhtwenty2.commerce_service.data.dto.catalogue.PreSignedUrlDTO.PresignedUrlResponse;
import static com.singhtwenty2.commerce_service.data.dto.catalogue.ProductDTO.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductMappingService {

    private final S3Service s3Service;
    private final SpecificationSanitizer specificationSanitizer;

    public void mapCreateRequestToProduct(CreateProductRequest request, Product product, Manufacturer manufacturer, Product parentProduct) {
        product.setName(request.getName());
        product.setModelNumber(request.getModelNumber());
        product.setDescription(request.getDescription());
        product.setShortDescription(request.getShortDescription());
        product.setSpecifications(specificationSanitizer.sanitizeSpecifications(request.getSpecifications()));
        product.setPrice(request.getPrice());
        product.setCompareAtPrice(request.getCompareAtPrice());
        product.setCostPrice(request.getCostPrice());
        product.setIsFeatured(request.getIsFeatured());
        product.setDisplayOrder(request.getDisplayOrder());
        product.setMetaTitle(request.getMetaTitle());
        product.setMetaDescription(request.getMetaDescription());
        product.setMetaKeywords(request.getMetaKeywords());
        product.setSearchTags(request.getSearchTags());
        product.setManufacturer(manufacturer);
        product.setParentProduct(parentProduct);

        if (parentProduct != null) {
            product.setVariantType(VariantType.VARIANT);
        } else {
            product.setVariantType(VariantType.STANDALONE);
        }
    }

    public void mapVariantRequestToProduct(CreateVariantRequest request, Product variant, Manufacturer manufacturer) {
        variant.setName(request.getName());
        variant.setModelNumber(request.getModelNumber());
        variant.setDescription(request.getDescription());
        variant.setShortDescription(request.getShortDescription());
        variant.setSpecifications(specificationSanitizer.sanitizeSpecifications(request.getSpecifications()));
        variant.setPrice(request.getPrice());
        variant.setCompareAtPrice(request.getCompareAtPrice());
        variant.setCostPrice(request.getCostPrice());
        variant.setMetaTitle(request.getMetaTitle());
        variant.setMetaDescription(request.getMetaDescription());
        variant.setMetaKeywords(request.getMetaKeywords());
        variant.setSearchTags(request.getSearchTags());
        variant.setManufacturer(manufacturer);
        variant.setVariantType(VariantType.VARIANT);
    }

    public void mapUpdateRequestToProduct(UpdateProductRequest request, Product product) {
        if (request.getName() != null) product.setName(request.getName());
        if (request.getModelNumber() != null) product.setModelNumber(request.getModelNumber());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getShortDescription() != null) product.setShortDescription(request.getShortDescription());
        if (request.getSpecifications() != null)
            product.setSpecifications(specificationSanitizer.sanitizeSpecifications(request.getSpecifications()));
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getCompareAtPrice() != null) product.setCompareAtPrice(request.getCompareAtPrice());
        if (request.getCostPrice() != null) product.setCostPrice(request.getCostPrice());
        if (request.getIsActive() != null) product.setIsActive(request.getIsActive());
        if (request.getIsFeatured() != null) product.setIsFeatured(request.getIsFeatured());
        if (request.getDisplayOrder() != null) product.setDisplayOrder(request.getDisplayOrder());
        if (request.getMetaTitle() != null) product.setMetaTitle(request.getMetaTitle());
        if (request.getMetaDescription() != null) product.setMetaDescription(request.getMetaDescription());
        if (request.getMetaKeywords() != null) product.setMetaKeywords(request.getMetaKeywords());
        if (request.getSearchTags() != null) product.setSearchTags(request.getSearchTags());
    }

    public ProductResponse mapProductToResponse(Product product, boolean includeVariants) {
        ProductResponse.ProductResponseBuilder builder = ProductResponse.builder()
                .productId(product.getId().toString())
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .modelNumber(product.getModelNumber())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .specifications(product.getSpecifications())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .costPrice(product.getCostPrice())
                .isActive(product.getIsActive())
                .isFeatured(product.getIsFeatured())
                .displayOrder(product.getDisplayOrder())
                .metaTitle(product.getMetaTitle())
                .metaDescription(product.getMetaDescription())
                .metaKeywords(product.getMetaKeywords())
                .searchTags(product.getSearchTags())
                .variantType(product.getVariantType().name())
                .variantPosition(product.getVariantPosition())
                .createdAt(product.getCreatedAt() != null
                        ? product.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .updatedAt(product.getUpdatedAt() != null
                        ? product.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .manufacturerId(product.getManufacturer().getId().toString())
                .manufacturerName(product.getManufacturerName())
                .categoryIds(product.getCategoryIds())
                .categoryNames(product.getCategoryNames())
                .parentProductId(product.getParentProduct() != null ? product.getParentProduct().getId().toString() : null);

        if (product.getCompatibilityBrands() != null && !product.getCompatibilityBrands().isEmpty()) {
            List<CompatibilityBrandInfo> compatibilityBrandInfos = product.getCompatibilityBrands().stream()
                    .map(brand -> CompatibilityBrandInfo.builder()
                            .compatibilityBrandId(brand.getId().toString())
                            .name(brand.getName())
                            .slug(brand.getSlug())
                            .build())
                    .collect(Collectors.toList());
            builder.compatibilityBrands(compatibilityBrandInfos);
        }

        if (product.getThumbnailObjectKey() != null &&
            s3Service.imageExists(product.getThumbnailObjectKey())) {
            PresignedUrlResponse thumbnailUrl = s3Service.generateReadPresignedUrl(product.getThumbnailObjectKey(), 60);
            builder.thumbnailInfo(ThumbnailInfo.builder()
                    .objectKey(product.getThumbnailObjectKey())
                    .accessUrl(thumbnailUrl.getPresignedUrl())
                    .fileSize(product.getThumbnailFileSize())
                    .contentType(product.getThumbnailContentType())
                    .width(product.getThumbnailWidth())
                    .height(product.getThumbnailHeight())
                    .build());
        }

        List<ProductImageInfo> imageInfos = product.getProductImages().stream()
                .map(this::mapProductImageToInfo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        builder.catalogImages(imageInfos);

        if (includeVariants && product.hasVariants()) {
            List<ProductVariantInfo> variants = product.getVariants().stream()
                    .map(this::mapProductToVariantInfo)
                    .collect(Collectors.toList());
            builder.variants(variants);
            builder.totalVariants((long) variants.size());
        } else {
            builder.totalVariants(0L);
        }

        return builder.build();
    }

    public ProductSummary mapProductToSummary(Product product) {
        ProductSummary.ProductSummaryBuilder builder = ProductSummary.builder()
                .productId(product.getId().toString())
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .modelNumber(product.getModelNumber())
                .shortDescription(product.getShortDescription())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .isActive(product.getIsActive())
                .isFeatured(product.getIsFeatured())
                .variantType(product.getVariantType().name())
                .manufacturerName(product.getManufacturerName())
                .categoryNames(product.getCategoryNames())
                .totalVariants((long) product.getVariants().size())
                .createdAt(product.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        if (product.getCompatibilityBrands() != null && !product.getCompatibilityBrands().isEmpty()) {
            List<CompatibilityBrandInfo> compatibilityBrandInfos = product.getCompatibilityBrands().stream()
                    .map(brand -> CompatibilityBrandInfo.builder()
                            .compatibilityBrandId(brand.getId().toString())
                            .name(brand.getName())
                            .slug(brand.getSlug())
                            .build())
                    .collect(Collectors.toList());
            builder.compatibilityBrands(compatibilityBrandInfos);
        }

        if (product.getThumbnailObjectKey() != null) {
            PresignedUrlResponse thumbnailUrl = s3Service.generateReadPresignedUrl(product.getThumbnailObjectKey(), 60);
            builder.thumbnailInfo(ThumbnailInfo.builder()
                    .objectKey(product.getThumbnailObjectKey())
                    .accessUrl(thumbnailUrl.getPresignedUrl())
                    .fileSize(product.getThumbnailFileSize())
                    .contentType(product.getThumbnailContentType())
                    .width(product.getThumbnailWidth())
                    .height(product.getThumbnailHeight())
                    .build());
        }

        return builder.build();
    }

    public ProductVariantInfo mapProductToVariantInfo(Product variant) {
        ProductVariantInfo.ProductVariantInfoBuilder builder = ProductVariantInfo.builder()
                .variantId(variant.getId().toString())
                .name(variant.getName())
                .slug(variant.getSlug())
                .sku(variant.getSku())
                .modelNumber(variant.getModelNumber())
                .price(variant.getPrice())
                .compareAtPrice(variant.getCompareAtPrice())
                .isActive(variant.getIsActive())
                .variantPosition(variant.getVariantPosition())
                .specifications(variant.getSpecifications());

        if (variant.getCompatibilityBrands() != null && !variant.getCompatibilityBrands().isEmpty()) {
            List<CompatibilityBrandInfo> compatibilityBrandInfos = variant.getCompatibilityBrands().stream()
                    .map(brand -> CompatibilityBrandInfo.builder()
                            .compatibilityBrandId(brand.getId().toString())
                            .name(brand.getName())
                            .slug(brand.getSlug())
                            .build())
                    .collect(Collectors.toList());
            builder.compatibilityBrands(compatibilityBrandInfos);
        }

        if (variant.getThumbnailObjectKey() != null) {
            PresignedUrlResponse thumbnailUrl = s3Service.generateReadPresignedUrl(variant.getThumbnailObjectKey(), 60);
            builder.thumbnailInfo(ThumbnailInfo.builder()
                    .objectKey(variant.getThumbnailObjectKey())
                    .accessUrl(thumbnailUrl.getPresignedUrl())
                    .fileSize(variant.getThumbnailFileSize())
                    .contentType(variant.getThumbnailContentType())
                    .width(variant.getThumbnailWidth())
                    .height(variant.getThumbnailHeight())
                    .build());
        }

        List<ProductImageInfo> imageInfos = variant.getProductImages().stream()
                .map(this::mapProductImageToInfo)
                .collect(Collectors.toList());
        builder.images(imageInfos);

        return builder.build();
    }

    public ProductImageInfo mapProductImageToInfo(ProductImage productImage) {
        if (!s3Service.imageExists(productImage.getObjectKey())) {
            log.warn("Image not found in S3, skipping: {}", productImage.getObjectKey());
            return null;
        }

        PresignedUrlResponse imageUrl = s3Service.generateReadPresignedUrl(productImage.getObjectKey(), 60);
        return ProductImageInfo.builder()
                .imageId(productImage.getId().toString())
                .objectKey(productImage.getObjectKey())
                .accessUrl(imageUrl.getPresignedUrl())
                .fileSize(productImage.getFileSize())
                .contentType(productImage.getContentType())
                .width(productImage.getWidth())
                .height(productImage.getHeight())
                .altText(productImage.getAltText())
                .displayOrder(productImage.getDisplayOrder())
                .isPrimary(productImage.getIsPrimary())
                .build();
    }
}