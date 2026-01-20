package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.OrderDto;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.security.CurrentUser;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<OrderDto>>> getAllOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<OrderDto> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOrderOwner(#id)")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(@PathVariable Long id) {
        OrderDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOrderOwnerByNumber(#orderNumber)")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderByOrderNumber(@PathVariable String orderNumber) {
        OrderDto order = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<PageResponse<OrderDto>>> getMyOrders(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<OrderDto> orders = orderService.getOrdersByUserId(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/my-orders/summary")
    public ResponseEntity<ApiResponse<List<OrderDto.OrderSummary>>> getMyOrderSummaries(
            @CurrentUser UserPrincipal currentUser) {
        List<OrderDto.OrderSummary> summaries = orderService.getUserOrderSummaries(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(summaries));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<OrderDto>>> getOrdersByStatus(
            @PathVariable Order.OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<OrderDto> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderDto>> createOrderFromCart(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody OrderDto.CreateRequest request) {
        OrderDto order = orderService.createOrderFromCart(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", order));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody OrderDto.CreateRequest request) {
        OrderDto order = orderService.createOrder(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", order));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderDto.UpdateStatusRequest request) {
        OrderDto order = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Order status updated", order));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOrderOwner(#id)")
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrder(@PathVariable Long id) {
        OrderDto order = orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<OrderDto> orders = orderService.getOrdersByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/stats/count/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> countOrdersByStatus(@PathVariable Order.OrderStatus status) {
        long count = orderService.countOrdersByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
