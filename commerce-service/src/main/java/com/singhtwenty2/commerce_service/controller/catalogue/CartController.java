package com.singhtwenty2.commerce_service.controller.catalogue;

import com.singhtwenty2.commerce_service.data.dto.common.GlobalApiResponse;
import com.singhtwenty2.commerce_service.data.enums.CartType;
import com.singhtwenty2.commerce_service.service.catalogue.CartService;
import com.singhtwenty2.commerce_service.util.io.AuthenticationUtils;
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

import static com.singhtwenty2.commerce_service.data.dto.cart.CartDTO.*;
import static com.singhtwenty2.commerce_service.util.io.NetworkUtils.getClientIP;

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
        String userId = AuthenticationUtils.extractUserId(authentication, httpRequest, "add item to cart");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<CartResponse>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.info("Add item to cart request from IP: {} for user: {}, cartType: {}",
                getClientIP(httpRequest), userId, request.getCartType());

        CartResponse response = cartService.addItemToCart(userId, request);

        if (response.getItemAlreadyExists() != null && response.getItemAlreadyExists()) {
            String message = "Item already exists in wishlist";
            return ResponseEntity.ok(
                    GlobalApiResponse.<CartResponse>builder()
                            .success(true)
                            .message(message)
                            .data(response)
                            .build()
            );
        }

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
        String userId = AuthenticationUtils.extractUserId(authentication, httpRequest, "update cart item");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<CartResponse>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.info("Update cart item quantity request from IP: {} for user: {}, itemId: {}",
                getClientIP(httpRequest), userId, cartItemId);

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
        String userId = AuthenticationUtils.extractUserId(authentication, httpRequest, "remove item from cart");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<CartResponse>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.info("Remove item from cart request from IP: {} for user: {}, itemId: {}, cartType: {}",
                getClientIP(httpRequest), userId, cartItemId, cartType);

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
        String userId = AuthenticationUtils.extractUserId(authentication, httpRequest, "move item between carts");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<CartResponse>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.info("Move item between carts request from IP: {} for user: {}, itemId: {}, targetType: {}",
                getClientIP(httpRequest), userId, cartItemId, request.getTargetCartType());

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
        String userId = AuthenticationUtils.extractUserId(authentication, httpRequest, "get cart");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<CartResponse>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.debug("Get cart request from IP: {} for user: {}, cartType: {}",
                getClientIP(httpRequest), userId, cartType);

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
        String userId = AuthenticationUtils.extractUserId(authentication, httpRequest, "get cart summaries");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<List<CartSummary>>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.debug("Get all cart summaries request from IP: {} for user: {}",
                getClientIP(httpRequest), userId);

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
        String userId = AuthenticationUtils.extractUserId(authentication, httpRequest, "clear cart");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<CartResponse>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.info("Clear cart request from IP: {} for user: {}, cartType: {}",
                getClientIP(httpRequest), userId, cartType);

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
        String userId = AuthenticationUtils.extractUserId(authentication, httpRequest, "sync cart prices");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    GlobalApiResponse.<Void>builder()
                            .success(false)
                            .message("Unauthorized access")
                            .build()
            );
        }

        log.info("Sync cart prices request from IP: {} for user: {}, cartType: {}",
                getClientIP(httpRequest), userId, cartType);

        String message = cartService.syncCartWithProductPrices(userId, cartType);

        return ResponseEntity.ok(
                GlobalApiResponse.<Void>builder()
                        .success(true)
                        .message(message)
                        .build()
        );
    }
}