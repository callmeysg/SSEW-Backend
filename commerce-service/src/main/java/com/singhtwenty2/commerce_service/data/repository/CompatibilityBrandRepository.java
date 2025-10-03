package com.singhtwenty2.commerce_service.data.repository;

import com.singhtwenty2.commerce_service.data.entity.CompatibilityBrand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompatibilityBrandRepository extends JpaRepository<CompatibilityBrand, UUID> {

    Optional<CompatibilityBrand> findBySlug(String slug);

    Optional<CompatibilityBrand> findByName(String name);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByNameAndIdNot(String name, UUID id);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    @Query("SELECT cb FROM CompatibilityBrand cb WHERE " +
           "(:searchTerm IS NULL OR LOWER(cb.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<CompatibilityBrand> findCompatibilityBrandsWithFilters(
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    @Query("SELECT COUNT(DISTINCT p) FROM Product p JOIN p.compatibilityBrands cb WHERE cb.id = :compatibilityBrandId")
    Long countProductsByCompatibilityBrandId(@Param("compatibilityBrandId") UUID compatibilityBrandId);

    @Query("SELECT cb FROM CompatibilityBrand cb JOIN FETCH cb.products WHERE cb.id = :compatibilityBrandId")
    Optional<CompatibilityBrand> findByIdWithProducts(@Param("compatibilityBrandId") UUID compatibilityBrandId);

    @Query("SELECT DISTINCT cb FROM CompatibilityBrand cb JOIN cb.products p WHERE p.id = :productId")
    List<CompatibilityBrand> findCompatibilityBrandsByProductId(@Param("productId") UUID productId);
}