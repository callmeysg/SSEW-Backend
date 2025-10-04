package com.singhtwenty2.commerce_service.service.catalogue;

import com.singhtwenty2.commerce_service.data.enums.CartType;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.singhtwenty2.commerce_service.data.dto.cart.CartDTO.*;

@Service
public interface CartService {
    CartResponse addItemToCart(String userId, AddItemRequest request);

    CartResponse updateCartItemQuantity(String userId, String cartItemId, UpdateItemRequest request);

    CartResponse removeItemFromCart(String userId, String cartItemId, CartType cartType);

    CartResponse moveItemBetweenCarts(String userId, String cartItemId, MoveItemRequest request);

    CartResponse getCart(String userId, CartType cartType);

    List<CartSummary> getAllCartSummaries(String userId);

    CartResponse clearCart(String userId, CartType cartType);

    String syncCartWithProductPrices(String userId, CartType cartType);
}
