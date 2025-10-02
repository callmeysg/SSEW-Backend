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
package com.singhtwenty2.ssew_core.data.dto.cart;

import com.singhtwenty2.ssew_core.data.enums.CartType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CartDTO {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddItemRequest {
        @NotBlank(message = "Product ID is required")
        private String productId;

        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        @NotNull(message = "Cart type is required")
        private CartType cartType;

        public Integer getQuantity() {
            if (cartType == CartType.WISHLIST) {
                return 1;
            }
            return quantity != null ? quantity : 1;
        }
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateItemRequest {
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MoveItemRequest {
        @NotNull(message = "Target cart type is required")
        private CartType targetCartType;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CartItemResponse {
        private String cartItemId;
        private String productId;
        private String productName;
        private String productSlug;
        private String productSku;
        private String manufacturerName;
        private String thumbnailUrl;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private Boolean inStock;
        private LocalDateTime addedAt;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CartResponse {
        private String cartId;
        private String userId;
        private CartType cartType;
        private Integer totalItems;
        private BigDecimal totalAmount;
        private List<CartItemResponse> items;
        private LocalDateTime lastUpdated;
        private Boolean itemAlreadyExists;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CartSummary {
        private CartType cartType;
        private Integer totalItems;
        private BigDecimal totalAmount;
        private LocalDateTime lastUpdated;
    }
}