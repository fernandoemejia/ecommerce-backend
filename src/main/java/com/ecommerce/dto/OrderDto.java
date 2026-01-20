package com.ecommerce.dto;

import com.ecommerce.entity.Order;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(
        Long id,
        String orderNumber,
        Long userId,
        String userEmail,
        List<OrderItemDto> orderItems,
        PaymentDto payment,
        Order.OrderStatus status,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal shippingAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String shippingAddress,
        String billingAddress,
        String notes,
        String trackingNumber,
        int totalItems,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt
) {
    public static OrderDto fromEntity(Order order) {
        List<OrderItemDto> itemDtos = order.getOrderItems() != null
                ? order.getOrderItems().stream()
                    .map(OrderItemDto::fromEntity)
                    .toList()
                : List.of();

        PaymentDto paymentDto = order.getPayment() != null
                ? PaymentDto.fromEntity(order.getPayment())
                : null;

        return new OrderDto(
                order.getId(),
                order.getOrderNumber(),
                order.getUser() != null ? order.getUser().getId() : null,
                order.getUser() != null ? order.getUser().getEmail() : null,
                itemDtos,
                paymentDto,
                order.getStatus(),
                order.getSubtotal(),
                order.getTaxAmount(),
                order.getShippingAmount(),
                order.getDiscountAmount(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getBillingAddress(),
                order.getNotes(),
                order.getTrackingNumber(),
                order.getTotalItems(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getShippedAt(),
                order.getDeliveredAt()
        );
    }

    public record CreateRequest(
            @NotEmpty List<OrderItemRequest> items,
            @NotBlank String shippingAddress,
            String billingAddress,
            String notes
    ) {}

    public record OrderItemRequest(
            Long productId,
            Integer quantity
    ) {}

    public record UpdateStatusRequest(
            Order.OrderStatus status,
            String trackingNumber
    ) {}

    public record OrderSummary(
            Long id,
            String orderNumber,
            Order.OrderStatus status,
            BigDecimal totalAmount,
            int totalItems,
            LocalDateTime createdAt
    ) {
        public static OrderSummary fromEntity(Order order) {
            return new OrderSummary(
                    order.getId(),
                    order.getOrderNumber(),
                    order.getStatus(),
                    order.getTotalAmount(),
                    order.getTotalItems(),
                    order.getCreatedAt()
            );
        }
    }
}
