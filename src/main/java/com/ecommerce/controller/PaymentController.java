package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.dto.PaymentDto;
import com.ecommerce.entity.Payment;
import com.ecommerce.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isPaymentOwner(#id)")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentById(@PathVariable Long id) {
        PaymentDto payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/transaction/{transactionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentByTransactionId(@PathVariable String transactionId) {
        PaymentDto payment = paymentService.getPaymentByTransactionId(transactionId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOrderOwner(#orderId)")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentDto payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<PaymentDto>>> getPaymentsByStatus(
            @PathVariable Payment.PaymentStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<PaymentDto> payments = paymentService.getPaymentsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentDto>> createPayment(
            @Valid @RequestBody PaymentDto.CreateRequest request) {
        PaymentDto payment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment created successfully", payment));
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<ApiResponse<PaymentDto>> processPayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentDto.ProcessRequest request) {
        PaymentDto payment = paymentService.processPayment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment processed", payment));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentDto>> refundPayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentDto.RefundRequest request) {
        PaymentDto payment = paymentService.refundPayment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment refunded", payment));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentDto>> cancelPayment(@PathVariable Long id) {
        PaymentDto payment = paymentService.cancelPayment(id);
        return ResponseEntity.ok(ApiResponse.success("Payment cancelled", payment));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<PaymentDto> payments = paymentService.getPaymentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/stats/total")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalCompletedPayments() {
        BigDecimal total = paymentService.getTotalCompletedPayments();
        return ResponseEntity.ok(ApiResponse.success(total));
    }

    @GetMapping("/stats/total/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalCompletedPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        BigDecimal total = paymentService.getTotalCompletedPaymentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(total));
    }

    @GetMapping("/stats/count/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> countPaymentsByStatus(@PathVariable Payment.PaymentStatus status) {
        long count = paymentService.countPaymentsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
