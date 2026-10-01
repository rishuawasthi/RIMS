package com.hcl.inventory.controller;

import com.hcl.inventory.entity.Inventory;
import com.hcl.inventory.service.InventoryService;
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
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(
            InventoryService inventoryService
    ) {

        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {

        return ResponseEntity.ok(
                inventoryService.getAllInventory()
        );
    }

    @GetMapping("/{inventoryId}")
    public ResponseEntity<Inventory> getInventoryById(
            @PathVariable Integer inventoryId
    ) {

        return ResponseEntity.ok(
                inventoryService.getInventoryById(
                        inventoryId
                )
        );
    }

    @PostMapping
    public ResponseEntity<Inventory> addInventory(
            @Valid @RequestBody Inventory inventory
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        inventoryService.addInventory(
                                inventory
                        )
                );
    }

    @PutMapping("/{inventoryId}")
    public ResponseEntity<Inventory> updateInventory(
            @PathVariable Integer inventoryId,
            @Valid @RequestBody Inventory inventory
    ) {

        return ResponseEntity.ok(
                inventoryService.updateInventory(
                        inventoryId,
                        inventory
                )
        );
    }

    @DeleteMapping("/{inventoryId}")
    public ResponseEntity<Void> deleteInventory(
            @PathVariable Integer inventoryId
    ) {

        inventoryService.deleteInventory(
                inventoryId
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<Inventory>> getInventoryByWarehouse(
            @PathVariable Integer warehouseId
    ) {

        return ResponseEntity.ok(
                inventoryService.getInventoryByWarehouse(
                        warehouseId
                )
        );
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Inventory>> getInventoryByProduct(
            @PathVariable Integer productId
    ) {

        return ResponseEntity.ok(
                inventoryService.getInventoryByProduct(
                        productId
                )
        );
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Inventory>> getLowStockInventory() {

        return ResponseEntity.ok(
                inventoryService.getLowStockInventory()
        );
    }
}