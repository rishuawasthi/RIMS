package com.hcl.inventory.controller;

import com.hcl.inventory.entity.Order;
import com.hcl.inventory.enums.OrderStatus;
import com.hcl.inventory.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Customer Orders", description = "Order intake, stock allocation, pick, pack, ship, and fulfillment lifecycle")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "List all customer orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<Order> getOrderById(@PathVariable Integer orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @PostMapping
    @Operation(summary = "Create draft customer order")
    public ResponseEntity<Order> addOrder(@Valid @RequestBody Order order) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.addOrder(order));
    }

    @PutMapping("/{orderId}")
    @Operation(summary = "Update customer order details (DRAFT only)")
    public ResponseEntity<Order> updateOrder(@PathVariable Integer orderId, @Valid @RequestBody Order order) {
        return ResponseEntity.ok(orderService.updateOrder(orderId, order));
    }

    @PostMapping("/{orderId}/confirm")
    @Operation(summary = "Confirm order and allocate/reserve stock")
    public ResponseEntity<Order> confirmOrder(@PathVariable Integer orderId) {
        return ResponseEntity.ok(orderService.confirmOrder(orderId));
    }

    @PostMapping("/{orderId}/pick")
    @Operation(summary = "Pick order items in warehouse")
    public ResponseEntity<Order> pickOrder(@PathVariable Integer orderId) {
        return ResponseEntity.ok(orderService.pickOrder(orderId));
    }

    @PostMapping("/{orderId}/pack")
    @Operation(summary = "Pack order items for dispatch")
    public ResponseEntity<Order> packOrder(@PathVariable Integer orderId) {
        return ResponseEntity.ok(orderService.packOrder(orderId));
    }

    @PostMapping("/{orderId}/ship")
    @Operation(summary = "Ship order, deduct physical & reserved stock, write outbound ledger entry")
    public ResponseEntity<Order> shipOrder(@PathVariable Integer orderId) {
        return ResponseEntity.ok(orderService.shipOrder(orderId));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order and release reserved stock")
    public ResponseEntity<Order> cancelOrder(@PathVariable Integer orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "Delete order (DRAFT or CANCELLED only)")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/order-number")
    @Operation(summary = "Search orders by order number")
    public ResponseEntity<List<Order>> searchByOrderNumber(@RequestParam String orderNumber) {
        return ResponseEntity.ok(orderService.searchByOrderNumber(orderNumber));
    }

    @GetMapping("/search/customer")
    @Operation(summary = "Search orders by customer name")
    public ResponseEntity<List<Order>> searchByCustomerName(@RequestParam String name) {
        return ResponseEntity.ok(orderService.searchByCustomerName(name));
    }

    @GetMapping("/customer/email")
    @Operation(summary = "Get orders by customer email")
    public ResponseEntity<List<Order>> getByCustomerEmail(@RequestParam String email) {
        return ResponseEntity.ok(orderService.getByCustomerEmail(email));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Filter orders by status")
    public ResponseEntity<List<Order>> getByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getByStatus(status));
    }
}
