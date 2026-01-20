package com.ecommerce.repository;

import com.ecommerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.id = :productId")
    List<OrderItem> findByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.id = :productId")
    Long getTotalQuantitySoldByProductId(@Param("productId") Long productId);

    @Query("SELECT oi.product.id, SUM(oi.quantity) as totalQuantity FROM OrderItem oi " +
            "GROUP BY oi.product.id ORDER BY totalQuantity DESC")
    List<Object[]> findBestSellingProducts();
}
