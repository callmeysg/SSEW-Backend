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

import com.singhtwenty2.ssew_core.data.entity.Cart;
import com.singhtwenty2.ssew_core.data.entity.CartItem;
import com.singhtwenty2.ssew_core.data.entity.Product;
import com.singhtwenty2.ssew_core.data.entity.User;
import com.singhtwenty2.ssew_core.data.enums.CartType;
import com.singhtwenty2.ssew_core.data.repository.CartItemRepository;
import com.singhtwenty2.ssew_core.data.repository.CartRepository;
import com.singhtwenty2.ssew_core.data.repository.ProductRepository;
import com.singhtwenty2.ssew_core.data.repository.UserRepository;
import com.singhtwenty2.ssew_core.exception.BusinessException;
import com.singhtwenty2.ssew_core.exception.ResourceNotFoundException;
import com.singhtwenty2.ssew_core.service.catalogue.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.singhtwenty2.ssew_core.data.dto.cart.CartDTO.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public CartResponse addItemToCart(String userId, AddItemRequest request) {
        log.info("Adding item to cart - UserId: {}, ProductId: {}, CartType: {}",
                userId, request.getProductId(), request.getCartType());

        User user = findUserById(userId);
        Product product = findProductById(request.getProductId());

        validateProductForCart(product, request.getCartType());

        Cart cart = getOrCreateCart(user, request.getCartType());

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(
                cart.getId(), product.getId());

        if (existingItem.isPresent()) {
            if (request.getCartType() == CartType.WISHLIST) {
                log.info("Item already exists in wishlist");
                CartResponse response = mapCartToResponse(cart);
                response.setItemAlreadyExists(true);
                return response;
            }
            CartItem cartItem = existingItem.get();
            cartItem.increaseQuantity(request.getQuantity());
            cartItem = cartItemRepository.save(cartItem);
            log.info("Updated existing cart item quantity to: {}", cartItem.getQuantity());
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPriceAtTime(product.getPrice());

            cart.addCartItem(cartItem);
            cart = cartRepository.save(cart);
            log.info("Added new item to cart");
        }

        CartResponse response = mapCartToResponse(cart);
        response.setItemAlreadyExists(false);
        return response;
    }

    @Override
    public CartResponse updateCartItemQuantity(String userId, String cartItemId, UpdateItemRequest request) {
        log.info("Updating cart item quantity - UserId: {}, CartItemId: {}", userId, cartItemId);

        User user = findUserById(userId);
        CartItem cartItem = findCartItemById(cartItemId);

        validateCartItemOwnership(cartItem, user);

        if (cartItem.getCart().getCartType() == CartType.WISHLIST) {
            throw new BusinessException("Cannot update quantity for wishlist items");
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        Cart cart = cartRepository.save(cartItem.getCart());
        log.info("Updated cart item quantity to: {}", request.getQuantity());

        return mapCartToResponse(cart);
    }

    @Override
    public CartResponse removeItemFromCart(String userId, String cartItemId, CartType cartType) {
        log.info("Removing item from cart - UserId: {}, CartItemId: {}, CartType: {}",
                userId, cartItemId, cartType);

        User user = findUserById(userId);
        CartItem cartItem = findCartItemById(cartItemId);

        validateCartItemOwnership(cartItem, user);

        if (cartItem.getCart().getCartType() != cartType) {
            throw new BusinessException("Cart item type mismatch");
        }

        Cart cart = cartItem.getCart();
        cart.removeCartItem(cartItem);
        cartItemRepository.delete(cartItem);

        cart = cartRepository.save(cart);
        log.info("Removed item from cart successfully");

        return mapCartToResponse(cart);
    }

    @Override
    public CartResponse moveItemBetweenCarts(String userId, String cartItemId, MoveItemRequest request) {
        log.info("Moving item between carts - UserId: {}, CartItemId: {}, TargetType: {}",
                userId, cartItemId, request.getTargetCartType());

        User user = findUserById(userId);
        CartItem sourceItem = findCartItemById(cartItemId);

        validateCartItemOwnership(sourceItem, user);

        CartType sourceType = sourceItem.getCart().getCartType();
        if (sourceType == request.getTargetCartType()) {
            throw new BusinessException("Source and target cart types are the same");
        }

        Cart targetCart = getOrCreateCart(user, request.getTargetCartType());
        Product product = sourceItem.getProduct();

        Optional<CartItem> existingTargetItem = cartItemRepository.findByCartIdAndProductId(
                targetCart.getId(), product.getId());

        if (existingTargetItem.isPresent()) {
            if (request.getTargetCartType() == CartType.CART) {
                CartItem targetItem = existingTargetItem.get();
                targetItem.increaseQuantity(1);
                cartItemRepository.save(targetItem);
                log.info("Item already exists in target cart, increased quantity");
            } else {
                throw new BusinessException("Item already exists in target cart");
            }
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(targetCart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getTargetCartType() == CartType.WISHLIST ? 1 : 1);
            newItem.setPriceAtTime(product.getPrice());

            targetCart.addCartItem(newItem);
            cartRepository.save(targetCart);
        }

        if (sourceType == CartType.CART) {
            Cart sourceCart = sourceItem.getCart();
            sourceCart.removeCartItem(sourceItem);
            cartItemRepository.delete(sourceItem);
            cartRepository.save(sourceCart);
        }

        Cart savedTargetCart = cartRepository.findById(targetCart.getId()).orElse(targetCart);
        log.info("Successfully moved item from {} to {}", sourceType, request.getTargetCartType());
        return mapCartToResponse(savedTargetCart);
    }

    @Override
    public CartResponse getCart(String userId, CartType cartType) {
        log.debug("Fetching cart - UserId: {}, CartType: {}", userId, cartType);

        User user = findUserById(userId);
        Optional<Cart> cartOpt = cartRepository.findByUserAndCartTypeWithItems(user, cartType);

        if (cartOpt.isEmpty()) {
            return createEmptyCartResponse(userId, cartType);
        }

        return mapCartToResponse(cartOpt.get());
    }

    @Override
    public List<CartSummary> getAllCartSummaries(String userId) {
        log.debug("Fetching all cart summaries - UserId: {}", userId);

        User user = findUserById(userId);
        List<Cart> carts = cartRepository.findAllByUserAndIsActive(user);

        return carts.stream()
                .map(this::mapCartToSummary)
                .collect(Collectors.toList());
    }

    @Override
    public CartResponse clearCart(String userId, CartType cartType) {
        log.info("Clearing cart - UserId: {}, CartType: {}", userId, cartType);

        User user = findUserById(userId);
        Optional<Cart> cartOpt = cartRepository.findByUserAndCartTypeAndIsActive(user, cartType, true);

        if (cartOpt.isEmpty()) {
            return createEmptyCartResponse(userId, cartType);
        }

        Cart cart = cartOpt.get();
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();

        cart = cartRepository.save(cart);
        log.info("Cart cleared successfully");

        return mapCartToResponse(cart);
    }

    @Override
    public String syncCartWithProductPrices(String userId, CartType cartType) {
        log.info("Syncing cart prices - UserId: {}, CartType: {}", userId, cartType);

        User user = findUserById(userId);
        Optional<Cart> cartOpt = cartRepository.findByUserAndCartTypeWithItems(user, cartType);

        if (cartOpt.isEmpty()) {
            return getEmptyCartMessage(cartType);
        }

        Cart cart = cartOpt.get();
        boolean pricesUpdated = false;

        for (CartItem item : cart.getCartItems()) {
            BigDecimal currentPrice = item.getProduct().getPrice();
            if (!item.getPriceAtTime().equals(currentPrice)) {
                item.setPriceAtTime(currentPrice);
                pricesUpdated = true;
            }
        }

        if (pricesUpdated) {
            cartRepository.save(cart);
            log.info("Cart prices synchronized");
            return getPricesUpdatedMessage(cartType);
        } else {
            return getPricesUpToDateMessage(cartType);
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldCarts() {
        log.info("Starting cleanup of old carts");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<Cart> oldCarts = cartRepository.findInactiveCartsOlderThan(cutoffDate);

        if (!oldCarts.isEmpty()) {
            cartRepository.deleteAll(oldCarts);
            log.info("Cleaned up {} old carts", oldCarts.size());
        }
    }

    private User findUserById(String userId) {
        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private Product findProductById(String productId) {
        return productRepository.findById(UUID.fromString(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
    }

    private CartItem findCartItemById(String cartItemId) {
        return cartItemRepository.findById(UUID.fromString(cartItemId))
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));
    }

    private void validateProductForCart(Product product, CartType cartType) {
        if (!product.getIsActive()) {
            throw new BusinessException("Product is not active");
        }
    }

    private void validateCartItemOwnership(CartItem cartItem, User user) {
        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new BusinessException("Cart item does not belong to the user");
        }
    }

    private Cart getOrCreateCart(User user, CartType cartType) {
        return cartRepository.findByUserAndCartTypeAndIsActive(user, cartType, true)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setCartType(cartType);
                    newCart.setIsActive(true);
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse createEmptyCartResponse(String userId, CartType cartType) {
        return CartResponse.builder()
                .cartId(null)
                .userId(userId)
                .cartType(cartType)
                .totalItems(0)
                .totalAmount(BigDecimal.ZERO)
                .items(List.of())
                .lastUpdated(LocalDateTime.now())
                .itemAlreadyExists(false)
                .build();
    }

    private CartResponse mapCartToResponse(Cart cart) {
        List<CartItemResponse> items = cart.getCartItems().stream()
                .map(this::mapCartItemToResponse)
                .collect(Collectors.toList());

        BigDecimal totalAmount = cart.getCartType() == CartType.CART ?
                items.stream()
                        .map(CartItemResponse::getTotalPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add) :
                BigDecimal.ZERO;

        return CartResponse.builder()
                .cartId(cart.getId().toString())
                .userId(cart.getUser().getId().toString())
                .cartType(cart.getCartType())
                .totalItems(cart.getTotalItems())
                .totalAmount(totalAmount)
                .items(items)
                .lastUpdated(cart.getUpdatedAt())
                .itemAlreadyExists(false)
                .build();
    }

    private CartItemResponse mapCartItemToResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();

        return CartItemResponse.builder()
                .cartItemId(cartItem.getId().toString())
                .productId(product.getId().toString())
                .productName(product.getName())
                .productSlug(product.getSlug())
                .productSku(product.getSku())
                .manufacturerName(product.getManufacturerName())
                .thumbnailUrl(product.getThumbnailObjectKey())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getPriceAtTime())
                .totalPrice(cartItem.getTotalPrice())
                .inStock(product.getIsActive())
                .addedAt(cartItem.getCreatedAt())
                .build();
    }

    private CartSummary mapCartToSummary(Cart cart) {
        BigDecimal totalAmount = cart.getCartType() == CartType.CART ?
                cart.getCartItems().stream()
                        .map(CartItem::getTotalPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add) :
                BigDecimal.ZERO;

        return CartSummary.builder()
                .cartType(cart.getCartType())
                .totalItems(cart.getTotalItems())
                .totalAmount(totalAmount)
                .lastUpdated(cart.getUpdatedAt())
                .build();
    }

    private String getEmptyCartMessage(CartType cartType) {
        return switch (cartType) {
            case CART -> "Cart is empty - no prices to synchronize";
            case WISHLIST -> "Wishlist is empty - no prices to synchronize";
        };
    }

    private String getPricesUpdatedMessage(CartType cartType) {
        return switch (cartType) {
            case CART -> "Cart prices synchronized successfully";
            case WISHLIST -> "Wishlist prices synchronized successfully";
        };
    }

    private String getPricesUpToDateMessage(CartType cartType) {
        return switch (cartType) {
            case CART -> "Cart prices are already up to date";
            case WISHLIST -> "Wishlist prices are already up to date";
        };
    }
}