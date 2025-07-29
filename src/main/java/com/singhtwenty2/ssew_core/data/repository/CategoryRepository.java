package com.singhtwenty2.ssew_core.data.repository;

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
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByNameAndIdNot(String name, UUID id);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    List<Category> findByIsActiveOrderByDisplayOrderAsc(Boolean isActive);

    Page<Category> findByIsActive(Boolean isActive, Pageable pageable);

    @Query("SELECT c FROM Category c WHERE " +
           "(:isActive IS NULL OR c.isActive = :isActive) AND " +
           "(:searchTerm IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Category> findCategoriesWithFilters(
            @Param("isActive") Boolean isActive,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    @Query("SELECT COUNT(b) FROM Brand b WHERE b.category.id = :categoryId")
    Long countBrandsByCategoryId(@Param("categoryId") UUID categoryId);

    Optional<Category> findTopByOrderByDisplayOrderDesc();
}