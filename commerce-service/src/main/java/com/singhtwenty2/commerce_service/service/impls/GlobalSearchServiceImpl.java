/**
 * Copyright 2025 Aryan Singh
 * Developer: Aryan Singh (@singhtwenty2)
 * Portfolio: https://singhtwenty2.pages.dev/
 * This file is part of SSEW E-commerce Backend System
 * Licensed under MIT License
 * For commercial use and inquiries: aryansingh.corp@gmail.com
 * @author Aryan Singh (@singhtwenty2)
 * @project SSEW E-commerce Backend System
 * @since 2025
 */
package com.singhtwenty2.commerce_service.service.impls;

import com.singhtwenty2.commerce_service.data.dto.catalogue.PreSignedUrlDTO;
import com.singhtwenty2.commerce_service.data.dto.search.GlobalSearchDTO.*;
import com.singhtwenty2.commerce_service.data.entity.Category;
import com.singhtwenty2.commerce_service.data.entity.Manufacturer;
import com.singhtwenty2.commerce_service.data.entity.Product;
import com.singhtwenty2.commerce_service.data.enums.VariantType;
import com.singhtwenty2.commerce_service.data.repository.CategorySearchRepository;
import com.singhtwenty2.commerce_service.data.repository.ManufacturerSearchRepository;
import com.singhtwenty2.commerce_service.data.repository.ProductSearchRepository;
import com.singhtwenty2.commerce_service.service.file_handeling.S3Service;
import com.singhtwenty2.commerce_service.service.search.GlobalSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GlobalSearchServiceImpl implements GlobalSearchService {

    private final ProductSearchRepository productSearchRepository;
    private final ManufacturerSearchRepository manufacturerSearchRepository;
    private final CategorySearchRepository categorySearchRepository;
    private final S3Service s3Service;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String SEARCH_CACHE_PREFIX = "search:global:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(15);
    private static final int DEFAULT_PRODUCT_LIMIT = 20;
    private static final int DEFAULT_MANUFACTURER_LIMIT = 10;
    private static final int DEFAULT_CATEGORY_LIMIT = 10;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "globalSearch", key = "#searchTerm + ':' + #productLimit + ':' + #manufacturerLimit + ':' + #categoryLimit")
    public GlobalSearchResponse globalSearch(
            String searchTerm,
            Integer productLimit,
            Integer manufacturerLimit,
            Integer categoryLimit
    ) {
        long startTime = System.currentTimeMillis();

        if (!StringUtils.hasText(searchTerm)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search term is required");
        }

        String trimmedSearchTerm = searchTerm.trim();
        if (trimmedSearchTerm.length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search term must be at least 2 characters");
        }

        log.debug("Performing global search for term: {}", trimmedSearchTerm);

        int finalProductLimit = productLimit != null ? productLimit : DEFAULT_PRODUCT_LIMIT;
        int finalManufacturerLimit = manufacturerLimit != null ? manufacturerLimit : DEFAULT_MANUFACTURER_LIMIT;
        int finalCategoryLimit = categoryLimit != null ? categoryLimit : DEFAULT_CATEGORY_LIMIT;

        String cacheKey = SEARCH_CACHE_PREFIX + trimmedSearchTerm + ":" + finalProductLimit + ":" + finalManufacturerLimit + ":" + finalCategoryLimit;
        GlobalSearchResponse cachedResponse = (GlobalSearchResponse) redisTemplate.opsForValue().get(cacheKey);

        if (cachedResponse != null) {
            log.debug("Returning cached search results for term: {}", trimmedSearchTerm);
            cachedResponse.getMetadata().setFromCache(true);
            cachedResponse.getMetadata().setSearchTimeMs(System.currentTimeMillis() - startTime);
            return cachedResponse;
        }

        CompletableFuture<List<ProductSearchResult>> productsFuture = CompletableFuture.supplyAsync(() ->
                searchProductsInternal(trimmedSearchTerm, finalProductLimit)
        );

        CompletableFuture<List<ManufacturerSearchResult>> manufacturersFuture = CompletableFuture.supplyAsync(() ->
                searchManufacturersInternal(trimmedSearchTerm, finalManufacturerLimit)
        );

        CompletableFuture<List<CategorySearchResult>> categoriesFuture = CompletableFuture.supplyAsync(() ->
                searchCategoriesInternal(trimmedSearchTerm, finalCategoryLimit)
        );

        CompletableFuture.allOf(productsFuture, manufacturersFuture, categoriesFuture).join();

        List<ProductSearchResult> products = productsFuture.join();
        List<ManufacturerSearchResult> manufacturers = manufacturersFuture.join();
        List<CategorySearchResult> categories = categoriesFuture.join();

        long searchTime = System.currentTimeMillis() - startTime;

        SearchMetadata metadata = SearchMetadata.builder()
                .totalProducts(products.size())
                .totalManufacturers(manufacturers.size())
                .totalCategories(categories.size())
                .totalResults(products.size() + manufacturers.size() + categories.size())
                .searchTimeMs(searchTime)
                .searchTerm(trimmedSearchTerm)
                .fromCache(false)
                .build();

        GlobalSearchResponse response = GlobalSearchResponse.builder()
                .products(products)
                .manufacturers(manufacturers)
                .categories(categories)
                .metadata(metadata)
                .build();

        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL);

        log.info("Global search completed for term: {} in {}ms - Products: {}, Manufacturers: {}, Categories: {}",
                trimmedSearchTerm, searchTime, products.size(), manufacturers.size(), categories.size());

        return response;
    }

    @Override
    @CacheEvict(value = "globalSearch", allEntries = true)
    public void clearSearchCache() {
        log.info("Clearing all global search cache");
        redisTemplate.keys(SEARCH_CACHE_PREFIX + "*").forEach(redisTemplate::delete);
    }

    @Override
    @CacheEvict(value = "globalSearch", key = "#searchTerm + ':*'")
    public void clearSearchCacheForTerm(String searchTerm) {
        log.info("Clearing search cache for term: {}", searchTerm);
        String cacheKey = SEARCH_CACHE_PREFIX + searchTerm;
        redisTemplate.delete(cacheKey);
    }

    private List<ProductSearchResult> searchProductsInternal(String searchTerm, int limit) {
        try {
            List<Product> products = productSearchRepository.searchProducts(searchTerm);

            if (products.isEmpty()) {
                return new ArrayList<>();
            }

            List<UUID> productIds = products.stream()
                    .limit(limit)
                    .map(Product::getId)
                    .collect(Collectors.toList());

            List<Product> productsWithDetails = productSearchRepository.findByIdsWithManufacturerAndCategories(productIds);

            return productsWithDetails.stream()
                    .map(this::buildProductSearchResult)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching products: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private List<ManufacturerSearchResult> searchManufacturersInternal(String searchTerm, int limit) {
        try {
            List<Manufacturer> manufacturers = manufacturerSearchRepository.searchManufacturers(searchTerm);

            if (manufacturers.isEmpty()) {
                return new ArrayList<>();
            }

            List<UUID> manufacturerIds = manufacturers.stream()
                    .limit(limit)
                    .map(Manufacturer::getId)
                    .collect(Collectors.toList());

            List<Manufacturer> manufacturersWithCategories = manufacturerSearchRepository.findByIdsWithCategories(manufacturerIds);

            return manufacturersWithCategories.stream()
                    .map(this::buildManufacturerSearchResult)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching manufacturers: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private List<CategorySearchResult> searchCategoriesInternal(String searchTerm, int limit) {
        try {
            List<Category> categories = categorySearchRepository.searchCategories(searchTerm);

            return categories.stream()
                    .limit(limit)
                    .map(this::buildCategorySearchResult)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching categories: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private ProductSearchResult buildProductSearchResult(Product product) {
        String thumbnailUrl = null;
        if (StringUtils.hasText(product.getThumbnailObjectKey())) {
            try {
                PreSignedUrlDTO.PresignedUrlResponse presignedUrlResponse = s3Service.generateReadPresignedUrl(
                        product.getThumbnailObjectKey(),
                        60
                );
                if (presignedUrlResponse != null) {
                    thumbnailUrl = presignedUrlResponse.getPresignedUrl();
                }
            } catch (Exception e) {
                log.warn("Failed to generate thumbnail URL for product: {}", product.getId(), e);
            }
        }

        int variantCount = 0;
        if (product.getVariantType() == VariantType.PARENT) {
            variantCount = product.getVariants().size();
        }

        return ProductSearchResult.builder()
                .productId(product.getId().toString())
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .shortDescription(product.getShortDescription())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .thumbnailUrl(thumbnailUrl)
                .manufacturerName(product.getManufacturerName())
                .categoryNames(product.getCategoryNames())
                .isActive(product.getIsActive())
                .isFeatured(product.getIsFeatured())
                .variantType(product.getVariantType().name())
                .variantCount(variantCount)
                .build();
    }

    private ManufacturerSearchResult buildManufacturerSearchResult(Manufacturer manufacturer) {
        String logoUrl = null;
        if (StringUtils.hasText(manufacturer.getLogoObjectKey())) {
            try {
                PreSignedUrlDTO.PresignedUrlResponse presignedUrlResponse = s3Service.generateReadPresignedUrl(
                        manufacturer.getLogoObjectKey(),
                        60
                );
                if (presignedUrlResponse != null) {
                    logoUrl = presignedUrlResponse.getPresignedUrl();
                }
            } catch (Exception e) {
                log.warn("Failed to generate logo URL for manufacturer: {}", manufacturer.getId(), e);
            }
        }

        List<String> categoryNames = manufacturer.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.toList());

        Long productCount = manufacturerSearchRepository.countActiveProductsByManufacturerId(manufacturer.getId());

        return ManufacturerSearchResult.builder()
                .manufacturerId(manufacturer.getId().toString())
                .name(manufacturer.getName())
                .slug(manufacturer.getSlug())
                .description(manufacturer.getDescription())
                .logoUrl(logoUrl)
                .categoryNames(categoryNames)
                .productCount(productCount)
                .isActive(manufacturer.getIsActive())
                .build();
    }

    private CategorySearchResult buildCategorySearchResult(Category category) {
        Long manufacturerCount = categorySearchRepository.countActiveManufacturersByCategoryId(category.getId());

        return CategorySearchResult.builder()
                .categoryId(category.getId().toString())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .manufacturerCount(manufacturerCount)
                .isActive(category.getIsActive())
                .build();
    }
}