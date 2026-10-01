package com.hcl.inventory.controller;

import com.hcl.inventory.entity.PurchaseOrder;
import com.hcl.inventory.enums.PurchaseOrderStatus;
import com.hcl.inventory.service.PurchaseOrderService;
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
@RequestMapping("/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @Autowired
    public PurchaseOrderController(
            PurchaseOrderService purchaseOrderService
    ) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrder>>
    getAllPurchaseOrders() {

        return ResponseEntity.ok(
                purchaseOrderService
                        .getAllPurchaseOrders()
        );
    }

    @GetMapping("/{poId}")
    public ResponseEntity<PurchaseOrder>
    getPurchaseOrderById(
            @PathVariable Integer poId
    ) {

        return ResponseEntity.ok(
                purchaseOrderService
                        .getPurchaseOrderById(poId)
        );
    }

    @PostMapping
    public ResponseEntity<PurchaseOrder>
    addPurchaseOrder(
            @Valid @RequestBody PurchaseOrder purchaseOrder
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        purchaseOrderService
                                .addPurchaseOrder(
                                        purchaseOrder
                                )
                );
    }

    @PutMapping("/{poId}")
    public ResponseEntity<PurchaseOrder>
    updatePurchaseOrder(
            @PathVariable Integer poId,
            @Valid @RequestBody PurchaseOrder purchaseOrder
    ) {

        return ResponseEntity.ok(
                purchaseOrderService
                        .updatePurchaseOrder(
                                poId,
                                purchaseOrder
                        )
        );
    }

    @PostMapping("/{poId}/place")
    public ResponseEntity<PurchaseOrder>
    placePurchaseOrder(
            @PathVariable Integer poId
    ) {

        return ResponseEntity.ok(
                purchaseOrderService
                        .placePurchaseOrder(poId)
        );
    }

    @PostMapping("/{poId}/cancel")
    public ResponseEntity<PurchaseOrder>
    cancelPurchaseOrder(
            @PathVariable Integer poId
    ) {

        return ResponseEntity.ok(
                purchaseOrderService
                        .cancelPurchaseOrder(poId)
        );
    }

    @PostMapping("/{poId}/receive")
    public ResponseEntity<PurchaseOrder>
    receivePurchaseOrder(
            @PathVariable Integer poId,
            @RequestParam Integer quantity
    ) {

        return ResponseEntity.ok(
                purchaseOrderService
                        .receivePurchaseOrder(
                                poId,
                                quantity
                        )
        );
    }

    @DeleteMapping("/{poId}")
    public ResponseEntity<Void>
    deletePurchaseOrder(
            @PathVariable Integer poId
    ) {

        purchaseOrderService
                .deletePurchaseOrder(poId);

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<PurchaseOrder>>
    searchByPoNumber(
            @RequestParam String poNumber
    ) {

        return ResponseEntity.ok(
                purchaseOrderService
                        .searchByPoNumber(poNumber)
        );
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<PurchaseOrder>>
    getBySupplier(
            @PathVariable Integer supplierId
    ) {

        return ResponseEntity.ok(
                purchaseOrderService
                        .getBySupplier(supplierId)
        );
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<PurchaseOrder>>
    getByProduct(
            @PathVariable Integer productId
    ) {

        return ResponseEntity.ok(
                purchaseOrderService
                        .getByProduct(productId)
        );
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<PurchaseOrder>>
    getByWarehouse(
            @PathVariable Integer warehouseId
    ) {

        return ResponseEntity.ok(
                purchaseOrderService
                        .getByWarehouse(warehouseId)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PurchaseOrder>>
    getByStatus(
            @PathVariable PurchaseOrderStatus status
    ) {

        return ResponseEntity.ok(
                purchaseOrderService
                        .getByStatus(status)
        );
    }
}