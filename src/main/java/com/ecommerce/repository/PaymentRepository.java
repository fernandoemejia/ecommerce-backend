package com.ecommerce.repository;

import com.ecommerce.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String transactionId);

    Optional<Payment> findByOrderId(Long orderId);

    @Query("SELECT p FROM Payment p WHERE p.status = :status")
    Page<Payment> findByStatus(@Param("status") Payment.PaymentStatus status, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.paymentMethod = :method")
    List<Payment> findByPaymentMethod(@Param("method") Payment.PaymentMethod method);

    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByStatusAndDateRange(@Param("status") Payment.PaymentStatus status,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED'")
    java.math.BigDecimal getTotalCompletedPayments();

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paidAt BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalCompletedPaymentsByDateRange(@Param("startDate") LocalDateTime startDate,
                                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    long countByStatus(@Param("status") Payment.PaymentStatus status);
}
