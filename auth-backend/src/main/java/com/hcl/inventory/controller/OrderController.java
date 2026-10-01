package com.hcl.inventory.controller;

import com.hcl.inventory.entity.Order;
import com.hcl.inventory.enums.OrderStatus;
import com.hcl.inventory.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(
            OrderService orderService
    ) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {

        return ResponseEntity.ok(
                orderService.getAllOrders()
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(
            @PathVariable Integer orderId
    ) {

        return ResponseEntity.ok(
                orderService.getOrderById(orderId)
        );
    }

    @PostMapping
    public ResponseEntity<Order> addOrder(
            @Valid @RequestBody Order order
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        orderService.addOrder(order)
                );
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<Order> updateOrder(
            @PathVariable Integer orderId,
            @Valid @RequestBody Order order
    ) {

        return ResponseEntity.ok(
                orderService.updateOrder(
                        orderId,
                        order
                )
        );
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Order> confirmOrder(
            @PathVariable Integer orderId
    ) {

        return ResponseEntity.ok(
                orderService.confirmOrder(orderId)
        );
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable Integer orderId
    ) {

        return ResponseEntity.ok(
                orderService.cancelOrder(orderId)
        );
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable Integer orderId
    ) {

        orderService.deleteOrder(orderId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/order-number")
    public ResponseEntity<List<Order>> searchByOrderNumber(
            @RequestParam String orderNumber
    ) {

        return ResponseEntity.ok(
                orderService.searchByOrderNumber(
                        orderNumber
                )
        );
    }

    @GetMapping("/search/customer")
    public ResponseEntity<List<Order>> searchByCustomerName(
            @RequestParam String name
    ) {

        return ResponseEntity.ok(
                orderService.searchByCustomerName(name)
        );
    }

    @GetMapping("/customer/email")
    public ResponseEntity<List<Order>> getByCustomerEmail(
            @RequestParam String email
    ) {

        return ResponseEntity.ok(
                orderService.getByCustomerEmail(email)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getByStatus(
            @PathVariable OrderStatus status
    ) {

        return ResponseEntity.ok(
                orderService.getByStatus(status)
        );
    }
}