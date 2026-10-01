package com.hcl.inventory.repository;

import com.hcl.inventory.entity.Order;
import com.hcl.inventory.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository
        extends JpaRepository<Order, Integer> {

    boolean existsByOrderNumber(String orderNumber);

    boolean existsByOrderNumberAndOrderIdNot(
            String orderNumber,
            Integer orderId
    );

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByOrderNumberContainingIgnoreCase(
            String orderNumber
    );

    List<Order> findByCustomerNameContainingIgnoreCase(
            String customerName
    );

    List<Order> findByCustomerEmailIgnoreCase(
            String customerEmail
    );

    List<Order> findByStatus(
            OrderStatus status
    );
}