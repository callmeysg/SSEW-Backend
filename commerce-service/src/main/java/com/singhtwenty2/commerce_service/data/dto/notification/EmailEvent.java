package com.singhtwenty2.commerce_service.data.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailEvent implements Serializable {

    private String eventId;
    private String eventType;
    private String recipientEmail;
    private LocalDateTime createdAt;
    private Integer retryCount;
    private EmailMetadata metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailMetadata implements Serializable {
        private String orderId;
        private String customerName;
        private String phoneNumber;
        private String fullAddress;
        private BigDecimal totalAmount;
        private Integer totalItems;
        private List<OrderItemData> orderItems;
        private String orderPlacedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemData implements Serializable {
        private String productName;
        private String productSku;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}