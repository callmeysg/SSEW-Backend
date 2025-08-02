package com.singhtwenty2.ssew_core.data.repository;

import com.singhtwenty2.ssew_core.data.entity.Product;
import com.singhtwenty2.ssew_core.data.entity.ProductImage;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    List<ProductImage> findByProduct(Product product, Sort sort);

    List<ProductImage> findByProductId(UUID productId, Sort sort);

    Optional<ProductImage> findByProductIdAndIsThumbnailTrue(UUID productId);

    List<ProductImage> findByProductIdAndIsThumbnailFalse(UUID productId, Sort sort);

    Optional<ProductImage> findByIdAndProductId(UUID imageId, UUID productId);

    boolean existsByProductIdAndIsThumbnailTrue(UUID productId);

    long countByProductId(UUID productId);

    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId AND pi.displayOrder > :displayOrder")
    List<ProductImage> findByProductIdAndDisplayOrderGreaterThan(@Param("productId") UUID productId, @Param("displayOrder") Integer displayOrder);

    @Modifying
    @Query("UPDATE ProductImage pi SET pi.displayOrder = pi.displayOrder - 1 WHERE pi.product.id = :productId AND pi.displayOrder > :displayOrder")
    void decrementDisplayOrderAfter(@Param("productId") UUID productId, @Param("displayOrder") Integer displayOrder);

    @Modifying
    @Query("UPDATE ProductImage pi SET pi.displayOrder = pi.displayOrder + 1 WHERE pi.product.id = :productId AND pi.displayOrder >= :displayOrder")
    void incrementDisplayOrderFrom(@Param("productId") UUID productId, @Param("displayOrder") Integer displayOrder);

    @Query("SELECT MAX(pi.displayOrder) FROM ProductImage pi WHERE pi.product.id = :productId")
    Optional<Integer> findMaxDisplayOrderByProductId(@Param("productId") UUID productId);

    @Modifying
    @Query("UPDATE ProductImage pi SET pi.isThumbnail = false WHERE pi.product.id = :productId")
    void clearThumbnailStatusForProduct(@Param("productId") UUID productId);

    void deleteByProductId(UUID productId);
}