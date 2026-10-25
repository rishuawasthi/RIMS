package com.hcl.inventory.controller;

import com.hcl.inventory.entity.StockMovement;
import com.hcl.inventory.service.StockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stock-movements")
@Tag(name = "Stock Movements Ledger", description = "Audit trail for all inventory movements and stock adjustments")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @GetMapping
    @Operation(summary = "Get recent stock movements", description = "Retrieves the 50 most recent stock movements across all warehouses and stores")
    public ResponseEntity<List<StockMovement>> getRecentMovements() {
        return ResponseEntity.ok(stockMovementService.getRecentMovements());
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get movements by product", description = "Filter movement history by product ID")
    public ResponseEntity<List<StockMovement>> getByProduct(@PathVariable Integer productId) {
        return ResponseEntity.ok(stockMovementService.getMovementsByProduct(productId));
    }

    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "Get movements by warehouse", description = "Filter movement history by warehouse ID")
    public ResponseEntity<List<StockMovement>> getByWarehouse(@PathVariable Integer warehouseId) {
        return ResponseEntity.ok(stockMovementService.getMovementsByWarehouse(warehouseId));
    }

    @PostMapping
    @Operation(summary = "Record stock movement", description = "Manually record an inventory transfer or audit adjustment")
    public ResponseEntity<StockMovement> recordMovement(@Valid @RequestBody StockMovement movement) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockMovementService.recordMovement(movement));
    }
}
