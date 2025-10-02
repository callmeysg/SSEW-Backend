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
package com.singhtwenty2.ssew_core.service.order_mangement;

import com.singhtwenty2.ssew_core.data.dto.order_mangement.OrderDTO.*;
import com.singhtwenty2.ssew_core.data.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest createRequest, String userId);

    OrderResponse getOrderById(String orderId, String userId);

    OrderResponse getOrderByIdForAdmin(String orderId);

    Page<OrderSummaryResponse> getUserOrders(String userId, OrderStatus status, Pageable pageable);

    Page<OrderSummaryResponse> getAllOrdersForAdmin(String phoneNumber, OrderStatus status,
                                                    String search, Pageable pageable);

    OrderResponse updateOrderStatus(String orderId, UpdateOrderStatusRequest updateRequest, boolean isAdmin);

    OrderResponse cancelOrder(String orderId, String userId, String remarks);

    OrderResponse cancelOrderByAdmin(String orderId, String remarks);

    void deleteOrder(String orderId);

    Map<String, Object> buyAgain(BuyAgainRequest buyAgainRequest, String userId);

    AdminOrderStatistics getOrderStatistics();

    UserOrderStatistics getUserOrderStatistics(String userId);
}