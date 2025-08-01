package com.singhtwenty2.ssew_core.service.impls;

import com.singhtwenty2.ssew_core.data.entity.Brand;
import com.singhtwenty2.ssew_core.data.entity.Product;
import com.singhtwenty2.ssew_core.data.enums.ProductStatus;
import com.singhtwenty2.ssew_core.data.repository.BrandRepository;
import com.singhtwenty2.ssew_core.data.repository.ProductRepository;
import com.singhtwenty2.ssew_core.service.ProductService;
import com.singhtwenty2.ssew_core.service.SkuGenerationService;
import com.singhtwenty2.ssew_core.util.sanitizer.SpecificationSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.singhtwenty2.ssew_core.data.dto.catalog_management.ProductDTO.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final SkuGenerationService skuGenerationService;
    private final SpecificationSanitizer specificationSanitizer;

    @Override
    public ProductResponse createProduct(CreateProductRequest createProductRequest) {
        log.debug("Creating product with name: {}", createProductRequest.getName());

        validateCreateProductRequest(createProductRequest);

        Brand brand = findBrandById(createProductRequest.getBrandId());

        String slug = generateSlug(createProductRequest.getName());
        validateUniqueSlug(slug);

        String sku = skuGenerationService.generateUniqueSku(brand);

        Product product = createProductFromRequest(createProductRequest, brand, slug, sku);
        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with ID: {} and SKU: {}", savedProduct.getId(), savedProduct.getSku());

        return buildProductResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(String productId) {
        log.debug("Fetching product by ID: {}", productId);

        Product product = findProductById(productId);
        return buildProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug) {
        log.debug("Fetching product by slug: {}", slug);

        Optional<Product> productOptional = productRepository.findBySlug(slug);
        if (productOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with slug: " + slug);
        }

        return buildProductResponse(productOptional.get());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        log.debug("Fetching product by SKU: {}", sku);

        Optional<Product> productOptional = productRepository.findBySku(sku);
        if (productOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with SKU: " + sku);
        }

        return buildProductResponse(productOptional.get());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        log.debug("Fetching all products with pagination");

        Page<Product> products = productRepository.findAll(pageable);
        return products.map(this::buildProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByStatus(ProductStatus status, Pageable pageable) {
        log.debug("Fetching products by status: {}", status);

        Page<Product> products = productRepository.findByStatus(status, pageable);
        return products.map(this::buildProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getFeaturedProducts(Pageable pageable) {
        log.debug("Fetching featured products with pagination");

        Page<Product> products = productRepository.findByIsFeaturedTrue(pageable);
        return products.map(this::buildProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByBrand(String brandId, Pageable pageable) {
        log.debug("Fetching products by brand ID: {}", brandId);

        Brand brand = findBrandById(brandId);
        Page<Product> products = productRepository.findByBrand(brand, pageable);
        return products.map(this::buildProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByBrandAndStatus(String brandId, ProductStatus status, Pageable pageable) {
        log.debug("Fetching products by brand ID: {} and status: {}", brandId, status);

        Brand brand = findBrandById(brandId);
        Page<Product> products = productRepository.findByBrandAndStatus(brand, status, pageable);
        return products.map(this::buildProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getFeaturedProductsByStatus(ProductStatus status, Pageable pageable) {
        log.debug("Fetching featured products by status: {}", status);

        Page<Product> products = productRepository.findByStatusAndIsFeaturedTrue(status, pageable);
        return products.map(this::buildProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(
            String name,
            String sku,
            String brandId,
            ProductStatus status,
            Boolean isFeatured,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {
        log.debug("Searching products with filters");

        UUID brandUuid = null;
        if (StringUtils.hasText(brandId)) {
            try {
                brandUuid = UUID.fromString(brandId);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid brand ID format");
            }
        }

        Page<Product> products = productRepository.findProductsWithFilters(
                name, sku, brandUuid, status, isFeatured, minPrice, maxPrice, pageable);
        return products.map(this::buildProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts() {
        log.debug("Fetching low stock products");

        List<Product> products = productRepository.findLowStockProducts();
        return products.stream()
                .map(this::buildProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getOutOfStockProducts() {
        log.debug("Fetching out of stock products");

        List<Product> products = productRepository.findOutOfStockProducts();
        return products.stream()
                .map(this::buildProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse updateProduct(String productId, UpdateProductRequest updateProductRequest) {
        log.debug("Updating product with ID: {}", productId);

        Product existingProduct = findProductById(productId);
        updateProductFields(existingProduct, updateProductRequest);

        Product updatedProduct = productRepository.save(existingProduct);

        log.info("Product updated successfully with ID: {}", updatedProduct.getId());

        return buildProductResponse(updatedProduct);
    }

    @Override
    public ProductResponse updateProductInventory(String productId, ProductInventoryUpdateRequest inventoryUpdateRequest) {
        log.debug("Updating product inventory for ID: {}", productId);

        Product product = findProductById(productId);

        product.setStockQuantity(inventoryUpdateRequest.getStockQuantity());

        if (inventoryUpdateRequest.getMinStockLevel() != null) {
            product.setMinStockLevel(inventoryUpdateRequest.getMinStockLevel());
        }

        if (inventoryUpdateRequest.getTrackInventory() != null) {
            product.setTrackInventory(inventoryUpdateRequest.getTrackInventory());
        }

        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);

        log.info("Product inventory updated successfully for ID: {}", productId);

        return buildProductResponse(updatedProduct);
    }

    @Override
    public ProductResponse updateProductStatus(String productId, ProductStatus status) {
        log.debug("Updating product status for ID: {} to: {}", productId, status);

        Product product = findProductById(productId);
        product.setStatus(status);
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);

        log.info("Product status updated successfully for ID: {}, new status: {}", productId, status);

        return buildProductResponse(updatedProduct);
    }

    @Override
    public ProductResponse toggleFeaturedStatus(String productId) {
        log.debug("Toggling featured status for product ID: {}", productId);

        Product product = findProductById(productId);
        product.setIsFeatured(!product.getIsFeatured());
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);

        log.info("Product featured status toggled successfully for ID: {}, new status: {}", productId, product.getIsFeatured());

        return buildProductResponse(updatedProduct);
    }

    @Override
    public ProductResponse updateProductSpecifications(String productId, ProductSpecificationUpdateRequest specificationRequest) {
        log.debug("Updating product specifications for ID: {}", productId);

        Product product = findProductById(productId);

        Map<String, String> sanitizedSpecs = specificationSanitizer.sanitizeSpecifications(specificationRequest.getSpecifications());
        product.setSpecifications(sanitizedSpecs);
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);

        log.info("Product specifications updated successfully for ID: {}", productId);

        return buildProductResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(String productId) {
        log.debug("Deleting product with ID: {}", productId);

        Product product = findProductById(productId);

        productRepository.delete(product);

        log.info("Product deleted successfully with ID: {}", productId);
    }

    private void validateCreateProductRequest(CreateProductRequest request) {
        if (!StringUtils.hasText(request.getName()) || request.getName().trim().length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name must be at least 2 characters long");
        }

        if (!StringUtils.hasText(request.getBrandId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand ID is required");
        }

        if (request.getComparePrice() != null && request.getPrice().compareTo(request.getComparePrice()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price cannot be greater than compare price");
        }
    }

    private Brand findBrandById(String brandId) {
        try {
            UUID brandUuid = UUID.fromString(brandId);
            Optional<Brand> brandOptional = brandRepository.findById(brandUuid);

            if (brandOptional.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with ID: " + brandId);
            }

            return brandOptional.get();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid brand ID format");
        }
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .trim();
    }

    private void validateUniqueSlug(String slug) {
        if (productRepository.existsBySlug(slug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product with this slug already exists");
        }
    }

    private Product createProductFromRequest(CreateProductRequest request, Brand brand, String slug, String sku) {
        Product product = new Product();
        product.setName(request.getName().trim());
        product.setSlug(slug);
        product.setSku(sku);
        product.setDescription(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
        product.setShortDescription(StringUtils.hasText(request.getShortDescription()) ? request.getShortDescription().trim() : null);
        product.setPrice(request.getPrice());
        product.setComparePrice(request.getComparePrice());
        product.setCostPrice(request.getCostPrice());
        product.setWeight(request.getWeight());
        product.setDimensions(StringUtils.hasText(request.getDimensions()) ? request.getDimensions().trim() : null);
        product.setStockQuantity(request.getStockQuantity());
        product.setMinStockLevel(request.getMinStockLevel());
        product.setTrackInventory(request.getTrackInventory());
        product.setStatus(request.getStatus());
        product.setIsFeatured(request.getIsFeatured());
        product.setMetaTitle(StringUtils.hasText(request.getMetaTitle()) ? request.getMetaTitle().trim() : null);
        product.setMetaDescription(StringUtils.hasText(request.getMetaDescription()) ? request.getMetaDescription().trim() : null);
        product.setTags(StringUtils.hasText(request.getTags()) ? request.getTags().trim() : null);
        product.setBrand(brand);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        if (request.getSpecifications() != null && !request.getSpecifications().isEmpty()) {
            product.setSpecifications(specificationSanitizer.sanitizeSpecifications(request.getSpecifications()));
        }

        return product;
    }

    private ProductResponse buildProductResponse(Product product) {
        Long imageCount = (long) product.getImages().size();

        return ProductResponse.builder()
                .productId(product.getId().toString())
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .price(product.getPrice())
                .comparePrice(product.getComparePrice())
                .costPrice(product.getCostPrice())
                .weight(product.getWeight())
                .dimensions(product.getDimensions())
                .stockQuantity(product.getStockQuantity())
                .minStockLevel(product.getMinStockLevel())
                .trackInventory(product.getTrackInventory())
                .status(product.getStatus())
                .isFeatured(product.getIsFeatured())
                .metaTitle(product.getMetaTitle())
                .metaDescription(product.getMetaDescription())
                .tags(product.getTags())
                .createdAt(product.getCreatedAt().toString())
                .updatedAt(product.getUpdatedAt().toString())
                .brandId(product.getBrand().getId().toString())
                .brandName(product.getBrand().getName())
                .categoryId(product.getBrand().getCategory().getId().toString())
                .categoryName(product.getBrand().getCategory().getName())
                .imageCount(imageCount)
                .isInStock(product.isInStock())
                .isLowStock(product.isLowStock())
                .specifications(product.getSpecifications() != null ? new HashMap<>(product.getSpecifications()) : new HashMap<>())
                .build();
    }

    private Product findProductById(String productId) {
        try {
            UUID productUuid = UUID.fromString(productId);
            Optional<Product> productOptional = productRepository.findById(productUuid);

            if (productOptional.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + productId);
            }

            return productOptional.get();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid product ID format");
        }
    }

    private void updateProductFields(Product product, UpdateProductRequest request) {
        boolean updated = false;

        if (StringUtils.hasText(request.getName()) && !request.getName().equals(product.getName())) {
            product.setName(request.getName().trim());

            String newSlug = generateSlug(request.getName());
            if (!newSlug.equals(product.getSlug())) {
                validateUniqueSlugForUpdate(newSlug, product.getId());
                product.setSlug(newSlug);
            }
            updated = true;
        }

        if (request.getDescription() != null) {
            product.setDescription(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
            updated = true;
        }

        if (request.getShortDescription() != null) {
            product.setShortDescription(StringUtils.hasText(request.getShortDescription()) ? request.getShortDescription().trim() : null);
            updated = true;
        }

        if (request.getPrice() != null && !request.getPrice().equals(product.getPrice())) {
            if (request.getComparePrice() != null && request.getPrice().compareTo(request.getComparePrice()) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price cannot be greater than compare price");
            }
            product.setPrice(request.getPrice());
            updated = true;
        }

        if (request.getComparePrice() != null) {
            if (request.getComparePrice().compareTo(product.getPrice()) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Compare price cannot be less than current price");
            }
            product.setComparePrice(request.getComparePrice());
            updated = true;
        }

        if (request.getCostPrice() != null) {
            product.setCostPrice(request.getCostPrice());
            updated = true;
        }

        if (request.getWeight() != null) {
            product.setWeight(request.getWeight());
            updated = true;
        }

        if (request.getDimensions() != null) {
            product.setDimensions(StringUtils.hasText(request.getDimensions()) ? request.getDimensions().trim() : null);
            updated = true;
        }

        if (request.getStockQuantity() != null && !request.getStockQuantity().equals(product.getStockQuantity())) {
            product.setStockQuantity(request.getStockQuantity());
            updated = true;
        }

        if (request.getMinStockLevel() != null && !request.getMinStockLevel().equals(product.getMinStockLevel())) {
            product.setMinStockLevel(request.getMinStockLevel());
            updated = true;
        }

        if (request.getTrackInventory() != null && !request.getTrackInventory().equals(product.getTrackInventory())) {
            product.setTrackInventory(request.getTrackInventory());
            updated = true;
        }

        if (request.getStatus() != null && !request.getStatus().equals(product.getStatus())) {
            product.setStatus(request.getStatus());
            updated = true;
        }

        if (request.getIsFeatured() != null && !request.getIsFeatured().equals(product.getIsFeatured())) {
            product.setIsFeatured(request.getIsFeatured());
            updated = true;
        }

        if (request.getMetaTitle() != null) {
            product.setMetaTitle(StringUtils.hasText(request.getMetaTitle()) ? request.getMetaTitle().trim() : null);
            updated = true;
        }

        if (request.getMetaDescription() != null) {
            product.setMetaDescription(StringUtils.hasText(request.getMetaDescription()) ? request.getMetaDescription().trim() : null);
            updated = true;
        }

        if (request.getTags() != null) {
            product.setTags(StringUtils.hasText(request.getTags()) ? request.getTags().trim() : null);
            updated = true;
        }

        if (StringUtils.hasText(request.getBrandId()) &&
            !request.getBrandId().equals(product.getBrand().getId().toString())) {
            Brand newBrand = findBrandById(request.getBrandId());
            product.setBrand(newBrand);
            updated = true;
        }

        if (request.getSpecifications() != null) {
            Map<String, String> sanitizedSpecs = specificationSanitizer.sanitizeSpecifications(request.getSpecifications());
            product.setSpecifications(sanitizedSpecs);
            updated = true;
        }

        if (updated) {
            product.setUpdatedAt(LocalDateTime.now());
        }
    }

    private void validateUniqueSlugForUpdate(String slug, UUID productId) {
        if (productRepository.existsBySlugAndIdNot(slug, productId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product with this slug already exists");
        }
    }
}