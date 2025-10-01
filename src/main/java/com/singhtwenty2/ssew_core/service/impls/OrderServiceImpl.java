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
package com.singhtwenty2.ssew_core.service.impls;

import com.singhtwenty2.ssew_core.data.dto.catalogue.PreSignedUrlDTO;
import com.singhtwenty2.ssew_core.data.dto.catalogue.PreSignedUrlDTO.PresignedUrlResponse;
import com.singhtwenty2.ssew_core.data.entity.*;
import com.singhtwenty2.ssew_core.data.enums.CartType;
import com.singhtwenty2.ssew_core.data.enums.OrderStatus;
import com.singhtwenty2.ssew_core.data.repository.CartRepository;
import com.singhtwenty2.ssew_core.data.repository.OrderRepository;
import com.singhtwenty2.ssew_core.data.repository.UserRepository;
import com.singhtwenty2.ssew_core.service.file_handeling.S3Service;
import com.singhtwenty2.ssew_core.service.order_mangement.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.singhtwenty2.ssew_core.data.dto.order_mangement.OrderDTO.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Override
    public OrderResponse createOrder(CreateOrderRequest createRequest, String userId) {
        log.debug("Creating order for user: {} from cart: {}", userId, createRequest.getCartId());

        User user = findUserById(userId);
        Cart cart = findCartById(createRequest.getCartId(), user.getId());

        validateCartForOrder(cart);
        Order savedOrder = createOrderFromCart(createRequest, user, cart);
        clearCart(cart);

        log.info("Order created successfully with ID: {}", savedOrder.getId());
        return buildOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String orderId, String userId) {
        log.debug("Fetching order by ID: {} for user: {}", orderId, userId);

        UUID userUuid = parseUUID(userId, "Invalid user ID format");
        Order order = findOrderByIdAndUserId(orderId, userUuid);

        return buildOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByIdForAdmin(String orderId) {
        log.debug("Fetching order by ID for admin: {}", orderId);

        Order order = findOrderById(orderId);
        return buildOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getUserOrders(String userId, OrderStatus status, Pageable pageable) {
        log.debug("Fetching orders for user: {} with status: {}", userId, status);

        UUID userUuid = parseUUID(userId, "Invalid user ID format");

        Page<Order> orderPage = status != null ?
                orderRepository.findUserOrdersWithFilters(userUuid, status, pageable) :
                orderRepository.findByUserIdOrderByCreatedAtDesc(userUuid, pageable);

        return orderPage.map(this::buildOrderSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getAllOrdersForAdmin(String phoneNumber, OrderStatus status,
                                                           String search, Pageable pageable) {
        log.debug("Fetching orders for admin with filters");

        Page<Order> orderPage;

        if (StringUtils.hasText(search)) {
            orderPage = orderRepository.findOrdersWithFiltersAndSearch(
                    StringUtils.hasText(phoneNumber) ? phoneNumber.trim() : null,
                    status,
                    search.trim(),
                    pageable
            );
        } else {
            if (StringUtils.hasText(phoneNumber) || status != null) {
                orderPage = orderRepository.findOrdersWithFilters(
                        StringUtils.hasText(phoneNumber) ? phoneNumber.trim() : null,
                        status,
                        pageable
                );
            } else {
                orderPage = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
        }

        return orderPage.map(this::buildOrderSummaryResponse);
    }

    @Override
    public OrderResponse updateOrderStatus(String orderId, UpdateOrderStatusRequest updateRequest, boolean isAdmin) {
        log.debug("Updating order status for ID: {} to status: {}", orderId, updateRequest.getStatus());

        if (updateRequest.getStatus() == OrderStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot set status to CANCELLED through this endpoint. Use cancellation endpoints instead."
            );
        }

        Order order = findOrderById(orderId);
        validateStatusTransition(order, updateRequest.getStatus(), isAdmin);

        order.updateStatus(updateRequest.getStatus(), updateRequest.getRemarks(), isAdmin);
        if (StringUtils.hasText(updateRequest.getRemarks()) && isAdmin) {
            order.setAdminRemarks(updateRequest.getRemarks());
        }

        Order updatedOrder = orderRepository.save(order);

        log.info("Order status updated successfully for ID: {} to status: {}", orderId, updateRequest.getStatus());
        return buildOrderResponse(updatedOrder);
    }

    @Override
    public OrderResponse cancelOrder(String orderId, String userId, String remarks) {
        log.debug("User cancelling order ID: {} with remarks: {}", orderId, remarks);

        UUID userUuid = parseUUID(userId, "Invalid user ID format");
        Order order = findOrderByIdAndUserId(orderId, userUuid);

        if (!order.canBeCancelledByUser()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order cannot be cancelled. Current status: " + order.getStatus()
            );
        }

        order.updateStatus(OrderStatus.CANCELLED, remarks, false);
        Order cancelledOrder = orderRepository.save(order);

        log.info("Order cancelled successfully by user for ID: {}", orderId);
        return buildOrderResponse(cancelledOrder);
    }

    @Override
    public OrderResponse cancelOrderByAdmin(String orderId, String remarks) {
        log.debug("Admin cancelling order ID: {} with remarks: {}", orderId, remarks);

        Order order = findOrderById(orderId);

        if (order.isCancelled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is already cancelled");
        }

        order.updateStatus(OrderStatus.CANCELLED, remarks, true);
        if (StringUtils.hasText(remarks)) {
            order.setAdminRemarks(remarks);
        }

        Order cancelledOrder = orderRepository.save(order);

        log.info("Order cancelled successfully by admin for ID: {}", orderId);
        return buildOrderResponse(cancelledOrder);
    }

    @Override
    public void deleteOrder(String orderId) {
        log.debug("Deleting order with ID: {}", orderId);

        Order order = findOrderById(orderId);
        orderRepository.delete(order);

        log.info("Order deleted successfully with ID: {}", orderId);
    }

    @Override
    public Map<String, Object> buyAgain(BuyAgainRequest buyAgainRequest, String userId) {
        log.debug("Processing buy again request for order: {} by user: {}", buyAgainRequest.getOrderId(), userId);

        UUID userUuid = parseUUID(userId, "Invalid user ID format");
        Order order = findOrderByIdAndUserId(buyAgainRequest.getOrderId(), userUuid);

        Cart cart = findOrCreateUserCart(userUuid);
        int itemsAdded = 0;

        for (OrderItem orderItem : order.getOrderItems()) {
            try {
                addToCart(cart, orderItem.getProduct(), orderItem.getQuantity());
                itemsAdded++;
            } catch (Exception e) {
                log.warn("Failed to add product {} to cart during buy again: {}",
                        orderItem.getProductSku(), e.getMessage());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("cart_id", cart.getId().toString());
        response.put("items_added", itemsAdded);
        response.put("total_items_requested", order.getOrderItems().size());

        log.info("Buy again completed for order: {} - {} items added to cart", order.getId(), itemsAdded);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOrderStatistics getOrderStatistics() {
        log.debug("Fetching order statistics");

        return AdminOrderStatistics.builder()
                .totalOrders(orderRepository.count())
                .placedOrders(orderRepository.countByStatus(OrderStatus.PLACED))
                .confirmedOrders(orderRepository.countByStatus(OrderStatus.CONFIRMED))
                .shippedOrders(orderRepository.countByStatus(OrderStatus.SHIPPED))
                .deliveredOrders(orderRepository.countByStatus(OrderStatus.DELIVERED))
                .cancelledOrders(orderRepository.countByStatus(OrderStatus.CANCELLED))
                .outOfStockOrders(orderRepository.countByStatus(OrderStatus.OUT_OF_STOCK))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserOrderStatistics getUserOrderStatistics(String userId) {
        log.debug("Fetching order statistics for user: {}", userId);

        UUID userUuid = parseUUID(userId, "Invalid user ID format");

        return UserOrderStatistics.builder()
                .totalOrders(orderRepository.countByUserId(userUuid))
                .placedOrders(orderRepository.countByUserIdAndStatus(userUuid, OrderStatus.PLACED))
                .confirmedOrders(orderRepository.countByUserIdAndStatus(userUuid, OrderStatus.CONFIRMED))
                .shippedOrders(orderRepository.countByUserIdAndStatus(userUuid, OrderStatus.SHIPPED))
                .deliveredOrders(orderRepository.countByUserIdAndStatus(userUuid, OrderStatus.DELIVERED))
                .cancelledOrders(orderRepository.countByUserIdAndStatus(userUuid, OrderStatus.CANCELLED))
                .build();
    }

    private User findUserById(String userId) {
        UUID userUuid = parseUUID(userId, "Invalid user ID format");
        return userRepository.findById(userUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Cart findCartById(String cartId, UUID userId) {
        UUID cartUuid = parseUUID(cartId, "Invalid cart ID format");
        return cartRepository.findByIdAndUserIdAndCartTypeAndIsActiveTrue(cartUuid, userId, CartType.CART)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));
    }

    private Order findOrderById(String orderId) {
        UUID orderUuid = parseUUID(orderId, "Invalid order ID format");
        return orderRepository.findById(orderUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private Order findOrderByIdAndUserId(String orderId, UUID userId) {
        UUID orderUuid = parseUUID(orderId, "Invalid order ID format");
        return orderRepository.findByIdAndUserId(orderUuid, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private Cart findOrCreateUserCart(UUID userId) {
        return cartRepository.findByUserIdAndCartTypeAndIsActiveTrue(userId, CartType.CART)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setCartType(CartType.CART);
                    newCart.setIsActive(true);
                    return cartRepository.save(newCart);
                });
    }

    private void validateCartForOrder(Cart cart) {
        if (cart.getCartItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        for (CartItem cartItem : cart.getCartItems()) {
            if (!cartItem.getProduct().getIsActive()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Product " + cartItem.getProduct().getName() + " is no longer available"
                );
            }
        }
    }

    private Order createOrderFromCart(CreateOrderRequest createRequest, User user, Cart cart) {
        Order order = new Order();
        order.setUser(user);
        order.setCustomerName(createRequest.getCustomerName());
        order.setPhoneNumber(createRequest.getPhoneNumber());
        order.setStreetAddress(createRequest.getStreetAddress());
        order.setCity(createRequest.getCity());
        order.setState(createRequest.getState());
        order.setPincode(createRequest.getPincode());
        order.setStatus(OrderStatus.PLACED);
        order.setStatusUpdatedAt(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalItems = 0;

        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem(
                    cartItem.getProduct(),
                    cartItem.getQuantity(),
                    cartItem.getPriceAtTime()
            );
            order.addOrderItem(orderItem);

            totalAmount = totalAmount.add(orderItem.getTotalPrice());
            totalItems += cartItem.getQuantity();
        }

        order.setTotalAmount(totalAmount);
        order.setTotalItems(totalItems);

        Order savedOrder = orderRepository.save(order);
        savedOrder.updateStatus(OrderStatus.PLACED, "Order placed successfully", false);

        return orderRepository.save(savedOrder);
    }

    private void clearCart(Cart cart) {
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    private void addToCart(Cart cart, Product product, Integer quantity) {
        cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                        existingItem -> existingItem.increaseQuantity(quantity),
                        () -> {
                            CartItem newItem = new CartItem();
                            newItem.setCart(cart);
                            newItem.setProduct(product);
                            newItem.setQuantity(quantity);
                            newItem.setPriceAtTime(product.getPrice());
                            cart.addCartItem(newItem);
                        }
                );

        cartRepository.save(cart);
    }

    private void validateStatusTransition(Order order, OrderStatus newStatus, boolean isAdmin) {
        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == newStatus) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is already in " + newStatus + " status");
        }

        if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot change status from " + currentStatus);
        }

        if (!isAdmin && newStatus != OrderStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admin can change status to " + newStatus);
        }
    }

    private OrderResponse buildOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId().toString())
                .userId(order.getUser().getId().toString())
                .customerName(order.getCustomerName())
                .phoneNumber(order.getPhoneNumber())
                .streetAddress(order.getStreetAddress())
                .city(order.getCity())
                .state(order.getState())
                .pincode(order.getPincode())
                .fullAddress(order.getFullAddress())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .totalItems(order.getTotalItems())
                .adminRemarks(order.getAdminRemarks())
                .statusUpdatedAt(order.getStatusUpdatedAt() != null ? order.getStatusUpdatedAt().toString() : null)
                .cancelledAt(order.getCancelledAt() != null ? order.getCancelledAt().toString() : null)
                .cancelledByAdmin(order.getCancelledByAdmin())
                .canBeCancelledByUser(order.canBeCancelledByUser())
                .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().toString() : null)
                .updatedAt(order.getUpdatedAt() != null ? order.getUpdatedAt().toString() : null)
                .orderItems(order.getOrderItems().stream().map(this::buildOrderItemResponse).toList())
                .statusHistory(order.getStatusHistory().stream().map(this::buildOrderStatusHistoryResponse).toList())
                .build();
    }

    private OrderSummaryResponse buildOrderSummaryResponse(Order order) {
        return OrderSummaryResponse.builder()
                .orderId(order.getId().toString())
                .customerName(order.getCustomerName())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .totalItems(order.getTotalItems())
                .canBeCancelledByUser(order.canBeCancelledByUser())
                .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().toString() : null)
                .statusUpdatedAt(order.getStatusUpdatedAt() != null ? order.getStatusUpdatedAt().toString() : null)
                .orderItems(order.getOrderItems().stream().map(this::buildOrderItemResponse).toList())
                .build();
    }

    private OrderItemResponse buildOrderItemResponse(OrderItem orderItem) {
        PresignedUrlResponse presignedUrlResponse = null;
        if (StringUtils.hasText(orderItem.getProduct().getThumbnailObjectKey())) {
            try {
                presignedUrlResponse = s3Service.generateReadPresignedUrl(
                        orderItem.getProduct().getThumbnailObjectKey(),
                        60
                );
            } catch (Exception e) {
                log.warn("Failed to generate thumbnail URL for product {}: {}",
                        orderItem.getProductSku(), e.getMessage());
            }
        }

        assert presignedUrlResponse != null;
        return OrderItemResponse.builder()
                .orderItemId(orderItem.getId().toString())
                .productId(orderItem.getProduct().getId().toString())
                .productName(orderItem.getProductName())
                .productSku(orderItem.getProductSku())
                .manufacturerName(orderItem.getManufacturerName())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .totalPrice(orderItem.getTotalPrice())
                .thumbnailUrl(
                        presignedUrlResponse.getPresignedUrl() != null ? presignedUrlResponse.getPresignedUrl() : null
                )
                .build();
    }

    private OrderStatusHistoryResponse buildOrderStatusHistoryResponse(OrderStatusHistory history) {
        return OrderStatusHistoryResponse.builder()
                .historyId(history.getId().toString())
                .previousStatus(history.getPreviousStatus())
                .newStatus(history.getNewStatus())
                .remarks(history.getRemarks())
                .changedByAdmin(history.getChangedByAdmin())
                .changedAt(history.getChangedAt().toString())
                .build();
    }

    private UUID parseUUID(String id, String errorMessage) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }
}