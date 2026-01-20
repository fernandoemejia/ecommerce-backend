package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.CartDto;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartDto>> getCart(@CurrentUser UserPrincipal currentUser) {
        CartDto cart = cartService.getCartByUserId(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartDto>> addItemToCart(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CartDto.AddItemRequest request) {
        CartDto cart = cartService.addItemToCart(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartDto>> updateCartItemQuantity(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long productId,
            @Valid @RequestBody CartDto.UpdateItemRequest request) {
        CartDto cart = cartService.updateCartItemQuantity(currentUser.getId(), productId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", cart));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartDto>> removeItemFromCart(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long productId) {
        CartDto cart = cartService.removeItemFromCart(currentUser.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<CartDto>> clearCart(@CurrentUser UserPrincipal currentUser) {
        CartDto cart = cartService.clearCart(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", cart));
    }
}
