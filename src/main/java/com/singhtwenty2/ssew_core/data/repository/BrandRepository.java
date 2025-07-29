package com.singhtwenty2.ssew_core.data.repository;

import com.singhtwenty2.ssew_core.data.entity.Brand;
import com.singhtwenty2.ssew_core.data.entity.Category;
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
public interface BrandRepository extends JpaRepository<Brand, UUID> {

    Optional<Brand> findBySlug(String slug);

    Optional<Brand> findByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    boolean existsByNameAndIdNot(String name, UUID id);

    List<Brand> findByIsActiveTrue();

    Page<Brand> findByIsActiveTrue(Pageable pageable);

    List<Brand> findByCategoryAndIsActiveTrue(Category category);

    Page<Brand> findByCategory(Category category, Pageable pageable);

    Page<Brand> findByCategoryAndIsActiveTrue(Category category, Pageable pageable);

    @Query("SELECT b FROM Brand b WHERE " +
           "(:name IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:categoryId IS NULL OR b.category.id = :categoryId) AND " +
           "(:isActive IS NULL OR b.isActive = :isActive)")
    Page<Brand> findBrandsWithFilters(
            @Param("name") String name,
            @Param("categoryId") UUID categoryId,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    @Query("SELECT COUNT(p) FROM Product p WHERE p.brand.id = :brandId")
    Long countProductsByBrandId(@Param("brandId") UUID brandId);

    List<Brand> findByCategoryOrderByDisplayOrderAsc(Category category);

    @Query("SELECT MAX(b.displayOrder) FROM Brand b WHERE b.category = :category")
    Integer findMaxDisplayOrderByCategory(@Param("category") Category category);
}