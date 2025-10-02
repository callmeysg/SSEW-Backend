/**
 * Copyright 2025 SSEW Core Service
 * Developer: Aryan Singh (@singhtwenty2)
 * Portfolio: https://singhtwenty2.pages.dev/
 * This file is part of SSEW E-commerce Backend System
 * Licensed under MIT License
 * For commercial use and inquiries: aryansingh.corp@gmail.com
 * @author Aryan Singh (@singhtwenty2)
 * @project SSEW E-commerce Backend System
 * @since 2025
 */
package com.singhtwenty2.ssew_core.data.repository;

import com.singhtwenty2.ssew_core.data.entity.Cart;
import com.singhtwenty2.ssew_core.data.entity.CartItem;
import com.singhtwenty2.ssew_core.data.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product p WHERE ci.cart = :cart ORDER BY ci.createdAt DESC")
    List<CartItem> findByCartWithProduct(@Param("cart") Cart cart);

    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart = :cart")
    Integer countByCart(@Param("cart") Cart cart);

    void deleteByCartAndProduct(Cart cart, Product product);

    @Query("SELECT ci FROM CartItem ci WHERE ci.product = :product")
    List<CartItem> findByProduct(@Param("product") Product product);

    Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);
}