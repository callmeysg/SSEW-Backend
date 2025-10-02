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
package com.singhtwenty2.commerce_service.data.repository;

import com.singhtwenty2.commerce_service.data.entity.Cart;
import com.singhtwenty2.commerce_service.data.entity.User;
import com.singhtwenty2.commerce_service.data.enums.CartType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByUserAndCartTypeAndIsActive(User user, CartType cartType, Boolean isActive);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.product p " +
           "WHERE c.user = :user AND c.cartType = :cartType AND c.isActive = true")
    Optional<Cart> findByUserAndCartTypeWithItems(@Param("user") User user, @Param("cartType") CartType cartType);

    @Query("SELECT c FROM Cart c WHERE c.user = :user AND c.isActive = true")
    List<Cart> findAllByUserAndIsActive(@Param("user") User user);

    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.user = :user AND ci.cart.cartType = :cartType AND ci.cart.isActive = true")
    Integer countItemsByUserAndCartType(@Param("user") User user, @Param("cartType") CartType cartType);

    @Query("SELECT c FROM Cart c WHERE c.updatedAt < :cutoffDate AND c.isActive = true")
    List<Cart> findInactiveCartsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    Optional<Cart> findByIdAndUserIdAndCartTypeAndIsActiveTrue(UUID cartId, UUID userId, CartType cartType);

    Optional<Cart> findByUserIdAndCartTypeAndIsActiveTrue(UUID userId, CartType cartType);
}