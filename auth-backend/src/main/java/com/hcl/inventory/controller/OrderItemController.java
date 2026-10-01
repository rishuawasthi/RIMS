package com.hcl.inventory.controller;

import com.hcl.inventory.entity.OrderItem;
import com.hcl.inventory.enums.OrderItemStatus;
import com.hcl.inventory.service.OrderItemService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/order-items")
public class OrderItemController {

    private final OrderItemService orderItemService;

    @Autowired
    public OrderItemController(
            OrderItemService orderItemService
    ) {
        this.orderItemService = orderItemService;
    }

    @GetMapping
    public ResponseEntity<List<OrderItem>> getAllOrderItems() {

        return ResponseEntity.ok(
                orderItemService.getAllOrderItems()
        );
    }

    @GetMapping("/{orderItemId}")
    public ResponseEntity<OrderItem> getOrderItemById(
            @PathVariable Integer orderItemId
    ) {

        return ResponseEntity.ok(
                orderItemService.getOrderItemById(
                        orderItemId
                )
        );
    }

    @PostMapping
    public ResponseEntity<OrderItem> addOrderItem(
            @Valid @RequestBody OrderItem orderItem
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        orderItemService.addOrderItem(
                                orderItem
                        )
                );
    }

    @PutMapping("/{orderItemId}")
    public ResponseEntity<OrderItem> updateOrderItem(
            @PathVariable Integer orderItemId,
            @Valid @RequestBody OrderItem orderItem
    ) {

        return ResponseEntity.ok(
                orderItemService.updateOrderItem(
                        orderItemId,
                        orderItem
                )
        );
    }

    @PostMapping("/{orderItemId}/cancel")
    public ResponseEntity<OrderItem> cancelOrderItem(
            @PathVariable Integer orderItemId
    ) {

        return ResponseEntity.ok(
                orderItemService.cancelOrderItem(
                        orderItemId
                )
        );
    }

    @DeleteMapping("/{orderItemId}")
    public ResponseEntity<Void> deleteOrderItem(
            @PathVariable Integer orderItemId
    ) {

        orderItemService.deleteOrderItem(
                orderItemId
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItem>> getItemsByOrder(
            @PathVariable Integer orderId
    ) {

        return ResponseEntity.ok(
                orderItemService.getItemsByOrder(
                        orderId
                )
        );
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<OrderItem>> getItemsByProduct(
            @PathVariable Integer productId
    ) {

        return ResponseEntity.ok(
                orderItemService.getItemsByProduct(
                        productId
                )
        );
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<OrderItem>> getItemsByWarehouse(
            @PathVariable Integer warehouseId
    ) {

        return ResponseEntity.ok(
                orderItemService.getItemsByWarehouse(
                        warehouseId
                )
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderItem>> getItemsByStatus(
            @PathVariable OrderItemStatus status
    ) {

        return ResponseEntity.ok(
                orderItemService.getItemsByStatus(
                        status
                )
        );
    }
}