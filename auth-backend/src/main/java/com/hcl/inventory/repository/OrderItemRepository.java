package com.hcl.inventory.repository;

import com.hcl.inventory.entity.OrderItem;
import com.hcl.inventory.enums.OrderItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Integer> {

    boolean existsByOrderOrderIdAndProductProductId(
            Integer orderId,
            Integer productId
    );

    boolean existsByOrderOrderIdAndProductProductIdAndOrderItemIdNot(
            Integer orderId,
            Integer productId,
            Integer orderItemId
    );

    List<OrderItem> findByOrderOrderId(
            Integer orderId
    );

    List<OrderItem> findByProductProductId(
            Integer productId
    );

    List<OrderItem> findByWarehouseWarehouseId(
            Integer warehouseId
    );

    List<OrderItem> findByStatus(
            OrderItemStatus status
    );

    @Query("""
            SELECT COALESCE(SUM(oi.subtotal), 0)
            FROM OrderItem oi
            WHERE oi.order.orderId = :orderId
            """)
    BigDecimal calculateOrderTotal(Integer orderId);
}