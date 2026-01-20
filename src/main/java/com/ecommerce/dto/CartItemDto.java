package com.ecommerce.dto;

import com.ecommerce.entity.CartItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CartItemDto(
        Long id,
        Long productId,
        String productName,
        String productImageUrl,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        boolean inStock,
        Integer availableStock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CartItemDto fromEntity(CartItem cartItem) {
        return new CartItemDto(
                cartItem.getId(),
                cartItem.getProduct() != null ? cartItem.getProduct().getId() : null,
                cartItem.getProduct() != null ? cartItem.getProduct().getName() : null,
                cartItem.getProduct() != null ? cartItem.getProduct().getImageUrl() : null,
                cartItem.getQuantity(),
                cartItem.getUnitPrice(),
                cartItem.getTotalPrice(),
                cartItem.getProduct() != null && cartItem.getProduct().isInStock(),
                cartItem.getProduct() != null ? cartItem.getProduct().getStockQuantity() : 0,
                cartItem.getCreatedAt(),
                cartItem.getUpdatedAt()
        );
    }
}
