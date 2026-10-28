package com.hcl.inventory.repository;

import com.hcl.inventory.entity.Order;
import com.hcl.inventory.enums.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.orderId = :orderId")
    Optional<Order> findByIdWithLock(@Param("orderId") Integer orderId);

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
