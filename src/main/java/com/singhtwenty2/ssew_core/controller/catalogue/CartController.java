package com.singhtwenty2.ssew_core.controller.catalogue;

import com.singhtwenty2.ssew_core.data.dto.common.GlobalApiResponse;
import com.singhtwenty2.ssew_core.data.enums.CartType;
import com.singhtwenty2.ssew_core.security.PrincipalUser;
import com.singhtwenty2.ssew_core.service.catalogue.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.singhtwenty2.ssew_core.data.dto.cart.CartDTO.*;
import static com.singhtwenty2.ssew_core.util.io.NetworkUtils.getClientIP;

@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<CartResponse>> addItemToCart(
            @Valid @RequestBody AddItemRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        PrincipalUser principalUser = validateAuthentication(authentication, httpRequest, "add item to cart");
        if (principalUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<CartResponse>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.info("Add item to cart request from IP: {} for user: {}, cartType: {}",
                getClientIP(httpRequest), principalUser.getUserId(), request.getCartType());

        String userId = principalUser.getUserId().toString();
        CartResponse response = cartService.addItemToCart(userId, request);

        String message = request.getCartType() == CartType.CART ?
                "Item added to cart successfully" : "Item added to wishlist successfully";

        return ResponseEntity.status(HttpStatus.CREATED).body(
                GlobalApiResponse.<CartResponse>builder()
                        .success(true)
                        .message(message)
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<CartResponse>> updateCartItemQuantity(
            @PathVariable String cartItemId,
            @Valid @RequestBody UpdateItemRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        PrincipalUser principalUser = validateAuthentication(authentication, httpRequest, "update cart item");
        if (principalUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<CartResponse>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.info("Update cart item quantity request from IP: {} for user: {}, itemId: {}",
                getClientIP(httpRequest), principalUser.getUserId(), cartItemId);

        String userId = principalUser.getUserId().toString();
        CartResponse response = cartService.updateCartItemQuantity(userId, cartItemId, request);

        return ResponseEntity.ok(
                GlobalApiResponse.<CartResponse>builder()
                        .success(true)
                        .message("Cart item quantity updated successfully")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<CartResponse>> removeItemFromCart(
            @PathVariable String cartItemId,
            @RequestParam CartType cartType,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        PrincipalUser principalUser = validateAuthentication(authentication, httpRequest, "remove item from cart");
        if (principalUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<CartResponse>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.info("Remove item from cart request from IP: {} for user: {}, itemId: {}, cartType: {}",
                getClientIP(httpRequest), principalUser.getUserId(), cartItemId, cartType);

        String userId = principalUser.getUserId().toString();
        CartResponse response = cartService.removeItemFromCart(userId, cartItemId, cartType);

        String message = cartType == CartType.CART ?
                "Item removed from cart successfully" : "Item removed from wishlist successfully";

        return ResponseEntity.ok(
                GlobalApiResponse.<CartResponse>builder()
                        .success(true)
                        .message(message)
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/items/{cartItemId}/move")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<CartResponse>> moveItemBetweenCarts(
            @PathVariable String cartItemId,
            @Valid @RequestBody MoveItemRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        PrincipalUser principalUser = validateAuthentication(authentication, httpRequest, "move item between carts");
        if (principalUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<CartResponse>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.info("Move item between carts request from IP: {} for user: {}, itemId: {}, targetType: {}",
                getClientIP(httpRequest), principalUser.getUserId(), cartItemId, request.getTargetCartType());

        String userId = principalUser.getUserId().toString();
        CartResponse response = cartService.moveItemBetweenCarts(userId, cartItemId, request);

        String message = request.getTargetCartType() == CartType.CART ?
                "Item moved to cart successfully" : "Item moved to wishlist successfully";

        return ResponseEntity.ok(
                GlobalApiResponse.<CartResponse>builder()
                        .success(true)
                        .message(message)
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{cartType}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<CartResponse>> getCart(
            @PathVariable CartType cartType,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        PrincipalUser principalUser = validateAuthentication(authentication, httpRequest, "get cart");
        if (principalUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<CartResponse>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.debug("Get cart request from IP: {} for user: {}, cartType: {}",
                getClientIP(httpRequest), principalUser.getUserId(), cartType);

        String userId = principalUser.getUserId().toString();
        CartResponse response = cartService.getCart(userId, cartType);

        String message = cartType == CartType.CART ?
                "Cart retrieved successfully" : "Wishlist retrieved successfully";

        return ResponseEntity.ok(
                GlobalApiResponse.<CartResponse>builder()
                        .success(true)
                        .message(message)
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<List<CartSummary>>> getAllCartSummaries(
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        PrincipalUser principalUser = validateAuthentication(authentication, httpRequest, "get cart summaries");
        if (principalUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<List<CartSummary>>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.debug("Get all cart summaries request from IP: {} for user: {}",
                getClientIP(httpRequest), principalUser.getUserId());

        String userId = principalUser.getUserId().toString();
        List<CartSummary> response = cartService.getAllCartSummaries(userId);

        return ResponseEntity.ok(
                GlobalApiResponse.<List<CartSummary>>builder()
                        .success(true)
                        .message("Cart summaries retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/{cartType}/clear")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<CartResponse>> clearCart(
            @PathVariable CartType cartType,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        PrincipalUser principalUser = validateAuthentication(authentication, httpRequest, "clear cart");
        if (principalUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<CartResponse>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.info("Clear cart request from IP: {} for user: {}, cartType: {}",
                getClientIP(httpRequest), principalUser.getUserId(), cartType);

        String userId = principalUser.getUserId().toString();
        CartResponse response = cartService.clearCart(userId, cartType);

        String message = cartType == CartType.CART ?
                "Cart cleared successfully" : "Wishlist cleared successfully";

        return ResponseEntity.ok(
                GlobalApiResponse.<CartResponse>builder()
                        .success(true)
                        .message(message)
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/sync-prices/{cartType}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<GlobalApiResponse<Void>> syncCartPrices(
            @PathVariable CartType cartType,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        PrincipalUser principalUser = validateAuthentication(authentication, httpRequest, "sync cart prices");
        if (principalUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<Void>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.info("Sync cart prices request from IP: {} for user: {}, cartType: {}",
                getClientIP(httpRequest), principalUser.getUserId(), cartType);

        String userId = principalUser.getUserId().toString();
        String message = cartService.syncCartWithProductPrices(userId, cartType);

        return ResponseEntity.ok(
                GlobalApiResponse.<Void>builder()
                        .success(true)
                        .message(message)
                        .build()
        );
    }

    private PrincipalUser validateAuthentication(Authentication authentication, HttpServletRequest request, String operation) {
        if (authentication == null || !(authentication.getPrincipal() instanceof PrincipalUser principalUser)) {
            log.warn("Unauthorized {} attempt from IP: {}", operation, getClientIP(request));
            return null;
        }
        return principalUser;
    }
}