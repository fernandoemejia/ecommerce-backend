package com.ecommerce.dto;

import com.ecommerce.entity.Cart;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CartDto(
        Long id,
        Long userId,
        List<CartItemDto> items,
        BigDecimal totalAmount,
        int totalItems,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CartDto fromEntity(Cart cart) {
        List<CartItemDto> itemDtos = cart.getCartItems() != null
                ? cart.getCartItems().stream()
                    .map(CartItemDto::fromEntity)
                    .toList()
                : List.of();

        return new CartDto(
                cart.getId(),
                cart.getUser() != null ? cart.getUser().getId() : null,
                itemDtos,
                cart.getTotalAmount(),
                cart.getTotalItems(),
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }

    public record AddItemRequest(
            @NotNull Long productId,
            @NotNull @Min(1) Integer quantity
    ) {}

    public record UpdateItemRequest(
            @NotNull @Min(1) Integer quantity
    ) {}
}
