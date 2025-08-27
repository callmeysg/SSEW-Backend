package com.singhtwenty2.ssew_core.data.repository;

import com.singhtwenty2.ssew_core.data.entity.Product;
import com.singhtwenty2.ssew_core.data.enums.VariantType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySku(String sku);

    boolean existsBySlug(String slug);

    boolean existsBySku(String sku);

    boolean existsByName(String name);

    boolean existsByModelNumber(String modelNumber);

    long countByIsActiveTrue();

    long countByIsFeaturedTrue();

    long countByVariantType(VariantType variantType);

    @Query("SELECT AVG(p.price) FROM Product p WHERE p.isActive = true")
    BigDecimal findAveragePrice();

    @Query("SELECT SUM(p.price) FROM Product p WHERE p.isActive = true")
    BigDecimal findTotalInventoryValue();

    @Query("SELECT p FROM Product p WHERE p.thumbnailObjectKey IS NOT NULL")
    List<Product> findProductsWithThumbnails();
}