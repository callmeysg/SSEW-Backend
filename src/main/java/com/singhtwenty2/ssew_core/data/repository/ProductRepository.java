package com.singhtwenty2.ssew_core.data.repository;

import com.singhtwenty2.ssew_core.data.entity.Brand;
import com.singhtwenty2.ssew_core.data.entity.Product;
import com.singhtwenty2.ssew_core.data.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySku(String sku);

    boolean existsBySlug(String slug);

    boolean existsBySku(String sku);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    boolean existsBySkuAndIdNot(String sku, UUID id);

    List<Product> findByStatus(ProductStatus status);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    List<Product> findByIsFeaturedTrue();

    Page<Product> findByIsFeaturedTrue(Pageable pageable);

    List<Product> findByBrand(Brand brand);

    Page<Product> findByBrand(Brand brand, Pageable pageable);

    Page<Product> findByBrandAndStatus(Brand brand, ProductStatus status, Pageable pageable);

    List<Product> findByStatusAndIsFeaturedTrue(ProductStatus status);

    Page<Product> findByStatusAndIsFeaturedTrue(ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:sku IS NULL OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :sku, '%'))) AND " +
           "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:isFeatured IS NULL OR p.isFeatured = :isFeatured) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findProductsWithFilters(
            @Param("name") String name,
            @Param("sku") String sku,
            @Param("brandId") UUID brandId,
            @Param("status") ProductStatus status,
            @Param("isFeatured") Boolean isFeatured,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    @Query("SELECT COUNT(p) FROM Product p WHERE p.brand.id = :brandId")
    Long countProductsByBrandId(@Param("brandId") UUID brandId);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.minStockLevel AND p.trackInventory = true")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.stockQuantity = 0 AND p.trackInventory = true")
    List<Product> findOutOfStockProducts();
}