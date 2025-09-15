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
package com.singhtwenty2.ssew_core.service.impls;

import com.singhtwenty2.ssew_core.data.entity.CompatibilityBrand;
import com.singhtwenty2.ssew_core.data.entity.Manufacturer;
import com.singhtwenty2.ssew_core.data.entity.Product;
import com.singhtwenty2.ssew_core.data.entity.ProductImage;
import com.singhtwenty2.ssew_core.data.enums.VariantType;
import com.singhtwenty2.ssew_core.data.repository.CompatibilityBrandRepository;
import com.singhtwenty2.ssew_core.data.repository.ManufacturerRepository;
import com.singhtwenty2.ssew_core.data.repository.ProductImageRepository;
import com.singhtwenty2.ssew_core.data.repository.ProductRepository;
import com.singhtwenty2.ssew_core.exception.BusinessException;
import com.singhtwenty2.ssew_core.exception.ResourceNotFoundException;
import com.singhtwenty2.ssew_core.service.catalogue.ProductService;
import com.singhtwenty2.ssew_core.service.catalogue.helper.*;
import com.singhtwenty2.ssew_core.service.file_handeling.ImageProcessingService;
import com.singhtwenty2.ssew_core.service.file_handeling.S3Service;
import com.singhtwenty2.ssew_core.util.slug.SlugGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.singhtwenty2.ssew_core.data.dto.catalogue.ImageDTO.ImageUploadResult;
import static com.singhtwenty2.ssew_core.data.dto.catalogue.ImageDTO.ProcessedImageResult;
import static com.singhtwenty2.ssew_core.data.dto.catalogue.PreSignedUrlDTO.PresignedUrlResponse;
import static com.singhtwenty2.ssew_core.data.dto.catalogue.ProductDTO.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final CompatibilityBrandRepository compatibilityBrandRepository;
    private final ProductImageRepository productImageRepository;
    private final S3Service s3Service;
    private final ImageProcessingService imageProcessingService;
    private final SlugGenerator slugGenerator;

    private final ProductValidationService validationService;
    private final ProductMappingService mappingService;
    private final SkuGeneratorService skuGeneratorService;
    private final ProductSearchService searchService;
    private final ProductImageService imageService;

    @Override
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product with name: {}", request.getName());

        Manufacturer manufacturer = manufacturerRepository.findById(UUID.fromString(request.getManufacturerId()))
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found with ID: " + request.getManufacturerId()));

        Product parentProduct = null;
        if (request.getParentProductId() != null) {
            parentProduct = productRepository.findById(UUID.fromString(request.getParentProductId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Parent product not found"));

            if (!parentProduct.getVariantType().equals(VariantType.PARENT) &&
                !parentProduct.getVariantType().equals(VariantType.STANDALONE)) {
                throw new BusinessException("Cannot create variant for a variant product");
            }
        }

        validationService.validateProductData(request, null);

        Product product = new Product();
        mappingService.mapCreateRequestToProduct(request, product, manufacturer, parentProduct);

        String sku = skuGeneratorService.generateUniqueSku(manufacturer, parentProduct, request.getName());
        product.setSku(sku);
        product.setSlug(generateUniqueSlug(request.getName()));

        if (request.getCompatibilityBrandIds() != null && !request.getCompatibilityBrandIds().isEmpty()) {
            attachCompatibilityBrands(product, request.getCompatibilityBrandIds());
        }

        product = productRepository.save(product);

        if (parentProduct != null) {
            parentProduct.addVariant(product);
            productRepository.save(parentProduct);
            imageService.inheritParentImages(parentProduct, product);
            product = productRepository.save(product);
        }

        log.info("Product created successfully with ID: {}", product.getId());
        return mappingService.mapProductToResponse(product, false);
    }

    @Override
    public ProductResponse createVariant(String parentProductId, CreateVariantRequest request) {
        log.info("Creating variant for parent product: {}", parentProductId);

        Product parentProduct = productRepository.findById(UUID.fromString(parentProductId))
                .orElseThrow(() -> new ResourceNotFoundException("Parent product not found"));

        if (parentProduct.getVariantType().equals(VariantType.VARIANT)) {
            throw new BusinessException("Cannot create variant for a variant product. Variants can only be created for parent or standalone products.");
        }

        Manufacturer manufacturer = parentProduct.getManufacturer();
        Product variant = new Product();
        mappingService.mapVariantRequestToProduct(request, variant, manufacturer);

        String sku = skuGeneratorService.generateUniqueSku(manufacturer, parentProduct, request.getName());
        variant.setSku(sku);
        variant.setSlug(generateUniqueSlug(request.getName()));
        variant.setParentProduct(parentProduct);
        variant.setVariantType(VariantType.VARIANT);

        if (request.getCompatibilityBrandIds() != null && !request.getCompatibilityBrandIds().isEmpty()) {
            attachCompatibilityBrands(variant, request.getCompatibilityBrandIds());
        }

        variant = productRepository.save(variant);
        parentProduct.addVariant(variant);
        imageService.inheritParentImages(parentProduct, variant);
        productRepository.save(parentProduct);
        variant = productRepository.save(variant);

        log.info("Variant created successfully with ID: {}", variant.getId());
        return mappingService.mapProductToResponse(variant, false);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(String productId) {
        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        return mappingService.mapProductToResponse(product, true);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        return mappingService.mapProductToResponse(product, true);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        return mappingService.mapProductToResponse(product, true);
    }

    @Override
    public ProductResponse updateProduct(String productId, UpdateProductRequest request) {
        log.info("Updating product with ID: {}", productId);

        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        validationService.validateProductUpdateData(request, product);
        mappingService.mapUpdateRequestToProduct(request, product);

        if (request.getManufacturerId() != null && !request.getManufacturerId().equals(product.getManufacturer().getId().toString())) {
            Manufacturer newManufacturer = manufacturerRepository.findById(UUID.fromString(request.getManufacturerId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found"));
            product.setManufacturer(newManufacturer);
        }

        if (request.getCompatibilityBrandIds() != null) {
            product.clearCompatibilityBrands();
            if (!request.getCompatibilityBrandIds().isEmpty()) {
                attachCompatibilityBrands(product, request.getCompatibilityBrandIds());
            }
        }

        product = productRepository.save(product);
        log.info("Product updated successfully with ID: {}", productId);
        return mappingService.mapProductToResponse(product, true);
    }

    @Override
    public void deleteProduct(String productId) {
        log.info("Deleting product with ID: {}", productId);

        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (product.getVariantType().equals(VariantType.VARIANT)) {
            throw new BusinessException("Use the variant deletion endpoint to delete variants");
        }

        if (product.hasVariants()) {
            throw new BusinessException("Cannot delete product with variants. Delete variants first.");
        }

        imageService.deleteProductOwnedImages(product);
        productRepository.delete(product);
        log.info("Product deleted successfully with ID: {}", productId);
    }

    @Override
    public void deleteVariant(String variantId) {
        log.info("Deleting variant with ID: {}", variantId);

        Product variant = productRepository.findById(UUID.fromString(variantId))
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with ID: " + variantId));

        if (!variant.getVariantType().equals(VariantType.VARIANT)) {
            throw new BusinessException("Product is not a variant. Use the product deletion endpoint for non-variants.");
        }

        Product parentProduct = variant.getParentProduct();
        imageService.deleteVariantSpecificImages(variant);
        productRepository.delete(variant);

        if (parentProduct != null) {
            parentProduct.removeVariant(variant);
            productRepository.save(parentProduct);
        }

        log.info("Variant deleted successfully with ID: {}", variantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummary> getAllProducts(ProductSearchFilters filters, Pageable pageable) {
        Specification<Product> spec = searchService.buildProductSpecificationExcludingVariants(filters);
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductSummary> summaries = productPage.getContent().stream()
                .map(mappingService::mapProductToSummary)
                .collect(Collectors.toList());

        return new PageImpl<>(summaries, pageable, productPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummary> getAllProductsIncludingVariants(ProductSearchFilters filters, Pageable pageable) {
        Specification<Product> spec = searchService.buildProductSpecification(filters);
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return productPage.map(mappingService::mapProductToSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantInfo> getProductVariants(String productId) {
        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (!product.hasVariants()) {
            return Collections.emptyList();
        }

        return product.getVariants().stream()
                .map(mappingService::mapProductToVariantInfo)
                .collect(Collectors.toList());
    }

    @Override
    public String uploadProductThumbnail(String productId, MultipartFile file) {
        log.info("Uploading thumbnail for product: {}", productId);

        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        ImageProcessingService.ValidationResult validation = imageProcessingService.validateImage(file, "product_thumbnail");
        if (!validation.isValid()) {
            throw new BusinessException("Invalid thumbnail image: " + validation.getErrorMessage());
        }

        if (product.getThumbnailObjectKey() != null && imageService.isImageOwnedByProduct(product.getThumbnailObjectKey(), product)) {
            s3Service.deleteImage(product.getThumbnailObjectKey());
        }

        ProcessedImageResult processedImage = imageProcessingService.processProductImage(file, true);
        ImageUploadResult uploadResult = s3Service.uploadProductImage(processedImage, productId, true);

        product.setThumbnailObjectKey(uploadResult.getObjectKey());
        product.setThumbnailFileSize(uploadResult.getFileSize());
        product.setThumbnailContentType(uploadResult.getContentType());
        product.setThumbnailWidth(0);
        product.setThumbnailHeight(0);

        productRepository.save(product);
        log.info("Thumbnail uploaded successfully for product: {}", productId);
        return uploadResult.getObjectKey();
    }

    @Override
    public List<String> uploadProductImages(String productId, List<MultipartFile> files) {
        log.info("Uploading {} images for product: {}", files.size(), productId);

        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (files.size() > 5) {
            throw new BusinessException("Maximum 5 catalog images allowed per product");
        }

        List<ProcessedImageResult> processedImages = new ArrayList<>();
        for (MultipartFile file : files) {
            ImageProcessingService.ValidationResult validation = imageProcessingService.validateImage(file, "product_image");
            if (!validation.isValid()) {
                throw new BusinessException("Invalid image: " + validation.getErrorMessage());
            }
            processedImages.add(imageProcessingService.processProductImage(file, false));
        }

        List<ImageUploadResult> uploadResults = s3Service.uploadProductImages(processedImages, productId);

        List<ProductImage> productImages = uploadResults.stream()
                .map(result -> {
                    ProductImage productImage = new ProductImage();
                    productImage.setObjectKey(result.getObjectKey());
                    productImage.setFileSize(result.getFileSize());
                    productImage.setContentType(result.getContentType());
                    productImage.setWidth(0);
                    productImage.setHeight(0);
                    productImage.setProduct(product);
                    return productImage;
                })
                .collect(Collectors.toList());

        productImageRepository.saveAll(productImages);

        List<String> objectKeys = uploadResults.stream()
                .map(ImageUploadResult::getObjectKey)
                .collect(Collectors.toList());

        log.info("Successfully uploaded {} images for product: {}", objectKeys.size(), productId);
        return objectKeys;
    }

    @Override
    public void deleteProductImage(String productId, String imageId) {
        ProductImage productImage = productImageRepository.findByIdAndProductId(
                        UUID.fromString(imageId), UUID.fromString(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found"));

        if (imageService.isImageOwnedByProduct(productImage.getObjectKey(), productImage.getProduct())) {
            s3Service.deleteImage(productImage.getObjectKey());
        }
        productImageRepository.delete(productImage);
        log.info("Product image deleted successfully: {}", imageId);
    }

    @Override
    public void deleteProductImageByObjectKey(String productId, String objectKey) {
        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (objectKey.equals(product.getThumbnailObjectKey())) {
            if (imageService.isImageOwnedByProduct(objectKey, product)) {
                s3Service.deleteImage(objectKey);
            }

            product.setThumbnailObjectKey(null);
            product.setThumbnailFileSize(null);
            product.setThumbnailContentType(null);
            product.setThumbnailWidth(null);
            product.setThumbnailHeight(null);

            if (product.hasVariants()) {
                imageService.clearThumbnailFromVariants(product, objectKey);
            }

            productRepository.save(product);
            log.info("Product thumbnail deleted and cleared from variants for product: {}", productId);
            return;
        }

        ProductImage productImage = productImageRepository
                .findByObjectKeyAndProductId(objectKey, UUID.fromString(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found"));

        if (imageService.isImageOwnedByProduct(objectKey, product)) {
            s3Service.deleteImage(objectKey);
        }
        productImageRepository.delete(productImage);

        if (product.hasVariants()) {
            imageService.removeImageFromVariants(product, objectKey);
        }

        log.info("Product catalog image deleted and cleared from variants: {}", objectKey);
    }

    @Override
    @Transactional(readOnly = true)
    public PresignedUrlResponse getProductImageUrl(String objectKey) {
        if (!s3Service.imageExists(objectKey)) {
            throw new ResourceNotFoundException("Image not found: " + objectKey);
        }
        return s3Service.generateReadPresignedUrl(objectKey, 60);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductStatsResponse getProductStats() {
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.countByIsActiveTrue();
        long featuredProducts = productRepository.countByIsFeaturedTrue();
        long productsWithVariants = productRepository.countByVariantType(VariantType.PARENT);

        BigDecimal averagePrice = productRepository.findAveragePrice();
        BigDecimal totalInventoryValue = productRepository.findTotalInventoryValue();

        return ProductStatsResponse.builder()
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .featuredProducts(featuredProducts)
                .productsWithVariants(productsWithVariants)
                .averagePrice(averagePrice)
                .totalInventoryValue(totalInventoryValue)
                .build();
    }

    private void attachCompatibilityBrands(Product product, List<String> compatibilityBrandIds) {
        List<CompatibilityBrand> compatibilityBrands = compatibilityBrandRepository
                .findAllById(compatibilityBrandIds.stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toList()));

        if (compatibilityBrands.size() != compatibilityBrandIds.size()) {
            throw new ResourceNotFoundException("One or more compatibility brands not found");
        }

        compatibilityBrands.forEach(product::addCompatibilityBrand);
    }

    private String generateUniqueSlug(String name) {
        String baseSlug = slugGenerator.generateSlug(name);
        String uniqueSlug = baseSlug;
        int counter = 1;

        while (productRepository.existsBySlug(uniqueSlug)) {
            uniqueSlug = baseSlug + "-" + counter++;
        }

        return uniqueSlug;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOrphanedImageReferences() {
        log.info("Starting cleanup of orphaned image references");

        List<Product> productsWithThumbnails = productRepository.findProductsWithThumbnails();
        for (Product product : productsWithThumbnails) {
            if (!s3Service.imageExists(product.getThumbnailObjectKey())) {
                log.info("Cleaning orphaned thumbnail for product: {}", product.getId());
                product.setThumbnailObjectKey(null);
                product.setThumbnailFileSize(null);
                product.setThumbnailContentType(null);
                product.setThumbnailWidth(null);
                product.setThumbnailHeight(null);
            }
        }
        productRepository.saveAll(productsWithThumbnails);

        List<ProductImage> allImages = productImageRepository.findAll();
        List<ProductImage> orphanedImages = allImages.stream()
                .filter(img -> !s3Service.imageExists(img.getObjectKey()))
                .collect(Collectors.toList());

        if (!orphanedImages.isEmpty()) {
            log.info("Cleaning {} orphaned catalog images", orphanedImages.size());
            productImageRepository.deleteAll(orphanedImages);
        }

        log.info("Completed cleanup of orphaned image references");
    }
}