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

import com.singhtwenty2.ssew_core.data.entity.Order;
import com.singhtwenty2.ssew_core.data.enums.OrderStatus;
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

    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT o FROM Order o WHERE " +
           "(:customerName IS NULL OR UPPER(o.customerName) LIKE UPPER(CONCAT('%', :customerName, '%'))) AND " +
           "(:phoneNumber IS NULL OR o.phoneNumber LIKE CONCAT('%', :phoneNumber, '%')) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:city IS NULL OR UPPER(o.city) LIKE UPPER(CONCAT('%', :city, '%'))) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findOrdersWithFilters(
            @Param("phoneNumber") String phoneNumber,
            @Param("status") OrderStatus status,
            Pageable pageable
    );

    @Query("SELECT o FROM Order o WHERE " +
           "o.user.id = :userId AND " +
           "(:status IS NULL OR o.status = :status) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findUserOrdersWithFilters(
            @Param("userId") UUID userId,
            @Param("status") OrderStatus status,
            Pageable pageable
    );

    @Query("SELECT o FROM Order o WHERE " +
           "(:phoneNumber IS NULL OR o.phoneNumber LIKE CONCAT('%', :phoneNumber, '%')) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:searchTerm IS NULL OR " +
           "o.phoneNumber LIKE CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findOrdersWithFiltersAndSearch(
            @Param("phoneNumber") String phoneNumber,
            @Param("status") OrderStatus status,
            @Param("searchTerm") String searchTerm,
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