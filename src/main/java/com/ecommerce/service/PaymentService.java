package com.ecommerce.service;

import com.ecommerce.dto.PageResponse;
import com.ecommerce.dto.PaymentDto;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Payment;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;

    public PaymentService(PaymentRepository paymentRepository, OrderService orderService) {
        this.paymentRepository = paymentRepository;
        this.orderService = orderService;
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .map(PaymentDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .map(PaymentDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionId", transactionId));
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(PaymentDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentDto> getPaymentsByStatus(Payment.PaymentStatus status, Pageable pageable) {
        Page<Payment> page = paymentRepository.findByStatus(status, pageable);
        return PageResponse.from(page, PaymentDto::fromEntity);
    }

    public PaymentDto createPayment(PaymentDto.CreateRequest request) {
        Order order = orderService.getOrderEntityById(request.orderId());

        if (paymentRepository.findByOrderId(request.orderId()).isPresent()) {
            throw new IllegalStateException("Payment already exists for this order");
        }

        if (order.getStatus() == Order.OrderStatus.CANCELLED ||
            order.getStatus() == Order.OrderStatus.REFUNDED) {
            throw new IllegalStateException("Cannot create payment for cancelled or refunded order");
        }

        Payment payment = new Payment(order, order.getTotalAmount(), request.paymentMethod());
        if (request.paymentProvider() != null) {
            payment.setPaymentProvider(request.paymentProvider());
        }

        order.setPayment(payment);

        Payment savedPayment = paymentRepository.save(payment);
        return PaymentDto.fromEntity(savedPayment);
    }

    public PaymentDto processPayment(Long paymentId, PaymentDto.ProcessRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is not in pending status");
        }

        payment.setStatus(Payment.PaymentStatus.PROCESSING);

        // Simulate payment processing
        boolean success = simulatePaymentProcessing(payment);

        if (success) {
            payment.markAsCompleted(request.providerReference());

            Order order = payment.getOrder();
            order.setStatus(Order.OrderStatus.CONFIRMED);
        } else {
            payment.markAsFailed("Payment processing failed");
        }

        Payment updatedPayment = paymentRepository.save(payment);
        return PaymentDto.fromEntity(updatedPayment);
    }

    private boolean simulatePaymentProcessing(Payment payment) {
        // In a real application, this would integrate with payment gateway
        return true;
    }

    public PaymentDto refundPayment(Long paymentId, PaymentDto.RefundRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only completed payments can be refunded");
        }

        payment.markAsRefunded();
        if (request.reason() != null) {
            payment.setFailureReason("Refund reason: " + request.reason());
        }

        Order order = payment.getOrder();
        order.setStatus(Order.OrderStatus.REFUNDED);

        Payment updatedPayment = paymentRepository.save(payment);
        return PaymentDto.fromEntity(updatedPayment);
    }

    public PaymentDto cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (payment.getStatus() == Payment.PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed payment, use refund instead");
        }

        if (payment.getStatus() == Payment.PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Payment is already refunded");
        }

        payment.setStatus(Payment.PaymentStatus.CANCELLED);

        Payment updatedPayment = paymentRepository.save(payment);
        return PaymentDto.fromEntity(updatedPayment);
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findByDateRange(startDate, endDate).stream()
                .map(PaymentDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalCompletedPayments() {
        BigDecimal total = paymentRepository.getTotalCompletedPayments();
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalCompletedPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal total = paymentRepository.getTotalCompletedPaymentsByDateRange(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public long countPaymentsByStatus(Payment.PaymentStatus status) {
        return paymentRepository.countByStatus(status);
    }
}
