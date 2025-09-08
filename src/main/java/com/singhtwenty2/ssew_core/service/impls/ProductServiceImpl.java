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
import com.singhtwenty2.ssew_core.service.file_handeling.ImageProcessingService;
import com.singhtwenty2.ssew_core.service.file_handeling.S3Service;
import com.singhtwenty2.ssew_core.util.sanitizer.SpecificationSanitizer;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
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
    private final SpecificationSanitizer specificationSanitizer;
    private final SlugGenerator slugGenerator;

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

        validateProductData(request, null);

        Product product = new Product();
        mapCreateRequestToProduct(request, product, manufacturer, parentProduct);

        String sku = generateUniqueSku(manufacturer, parentProduct, request.getName());
        product.setSku(sku);
        product.setSlug(generateUniqueSlug(request.getName()));

        if (request.getCompatibilityBrandIds() != null && !request.getCompatibilityBrandIds().isEmpty()) {
            List<CompatibilityBrand> compatibilityBrands = compatibilityBrandRepository
                    .findAllById(request.getCompatibilityBrandIds().stream()
                            .map(UUID::fromString)
                            .collect(Collectors.toList()));

            if (compatibilityBrands.size() != request.getCompatibilityBrandIds().size()) {
                throw new ResourceNotFoundException("One or more compatibility brands not found");
            }

            compatibilityBrands.forEach(product::addCompatibilityBrand);
        }

        product = productRepository.save(product);

        if (parentProduct != null) {
            parentProduct.addVariant(product);
            productRepository.save(parentProduct);
            inheritParentImages(parentProduct, product);
            product = productRepository.save(product);
        }

        log.info("Product created successfully with ID: {}", product.getId());
        return mapProductToResponse(product, false);
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
        mapVariantRequestToProduct(request, variant, manufacturer);

        String sku = generateUniqueSku(manufacturer, parentProduct, request.getName());
        variant.setSku(sku);
        variant.setSlug(generateUniqueSlug(request.getName()));

        variant.setParentProduct(parentProduct);
        variant.setVariantType(VariantType.VARIANT);

        if (request.getCompatibilityBrandIds() != null && !request.getCompatibilityBrandIds().isEmpty()) {
            List<CompatibilityBrand> compatibilityBrands = compatibilityBrandRepository
                    .findAllById(request.getCompatibilityBrandIds().stream()
                            .map(UUID::fromString)
                            .collect(Collectors.toList()));

            if (compatibilityBrands.size() != request.getCompatibilityBrandIds().size()) {
                throw new ResourceNotFoundException("One or more compatibility brands not found");
            }

            compatibilityBrands.forEach(variant::addCompatibilityBrand);
        }

        variant = productRepository.save(variant);

        parentProduct.addVariant(variant);
        inheritParentImages(parentProduct, variant);

        productRepository.save(parentProduct);
        variant = productRepository.save(variant);

        log.info("Variant created successfully with ID: {}", variant.getId());
        return mapProductToResponse(variant, false);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(String productId) {
        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        return mapProductToResponse(product, true);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        return mapProductToResponse(product, true);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        return mapProductToResponse(product, true);
    }

    @Override
    public ProductResponse updateProduct(String productId, UpdateProductRequest request) {
        log.info("Updating product with ID: {}", productId);

        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        validateProductUpdateData(request, product);

        mapUpdateRequestToProduct(request, product);

        if (request.getManufacturerId() != null && !request.getManufacturerId().equals(product.getManufacturer().getId().toString())) {
            Manufacturer newManufacturer = manufacturerRepository.findById(UUID.fromString(request.getManufacturerId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found"));
            product.setManufacturer(newManufacturer);
        }

        if (request.getCompatibilityBrandIds() != null) {
            product.clearCompatibilityBrands();

            if (!request.getCompatibilityBrandIds().isEmpty()) {
                List<CompatibilityBrand> compatibilityBrands = compatibilityBrandRepository
                        .findAllById(request.getCompatibilityBrandIds().stream()
                                .map(UUID::fromString)
                                .collect(Collectors.toList()));

                if (compatibilityBrands.size() != request.getCompatibilityBrandIds().size()) {
                    throw new ResourceNotFoundException("One or more compatibility brands not found");
                }

                compatibilityBrands.forEach(product::addCompatibilityBrand);
            }
        }

        product = productRepository.save(product);
        log.info("Product updated successfully with ID: {}", productId);
        return mapProductToResponse(product, true);
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

        deleteProductOwnedImages(product);
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

        deleteVariantSpecificImages(variant);

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
        Specification<Product> spec = buildProductSpecificationExcludingVariants(filters);
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductSummary> summaries = productPage.getContent().stream()
                .map(this::mapProductToSummary)
                .collect(Collectors.toList());

        return new PageImpl<>(summaries, pageable, productPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummary> getAllProductsIncludingVariants(ProductSearchFilters filters, Pageable pageable) {
        Specification<Product> spec = buildProductSpecification(filters);
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return productPage.map(this::mapProductToSummary);
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
                .map(this::mapProductToVariantInfo)
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

        if (product.getThumbnailObjectKey() != null && isImageOwnedByProduct(product.getThumbnailObjectKey(), product)) {
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

        if (isImageOwnedByProduct(productImage.getObjectKey(), productImage.getProduct())) {
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
            if (isImageOwnedByProduct(objectKey, product)) {
                s3Service.deleteImage(objectKey);
            }

            product.setThumbnailObjectKey(null);
            product.setThumbnailFileSize(null);
            product.setThumbnailContentType(null);
            product.setThumbnailWidth(null);
            product.setThumbnailHeight(null);

            if (product.hasVariants()) {
                clearThumbnailFromVariants(product, objectKey);
            }

            productRepository.save(product);
            log.info("Product thumbnail deleted and cleared from variants for product: {}", productId);
            return;
        }

        ProductImage productImage = productImageRepository
                .findByObjectKeyAndProductId(objectKey, UUID.fromString(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found"));

        if (isImageOwnedByProduct(objectKey, product)) {
            s3Service.deleteImage(objectKey);
        }
        productImageRepository.delete(productImage);

        if (product.hasVariants()) {
            removeImageFromVariants(product, objectKey);
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

    private boolean isImageOwnedByProduct(String objectKey, Product product) {
        String productIdFromKey = extractProductIdFromObjectKey(objectKey);
        return productIdFromKey != null && productIdFromKey.equals(product.getId().toString());
    }

    private String extractProductIdFromObjectKey(String objectKey) {
        if (objectKey == null || !objectKey.startsWith("products/")) {
            return null;
        }
        String[] parts = objectKey.split("/");
        if (parts.length >= 2) {
            return parts[1];
        }
        return null;
    }

    private void deleteProductOwnedImages(Product product) {
        List<String> objectKeys = new ArrayList<>();

        if (product.getThumbnailObjectKey() != null && isImageOwnedByProduct(product.getThumbnailObjectKey(), product)) {
            objectKeys.add(product.getThumbnailObjectKey());
        }

        objectKeys.addAll(product.getProductImages().stream()
                .map(ProductImage::getObjectKey)
                .filter(key -> isImageOwnedByProduct(key, product))
                .toList());

        if (!objectKeys.isEmpty()) {
            s3Service.deleteImages(objectKeys);
        }
    }

    private void deleteVariantSpecificImages(Product variant) {
        List<String> objectKeysToDelete = new ArrayList<>();

        if (variant.getThumbnailObjectKey() != null && isImageOwnedByProduct(variant.getThumbnailObjectKey(), variant)) {
            objectKeysToDelete.add(variant.getThumbnailObjectKey());
        }

        objectKeysToDelete.addAll(variant.getProductImages().stream()
                .map(ProductImage::getObjectKey)
                .filter(key -> isImageOwnedByProduct(key, variant))
                .toList());

        if (!objectKeysToDelete.isEmpty()) {
            s3Service.deleteImages(objectKeysToDelete);
        }
    }

    private String generateUniqueSku(Manufacturer manufacturer, Product parentProduct, String productName) {
        String baseSku;

        if (parentProduct != null) {
            String parentBaseSku = extractBaseSku(parentProduct.getSku());
            baseSku = parentBaseSku + "-V";
        } else {
            String categoryCode = manufacturer.getCategories() != null && !manufacturer.getCategories().isEmpty() ?
                    cleanAndTruncate(manufacturer.getCategories().get(0).getName()).toUpperCase() : "GEN";
            String manufacturerCode = cleanAndTruncate(manufacturer.getName()).toUpperCase();
            String productCode = cleanAndTruncate(productName).toUpperCase();
            baseSku = String.format("SSEW-%s-%s-%s", categoryCode, manufacturerCode, productCode);
        }

        String uniqueSku;
        int attempts = 0;
        do {
            String suffix = generateRandomSuffix();
            uniqueSku = baseSku + "-" + suffix;
            attempts++;
            if (attempts > 10) {
                uniqueSku = baseSku + "-" + System.currentTimeMillis();
                break;
            }
        } while (productRepository.existsBySku(uniqueSku));

        return uniqueSku;
    }

    private String extractBaseSku(String sku) {
        int lastDashIndex = sku.lastIndexOf("-");
        return lastDashIndex > 0 ? sku.substring(0, lastDashIndex) : sku;
    }

    private String generateRandomSuffix() {
        return String.format("%04X", ThreadLocalRandom.current().nextInt(0x10000));
    }

    private String cleanAndTruncate(String input) {
        if (input == null) return "UNK";
        String cleaned = input.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        return cleaned.length() > 3 ? cleaned.substring(0, 3) : cleaned;
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

    private void inheritParentImages(Product parentProduct, Product variant) {
        log.info("Inheriting images from parent {} to variant {}",
                parentProduct.getId(), variant.getId());

        if (parentProduct.getThumbnailObjectKey() != null &&
            s3Service.imageExists(parentProduct.getThumbnailObjectKey())) {

            variant.setThumbnailObjectKey(parentProduct.getThumbnailObjectKey());
            variant.setThumbnailFileSize(parentProduct.getThumbnailFileSize());
            variant.setThumbnailContentType(parentProduct.getThumbnailContentType());
            variant.setThumbnailWidth(parentProduct.getThumbnailWidth());
            variant.setThumbnailHeight(parentProduct.getThumbnailHeight());
            log.info("Inherited thumbnail: {}", parentProduct.getThumbnailObjectKey());
        }

        List<ProductImage> parentImages = productImageRepository
                .findByProductIdOrderByDisplayOrderAsc(parentProduct.getId());

        if (!parentImages.isEmpty()) {
            List<ProductImage> variantImages = parentImages.stream()
                    .filter(img -> s3Service.imageExists(img.getObjectKey()))
                    .map(parentImage -> {
                        ProductImage variantImage = new ProductImage();
                        variantImage.setObjectKey(parentImage.getObjectKey());
                        variantImage.setFileSize(parentImage.getFileSize());
                        variantImage.setContentType(parentImage.getContentType());
                        variantImage.setWidth(parentImage.getWidth());
                        variantImage.setHeight(parentImage.getHeight());
                        variantImage.setAltText(parentImage.getAltText());
                        variantImage.setDisplayOrder(parentImage.getDisplayOrder());
                        variantImage.setIsPrimary(parentImage.getIsPrimary());
                        variantImage.setProduct(variant);
                        return variantImage;
                    })
                    .collect(Collectors.toList());

            if (!variantImages.isEmpty()) {
                productImageRepository.saveAll(variantImages);
                log.info("Inherited {} catalog images for variant {}",
                        variantImages.size(), variant.getId());
            }
        }
    }

    private void validateProductData(CreateProductRequest request, Product existingProduct) {
        if (request.getName() != null && (existingProduct == null || !request.getName().equals(existingProduct.getName()))) {
            if (productRepository.existsByName(request.getName())) {
                throw new BusinessException("Product with this name already exists");
            }
        }

        if (request.getModelNumber() != null && (existingProduct == null || !request.getModelNumber().equals(existingProduct.getModelNumber()))) {
            if (productRepository.existsByModelNumber(request.getModelNumber())) {
                throw new BusinessException("Product with this model number already exists");
            }
        }
    }

    private void validateProductUpdateData(UpdateProductRequest request, Product existingProduct) {
        if (request.getName() != null && !request.getName().equals(existingProduct.getName())) {
            if (productRepository.existsByName(request.getName())) {
                throw new BusinessException("Product with this name already exists");
            }
        }

        if (request.getModelNumber() != null && !request.getModelNumber().equals(existingProduct.getModelNumber())) {
            if (productRepository.existsByModelNumber(request.getModelNumber())) {
                throw new BusinessException("Product with this model number already exists");
            }
        }
    }

    private Specification<Product> buildProductSpecification(ProductSearchFilters filters) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (filters.getKeyword() != null && !filters.getKeyword().trim().isEmpty()) {
                String keyword = "%" + filters.getKeyword().toLowerCase() + "%";
                jakarta.persistence.criteria.Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), keyword);
                jakarta.persistence.criteria.Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), keyword);
                jakarta.persistence.criteria.Predicate skuPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("sku")), keyword);

                predicates.add(criteriaBuilder.or(namePredicate, descriptionPredicate, skuPredicate));
            }

            if (filters.getCategoryId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.join("manufacturer").join("categories").get("id"), UUID.fromString(filters.getCategoryId())));
            }

            if (filters.getManufacturerId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("manufacturer").get("id"), UUID.fromString(filters.getManufacturerId())));
            }

            if (filters.getCompatibilityBrandIds() != null && !filters.getCompatibilityBrandIds().isEmpty()) {
                List<UUID> compatibilityBrandUuids = filters.getCompatibilityBrandIds().stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toList());
                predicates.add(root.join("compatibilityBrands").get("id").in(compatibilityBrandUuids));
            }

            if (filters.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), filters.getMinPrice()));
            }

            if (filters.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), filters.getMaxPrice()));
            }

            if (filters.getIsFeatured() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isFeatured"), filters.getIsFeatured()));
            }

            predicates.add(criteriaBuilder.isTrue(root.get("isActive")));

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private Specification<Product> buildProductSpecificationExcludingVariants(ProductSearchFilters filters) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.notEqual(root.get("variantType"), VariantType.VARIANT));

            if (filters.getKeyword() != null && !filters.getKeyword().trim().isEmpty()) {
                String keyword = "%" + filters.getKeyword().toLowerCase() + "%";
                jakarta.persistence.criteria.Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), keyword);
                jakarta.persistence.criteria.Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), keyword);
                jakarta.persistence.criteria.Predicate skuPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("sku")), keyword);

                predicates.add(criteriaBuilder.or(namePredicate, descriptionPredicate, skuPredicate));
            }

            if (filters.getCategoryId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.join("manufacturer").join("categories").get("id"), UUID.fromString(filters.getCategoryId())));
            }

            if (filters.getManufacturerId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("manufacturer").get("id"), UUID.fromString(filters.getManufacturerId())));
            }

            if (filters.getCompatibilityBrandIds() != null && !filters.getCompatibilityBrandIds().isEmpty()) {
                List<UUID> compatibilityBrandUuids = filters.getCompatibilityBrandIds().stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toList());
                predicates.add(root.join("compatibilityBrands").get("id").in(compatibilityBrandUuids));
            }

            if (filters.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), filters.getMinPrice()));
            }

            if (filters.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), filters.getMaxPrice()));
            }

            if (filters.getIsFeatured() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isFeatured"), filters.getIsFeatured()));
            }

            predicates.add(criteriaBuilder.isTrue(root.get("isActive")));

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private void mapCreateRequestToProduct(CreateProductRequest request, Product product, Manufacturer manufacturer, Product parentProduct) {
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

    private void mapVariantRequestToProduct(CreateVariantRequest request, Product variant, Manufacturer manufacturer) {
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

    private void mapUpdateRequestToProduct(UpdateProductRequest request, Product product) {
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

    private ProductResponse mapProductToResponse(Product product, boolean includeVariants) {
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

    private ProductSummary mapProductToSummary(Product product) {
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

    private ProductVariantInfo mapProductToVariantInfo(Product variant) {
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

    private ProductImageInfo mapProductImageToInfo(ProductImage productImage) {
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

    private void clearThumbnailFromVariants(Product parentProduct, String objectKey) {
        List<Product> variants = parentProduct.getVariants();
        for (Product variant : variants) {
            if (objectKey.equals(variant.getThumbnailObjectKey())) {
                variant.setThumbnailObjectKey(null);
                variant.setThumbnailFileSize(null);
                variant.setThumbnailContentType(null);
                variant.setThumbnailWidth(null);
                variant.setThumbnailHeight(null);
            }
        }
        productRepository.saveAll(variants);
    }

    private void removeImageFromVariants(Product parentProduct, String objectKey) {
        List<Product> variants = parentProduct.getVariants();
        for (Product variant : variants) {
            Optional<ProductImage> variantImage = productImageRepository
                    .findByObjectKeyAndProductId(objectKey, variant.getId());
            variantImage.ifPresent(productImageRepository::delete);
        }
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