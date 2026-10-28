package com.hcl.inventory.repository;

import com.hcl.inventory.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.product.productId = :productId AND i.warehouse.warehouseId = :warehouseId")
    Optional<Inventory> findByProductProductIdAndWarehouseWarehouseIdWithLock(
            @Param("productId") Integer productId,
            @Param("warehouseId") Integer warehouseId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.inventoryId = :inventoryId")
    Optional<Inventory> findByIdWithLock(@Param("inventoryId") Integer inventoryId);

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
