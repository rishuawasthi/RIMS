package com.hcl.inventory.repository;

import com.hcl.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository
        extends JpaRepository<Inventory, Integer> {

    boolean existsByProductProductIdAndWarehouseWarehouseId(
            Integer productId,
            Integer warehouseId
    );

    boolean existsByProductProductIdAndWarehouseWarehouseIdAndInventoryIdNot(
            Integer productId,
            Integer warehouseId,
            Integer inventoryId
    );

    Optional<Inventory> findByProductProductIdAndWarehouseWarehouseId(
            Integer productId,
            Integer warehouseId
    );

    List<Inventory> findByWarehouseWarehouseId(
            Integer warehouseId
    );

    List<Inventory> findByProductProductId(
            Integer productId
    );

    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.quantity <= i.reorderLevel
            """)
    List<Inventory> findLowStockInventory();
}