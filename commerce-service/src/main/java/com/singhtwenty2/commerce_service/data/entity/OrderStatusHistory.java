package com.singhtwenty2.commerce_service.data.entity;

import com.singhtwenty2.commerce_service.data.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "order_status_history",
        indexes = {
                @Index(name = "idx_order_status_history_order", columnList = "order_id"),
                @Index(name = "idx_order_status_history_changed_at", columnList = "changed_at DESC"),
                @Index(name = "idx_order_status_history_order_changed", columnList = "order_id, changed_at DESC"),
                @Index(name = "idx_order_status_history_status", columnList = "new_status"),
                @Index(name = "idx_order_status_history_admin", columnList = "changed_by_admin")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private OrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private OrderStatus newStatus;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "changed_by_admin", nullable = false)
    private Boolean changedByAdmin = false;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public boolean isStatusUpgrade() {
        if (previousStatus == null) return true;

        return switch (previousStatus) {
            case PLACED -> newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED;
            case CONFIRMED ->
                    newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.OUT_OF_STOCK;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELLED;
            case DELIVERED, OUT_OF_STOCK, CANCELLED -> false;
        };
    }

    public boolean isCancellation() {
        return newStatus == OrderStatus.CANCELLED;
    }
}