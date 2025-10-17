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
package com.singhtwenty2.commerce_service.data.dto.order_mangement;

import com.singhtwenty2.commerce_service.data.enums.OrderStatus;
import com.singhtwenty2.commerce_service.data.enums.OrderType;
import com.singhtwenty2.commerce_service.data.validation.ValidOrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

public class OrderDTO {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateOrderRequest {

        @NotBlank(message = "Cart ID is required")
        private String cartId;

        @NotBlank(message = "Customer name is required")
        @Size(max = 100, message = "Customer name must not exceed 100 characters")
        private String customerName;

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian phone number format")
        private String phoneNumber;

        @NotBlank(message = "Street address is required")
        @Size(max = 300, message = "Street address must not exceed 300 characters")
        private String streetAddress;

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City name must not exceed 100 characters")
        private String city;

        @NotBlank(message = "State is required")
        @Size(max = 50, message = "State name must not exceed 50 characters")
        private String state;

        @NotBlank(message = "Pincode is required")
        @Pattern(regexp = "^[1-9]\\d{5}$", message = "Invalid pincode format")
        private String pincode;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreatePickupOrderRequest {

        @NotBlank(message = "Cart ID is required")
        private String cartId;

        @NotBlank(message = "Customer name is required")
        @Size(max = 100, message = "Customer name must not exceed 100 characters")
        private String customerName;

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian phone number format")
        private String phoneNumber;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateOrderStatusRequest {

        @NotNull(message = "Order status is required")
        @ValidOrderStatus
        private OrderStatus status;

        @Size(max = 1000, message = "Remarks must not exceed 1000 characters")
        private String remarks;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemResponse {
        private String orderItemId;
        private String productId;
        private String productName;
        private String productSku;
        private String manufacturerName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String thumbnailUrl;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderStatusHistoryResponse {
        private String historyId;
        private OrderStatus previousStatus;
        private OrderStatus newStatus;
        private String remarks;
        private Boolean changedByAdmin;
        private String changedAt;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderResponse {
        private String orderId;
        private String userId;
        private String customerName;
        private String phoneNumber;
        private OrderType orderType;
        private String streetAddress;
        private String city;
        private String state;
        private String pincode;
        private String fullAddress;
        private OrderStatus status;
        private BigDecimal totalAmount;
        private Integer totalItems;
        private String adminRemarks;
        private String statusUpdatedAt;
        private String cancelledAt;
        private Boolean cancelledByAdmin;
        private Boolean canBeCancelledByUser;
        private String createdAt;
        private String updatedAt;
        private List<OrderItemResponse> orderItems;
        private List<OrderStatusHistoryResponse> statusHistory;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderSummaryResponse {
        private String orderId;
        private String customerName;
        private OrderType orderType;
        private OrderStatus status;
        private BigDecimal totalAmount;
        private Integer totalItems;
        private Boolean canBeCancelledByUser;
        private String createdAt;
        private String statusUpdatedAt;
        private List<OrderItemResponse> orderItems;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BuyAgainRequest {
        @NotBlank(message = "Order ID is required")
        private String orderId;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminOrderStatistics {
        private Long totalOrders;
        private Long placedOrders;
        private Long confirmedOrders;
        private Long shippedOrders;
        private Long deliveredOrders;
        private Long cancelledOrders;
        private Long outOfStockOrders;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserOrderStatistics {
        private Long totalOrders;
        private Long placedOrders;
        private Long confirmedOrders;
        private Long shippedOrders;
        private Long deliveredOrders;
        private Long cancelledOrders;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderFilterRequest {

        private String customerName;
        private OrderStatus status;

        @Builder.Default
        private Integer index = 0;

        @Builder.Default
        private Integer limit = 10;

        @Builder.Default
        private String sortBy = "createdAt";

        @Builder.Default
        private String sortDir = "desc";

        public boolean hasFilters() {
            return customerName != null || status != null;
        }

        public boolean hasCustomerNameFilter() {
            return customerName != null && !customerName.trim().isEmpty();
        }

        public boolean hasStatusFilter() {
            return status != null;
        }
    }
}