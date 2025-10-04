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

import com.singhtwenty2.commerce_service.data.entity.Order;
import com.singhtwenty2.commerce_service.data.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByIdAndUserId(UUID orderId, UUID userId);

    Page<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Order> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE " +
           "(:customerName IS NULL OR UPPER(o.customerName) LIKE UPPER(CONCAT('%', :customerName, '%'))) AND " +
           "(:status IS NULL OR o.status = :status)")
    Page<Order> findOrdersWithFilters(
            @Param("customerName") String customerName,
            @Param("status") OrderStatus status,
            Pageable pageable
    );

    @Query("SELECT o FROM Order o WHERE " +
           "o.user.id = :userId AND " +
           "(:status IS NULL OR o.status = :status)")
    Page<Order> findUserOrdersWithFilters(
            @Param("userId") UUID userId,
            @Param("status") OrderStatus status,
            Pageable pageable
    );

    List<Order> findByUserIdAndStatusInOrderByCreatedAtDesc(UUID userId, List<OrderStatus> statuses);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    Long countByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    Long countByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    List<Order> findOrdersByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    List<Order> findUserOrdersByDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    boolean existsByUserIdAndId(UUID userId, UUID orderId);
}