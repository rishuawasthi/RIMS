package com.hcl.inventory.controller;

import com.hcl.inventory.entity.Warehouse;
import com.hcl.inventory.service.WarehouseService;
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
@RequestMapping("/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @Autowired
    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    public ResponseEntity<List<Warehouse>> getAllWarehouses() {

        return ResponseEntity.ok(
                warehouseService.getAllWarehouses()
        );
    }

    @GetMapping("/{warehouseId}")
    public ResponseEntity<Warehouse> getWarehouseById(
            @PathVariable Integer warehouseId
    ) {

        return ResponseEntity.ok(
                warehouseService.getWarehouseById(warehouseId)
        );
    }

    @PostMapping
    public ResponseEntity<Warehouse> addWarehouse(
            @Valid @RequestBody Warehouse warehouse
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        warehouseService.addWarehouse(warehouse)
                );
    }

    @PutMapping("/{warehouseId}")
    public ResponseEntity<Warehouse> updateWarehouse(
            @PathVariable Integer warehouseId,
            @Valid @RequestBody Warehouse warehouse
    ) {

        return ResponseEntity.ok(
                warehouseService.updateWarehouse(
                        warehouseId,
                        warehouse
                )
        );
    }

    @DeleteMapping("/{warehouseId}")
    public ResponseEntity<Void> deleteWarehouse(
            @PathVariable Integer warehouseId
    ) {

        warehouseService.deleteWarehouse(warehouseId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<Warehouse>> searchByName(
            @RequestParam String name
    ) {

        return ResponseEntity.ok(
                warehouseService.searchByName(name)
        );
    }

    @GetMapping("/search/city")
    public ResponseEntity<List<Warehouse>> searchByCity(
            @RequestParam String city
    ) {

        return ResponseEntity.ok(
                warehouseService.searchByCity(city)
        );
    }

    @GetMapping("/search/state")
    public ResponseEntity<List<Warehouse>> searchByState(
            @RequestParam String state
    ) {

        return ResponseEntity.ok(
                warehouseService.searchByState(state)
        );
    }

    @GetMapping("/search/location")
    public ResponseEntity<List<Warehouse>> searchByLocation(
            @RequestParam String location
    ) {

        return ResponseEntity.ok(
                warehouseService.searchByLocation(location)
        );
    }
}