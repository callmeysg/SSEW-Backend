package com.singhtwenty2.commerce_service.data.entity;

import com.singhtwenty2.commerce_service.data.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_order_user", columnList = "user_id"),
                @Index(name = "idx_order_status", columnList = "status"),
                @Index(name = "idx_order_created", columnList = "created_at"),
                @Index(name = "idx_order_user_status", columnList = "user_id, status"),
                @Index(name = "idx_order_user_created", columnList = "user_id, created_at DESC"),
                @Index(name = "idx_order_phone", columnList = "phone_number")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "street_address", nullable = false, length = 300)
    private String streetAddress;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 50)
    private String state;

    @Column(name = "pincode", nullable = false, length = 10)
    private String pincode;

    @Column(name = "full_address", nullable = false, length = 500)
    private String fullAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PLACED;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "total_items", nullable = false)
    private Integer totalItems;

    @Column(name = "admin_remarks", columnDefinition = "TEXT")
    private String adminRemarks;

    @Column(name = "status_updated_at")
    private LocalDateTime statusUpdatedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by_admin", nullable = false)
    private Boolean cancelledByAdmin = false;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void removeOrderItem(OrderItem orderItem) {
        orderItems.remove(orderItem);
        orderItem.setOrder(null);
    }

    public void addStatusHistory(OrderStatusHistory statusHistory) {
        this.statusHistory.add(statusHistory);
        statusHistory.setOrder(this);
    }

    public void updateStatus(OrderStatus newStatus, String remarks, boolean isAdmin) {
        OrderStatus previousStatus = this.status;
        this.status = newStatus;
        this.statusUpdatedAt = LocalDateTime.now();

        if (newStatus == OrderStatus.CANCELLED) {
            this.cancelledAt = LocalDateTime.now();
            this.cancelledByAdmin = isAdmin;
        }

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(this);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setRemarks(remarks);
        history.setChangedByAdmin(isAdmin);
        history.setChangedAt(LocalDateTime.now());

        addStatusHistory(history);
    }

    public boolean canBeCancelledByUser() {
        return status == OrderStatus.PLACED && cancelledAt == null;
    }

    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }

    public boolean isDelivered() {
        return status == OrderStatus.DELIVERED;
    }

    public boolean isPending() {
        return status == OrderStatus.PLACED || status == OrderStatus.CONFIRMED;
    }

    public boolean isInTransit() {
        return status == OrderStatus.SHIPPED;
    }

    public String getFormattedAddress() {
        return String.format("%s, %s, %s - %s", streetAddress, city, state, pincode);
    }

    @PrePersist
    private void generateFullAddress() {
        if (streetAddress != null && city != null && state != null && pincode != null) {
            this.fullAddress = getFormattedAddress();
        }
    }

    @PreUpdate
    private void updateFullAddress() {
        if (streetAddress != null && city != null && state != null && pincode != null) {
            this.fullAddress = getFormattedAddress();
        }
    }
}