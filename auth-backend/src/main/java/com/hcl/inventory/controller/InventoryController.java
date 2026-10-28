package com.hcl.inventory.controller;

import com.hcl.inventory.dto.StockTransferRequest;
import com.hcl.inventory.entity.Inventory;
import com.hcl.inventory.service.InventoryService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory")
@Tag(name = "Inventory Management", description = "Multi-warehouse real-time stock levels, reorder alerts, and inter-warehouse transfers")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(
            InventoryService inventoryService
    ) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @Operation(summary = "List all inventory records")
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(
                inventoryService.getAllInventory()
        );
    }

    @GetMapping("/{inventoryId}")
    @Operation(summary = "Get inventory record by ID")
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
    @Operation(summary = "Create inventory record")
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
    @Operation(summary = "Update inventory record")
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
    @Operation(summary = "Delete inventory record")
    public ResponseEntity<Void> deleteInventory(
            @PathVariable Integer inventoryId
    ) {
        inventoryService.deleteInventory(
                inventoryId
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "Get inventory by warehouse ID")
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
    @Operation(summary = "Get inventory by product ID")
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
    @Operation(summary = "Get low stock inventory items below reorder threshold")
    public ResponseEntity<List<Inventory>> getLowStockInventory() {
        return ResponseEntity.ok(
                inventoryService.getLowStockInventory()
        );
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer stock between warehouses")
    public ResponseEntity<Map<String, Object>> transferStock(
            @Valid @RequestBody StockTransferRequest request
    ) {
        return ResponseEntity.ok(
                inventoryService.transferStock(request)
        );
    }
}
