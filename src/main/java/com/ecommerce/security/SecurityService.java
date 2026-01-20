package com.ecommerce.security;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.Payment;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.PaymentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public SecurityService(OrderRepository orderRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    public boolean isCurrentUser(Long userId) {
        UserPrincipal currentUser = getCurrentUser();
        return currentUser != null && currentUser.getId().equals(userId);
    }

    public boolean isOrderOwner(Long orderId) {
        UserPrincipal currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        return orderRepository.findById(orderId)
                .map(order -> order.getUser().getId().equals(currentUser.getId()))
                .orElse(false);
    }

    public boolean isOrderOwnerByNumber(String orderNumber) {
        UserPrincipal currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        return orderRepository.findByOrderNumber(orderNumber)
                .map(order -> order.getUser().getId().equals(currentUser.getId()))
                .orElse(false);
    }

    public boolean isPaymentOwner(Long paymentId) {
        UserPrincipal currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        return paymentRepository.findById(paymentId)
                .map(payment -> payment.getOrder().getUser().getId().equals(currentUser.getId()))
                .orElse(false);
    }

    private UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        return null;
    }
}
