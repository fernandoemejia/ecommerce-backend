package com.ecommerce.dto;

import com.ecommerce.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemDto(
        Long id,
        Long productId,
        String productName,
        String productSku,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal discountAmount,
        BigDecimal totalPrice
) {
    public static OrderItemDto fromEntity(OrderItem orderItem) {
        return new OrderItemDto(
                orderItem.getId(),
                orderItem.getProduct() != null ? orderItem.getProduct().getId() : null,
                orderItem.getProductName(),
                orderItem.getProductSku(),
                orderItem.getQuantity(),
                orderItem.getUnitPrice(),
                orderItem.getDiscountAmount(),
                orderItem.getTotalPrice()
        );
    }
}
