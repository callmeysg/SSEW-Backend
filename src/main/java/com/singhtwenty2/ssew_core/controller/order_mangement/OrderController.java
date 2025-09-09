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
package com.singhtwenty2.ssew_core.controller.order_mangement;

import com.singhtwenty2.ssew_core.data.dto.common.GlobalApiResponse;
import com.singhtwenty2.ssew_core.data.dto.common.PageResponse;
import com.singhtwenty2.ssew_core.data.dto.order_mangement.OrderDTO.*;
import com.singhtwenty2.ssew_core.data.enums.OrderStatus;
import com.singhtwenty2.ssew_core.service.order_mangement.OrderService;
import com.singhtwenty2.ssew_core.util.io.AuthenticationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.singhtwenty2.ssew_core.util.io.NetworkUtils.getClientIP;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest createRequest,
            Authentication authentication,
            HttpServletRequest request
    ) {
        String userId = AuthenticationUtils.extractUserId(authentication, request, "create order");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<OrderResponse>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.info("Order creation attempt from IP: {} for user: {}", getClientIP(request), userId);

        OrderResponse response = orderService.createOrder(createRequest, userId);

        log.info("Order created successfully with ID: {}", response.getOrderId());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                GlobalApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order created successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<OrderResponse>> getOrderById(
            @PathVariable String orderId,
            Authentication authentication,
            HttpServletRequest request
    ) {
        String userId = AuthenticationUtils.extractUserId(authentication, request, "get order by id");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<OrderResponse>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.debug("Fetching order by ID: {} for user: {} from IP: {}", orderId, userId, getClientIP(request));

        OrderResponse response = orderService.getOrderById(orderId, userId);

        return ResponseEntity.ok(
                GlobalApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/admin/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<OrderResponse>> getOrderByIdForAdmin(
            @PathVariable String orderId,
            HttpServletRequest request
    ) {
        log.debug("Fetching order by ID for admin: {} from IP: {}", orderId, getClientIP(request));

        OrderResponse response = orderService.getOrderByIdForAdmin(orderId);

        return ResponseEntity.ok(
                GlobalApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<PageResponse<OrderSummaryResponse>>> getUserOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication,
            HttpServletRequest request
    ) {
        String userId = AuthenticationUtils.extractUserId(authentication, request, "get user orders");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<PageResponse<OrderSummaryResponse>>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.debug("Fetching user orders for user: {} from IP: {}", userId, getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<OrderSummaryResponse> orderPage = orderService.getUserOrders(userId, status, pageable);
        PageResponse<OrderSummaryResponse> response = PageResponse.from(orderPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<OrderSummaryResponse>>builder()
                        .success(true)
                        .message("User orders retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<PageResponse<OrderSummaryResponse>>> getAllOrdersForAdmin(
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request
    ) {
        log.debug("Fetching all orders for admin from IP: {}", getClientIP(request));

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(index, limit, sort);

        Page<OrderSummaryResponse> orderPage = orderService.getAllOrdersForAdmin(
                phoneNumber, status, search, pageable);
        PageResponse<OrderSummaryResponse> response = PageResponse.from(orderPage);

        return ResponseEntity.ok(
                GlobalApiResponse.<PageResponse<OrderSummaryResponse>>builder()
                        .success(true)
                        .message("Orders retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusRequest updateRequest,
            HttpServletRequest request
    ) {
        log.info("Order status update attempt from IP: {} for order ID: {} to status: {}",
                getClientIP(request), orderId, updateRequest.getStatus());

        OrderResponse response = orderService.updateOrderStatus(orderId, updateRequest, true);

        log.info("Order status updated successfully for ID: {} to status: {}", orderId, updateRequest.getStatus());

        return ResponseEntity.ok(
                GlobalApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order status updated successfully")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<OrderResponse>> cancelOrder(
            @PathVariable String orderId,
            @RequestParam(required = false) String remarks,
            Authentication authentication,
            HttpServletRequest request
    ) {
        String userId = AuthenticationUtils.extractUserId(authentication, request, "cancel order");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<OrderResponse>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.info("Order cancellation attempt from IP: {} for order ID: {} by user: {}",
                getClientIP(request), orderId, userId);

        OrderResponse response = orderService.cancelOrder(orderId, userId, remarks);

        log.info("Order cancelled successfully for ID: {} by user: {}", orderId, userId);

        return ResponseEntity.ok(
                GlobalApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order cancelled successfully")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/admin/{orderId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<OrderResponse>> cancelOrderByAdmin(
            @PathVariable String orderId,
            @RequestParam(required = false) String remarks,
            HttpServletRequest request
    ) {
        log.info("Order cancellation attempt by admin from IP: {} for order ID: {}",
                getClientIP(request), orderId);

        OrderResponse response = orderService.cancelOrderByAdmin(orderId, remarks);

        log.info("Order cancelled successfully by admin for ID: {}", orderId);

        return ResponseEntity.ok(
                GlobalApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order cancelled successfully")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/admin/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<Map<String, Object>>> deleteOrder(
            @PathVariable String orderId,
            HttpServletRequest request
    ) {
        log.info("Order deletion attempt by admin from IP: {} for order ID: {}", getClientIP(request), orderId);

        orderService.deleteOrder(orderId);

        log.info("Order deleted successfully by admin for ID: {}", orderId);

        return ResponseEntity.ok(
                GlobalApiResponse.<Map<String, Object>>builder()
                        .success(true)
                        .message("Order deleted successfully")
                        .data(null)
                        .build()
        );
    }

    @PostMapping("/buy-again")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<Map<String, Object>>> buyAgain(
            @Valid @RequestBody BuyAgainRequest buyAgainRequest,
            Authentication authentication,
            HttpServletRequest request
    ) {
        String userId = AuthenticationUtils.extractUserId(authentication, request, "buy again");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.info("Buy again request from IP: {} for order ID: {} by user: {}",
                getClientIP(request), buyAgainRequest.getOrderId(), userId);

        Map<String, Object> response = orderService.buyAgain(buyAgainRequest, userId);

        log.info("Buy again completed successfully for order ID: {} by user: {}",
                buyAgainRequest.getOrderId(), userId);

        return ResponseEntity.ok(
                GlobalApiResponse.<Map<String, Object>>builder()
                        .success(true)
                        .message("Items added to cart successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<AdminOrderStatistics>> getOrderStatistics(
            HttpServletRequest request
    ) {
        log.debug("Fetching order statistics from IP: {}", getClientIP(request));

        AdminOrderStatistics statistics = orderService.getOrderStatistics();

        return ResponseEntity.ok(
                GlobalApiResponse.<AdminOrderStatistics>builder()
                        .success(true)
                        .message("Order statistics retrieved successfully")
                        .data(statistics)
                        .build()
        );
    }

    @GetMapping("/my-statistics")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<UserOrderStatistics>> getUserOrderStatistics(
            Authentication authentication,
            HttpServletRequest request
    ) {
        String userId = AuthenticationUtils.extractUserId(authentication, request, "get user order statistics");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<UserOrderStatistics>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.debug("Fetching user order statistics for user: {} from IP: {}", userId, getClientIP(request));

        UserOrderStatistics statistics = orderService.getUserOrderStatistics(userId);

        return ResponseEntity.ok(
                GlobalApiResponse.<UserOrderStatistics>builder()
                        .success(true)
                        .message("User order statistics retrieved successfully")
                        .data(statistics)
                        .build()
        );
    }
}