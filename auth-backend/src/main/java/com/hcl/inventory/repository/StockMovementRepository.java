package com.hcl.inventory.repository;

import com.hcl.inventory.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProductProductId(Integer productId);

    List<StockMovement> findByWarehouseWarehouseId(Integer warehouseId);

    List<StockMovement> findTop50ByOrderByCreatedAtDesc();
}
