package com.ecommerce.dto;

import com.ecommerce.entity.Payment;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentDto(
        Long id,
        String transactionId,
        Long orderId,
        String orderNumber,
        BigDecimal amount,
        Payment.PaymentMethod paymentMethod,
        Payment.PaymentStatus status,
        String paymentProvider,
        String providerReference,
        String failureReason,
        String currency,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime paidAt,
        LocalDateTime refundedAt
) {
    public static PaymentDto fromEntity(Payment payment) {
        return new PaymentDto(
                payment.getId(),
                payment.getTransactionId(),
                payment.getOrder() != null ? payment.getOrder().getId() : null,
                payment.getOrder() != null ? payment.getOrder().getOrderNumber() : null,
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getPaymentProvider(),
                payment.getProviderReference(),
                payment.getFailureReason(),
                payment.getCurrency(),
                payment.getCreatedAt(),
                payment.getUpdatedAt(),
                payment.getPaidAt(),
                payment.getRefundedAt()
        );
    }

    public record CreateRequest(
            @NotNull Long orderId,
            @NotNull Payment.PaymentMethod paymentMethod,
            String paymentProvider
    ) {}

    public record ProcessRequest(
            String providerReference
    ) {}

    public record RefundRequest(
            String reason
    ) {}
}
