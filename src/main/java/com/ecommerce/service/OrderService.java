package com.ecommerce.service;

import com.ecommerce.dto.OrderDto;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository, UserService userService,
                        ProductService productService, CartService cartService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.productService = productService;
        this.cartService = cartService;
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderDto> getAllOrders(Pageable pageable) {
        Page<Order> page = orderRepository.findAll(pageable);
        return PageResponse.from(page, OrderDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(OrderDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(OrderDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
    }

    @Transactional(readOnly = true)
    public Order getOrderEntityById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderDto> getOrdersByUserId(Long userId, Pageable pageable) {
        Page<Order> page = orderRepository.findByUserId(userId, pageable);
        return PageResponse.from(page, OrderDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<OrderDto.OrderSummary> getUserOrderSummaries(Long userId) {
        return orderRepository.findAllByUserId(userId).stream()
                .map(OrderDto.OrderSummary::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderDto> getOrdersByStatus(Order.OrderStatus status, Pageable pageable) {
        Page<Order> page = orderRepository.findByStatus(status, pageable);
        return PageResponse.from(page, OrderDto::fromEntity);
    }

    public OrderDto createOrderFromCart(Long userId, OrderDto.CreateRequest request) {
        cartService.validateCartForCheckout(userId);

        User user = userService.getUserEntityById(userId);
        Cart cart = cartService.getCartEntityByUserId(userId);

        Order order = new Order(user);
        order.setShippingAddress(request.shippingAddress());
        order.setBillingAddress(request.billingAddress() != null ? request.billingAddress() : request.shippingAddress());
        order.setNotes(request.notes());

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            OrderItem orderItem = new OrderItem(order, product, cartItem.getQuantity());
            order.addOrderItem(orderItem);

            productService.reduceStock(product.getId(), cartItem.getQuantity());
        }

        order.calculateTotals();

        Order savedOrder = orderRepository.save(order);

        cartService.clearCart(userId);

        return OrderDto.fromEntity(savedOrder);
    }

    public OrderDto createOrder(Long userId, OrderDto.CreateRequest request) {
        User user = userService.getUserEntityById(userId);

        Order order = new Order(user);
        order.setShippingAddress(request.shippingAddress());
        order.setBillingAddress(request.billingAddress() != null ? request.billingAddress() : request.shippingAddress());
        order.setNotes(request.notes());

        for (OrderDto.OrderItemRequest itemRequest : request.items()) {
            Product product = productService.getProductEntityById(itemRequest.productId());

            if (!product.isActive()) {
                throw new IllegalStateException("Product '" + product.getName() + "' is not available");
            }
            if (product.getStockQuantity() < itemRequest.quantity()) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem(order, product, itemRequest.quantity());
            order.addOrderItem(orderItem);

            productService.reduceStock(product.getId(), itemRequest.quantity());
        }

        order.calculateTotals();

        Order savedOrder = orderRepository.save(order);
        return OrderDto.fromEntity(savedOrder);
    }

    public OrderDto updateOrderStatus(Long id, OrderDto.UpdateStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        if (request.status() != null) {
            validateStatusTransition(order.getStatus(), request.status());
            order.setStatus(request.status());
        }

        if (request.trackingNumber() != null) {
            order.setTrackingNumber(request.trackingNumber());
        }

        Order updatedOrder = orderRepository.save(order);
        return OrderDto.fromEntity(updatedOrder);
    }

    public OrderDto cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        if (order.getStatus() == Order.OrderStatus.SHIPPED ||
            order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel order that has been shipped or delivered");
        }

        for (OrderItem item : order.getOrderItems()) {
            productService.increaseStock(item.getProduct().getId(), item.getQuantity());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);
        return OrderDto.fromEntity(updatedOrder);
    }

    private void validateStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        if (currentStatus == Order.OrderStatus.CANCELLED || currentStatus == Order.OrderStatus.REFUNDED) {
            throw new IllegalStateException("Cannot change status of cancelled or refunded order");
        }

        if (currentStatus == Order.OrderStatus.DELIVERED && newStatus != Order.OrderStatus.REFUNDED) {
            throw new IllegalStateException("Delivered order can only be refunded");
        }
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByDateRange(startDate, endDate).stream()
                .map(OrderDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenueByStatus(Order.OrderStatus status) {
        BigDecimal total = orderRepository.sumTotalAmountByStatus(status);
        return total != null ? total : BigDecimal.ZERO;
    }
}
