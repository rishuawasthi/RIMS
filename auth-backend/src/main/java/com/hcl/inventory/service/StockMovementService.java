package com.hcl.inventory.service;

import com.hcl.inventory.entity.StockMovement;
import com.hcl.inventory.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;

    public StockMovementService(StockMovementRepository stockMovementRepository) {
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional(readOnly = true)
    public List<StockMovement> getRecentMovements() {
        return stockMovementRepository.findTop50ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<StockMovement> getMovementsByProduct(Integer productId) {
        return stockMovementRepository.findByProductProductId(productId);
    }

    @Transactional(readOnly = true)
    public List<StockMovement> getMovementsByWarehouse(Integer warehouseId) {
        return stockMovementRepository.findByWarehouseWarehouseId(warehouseId);
    }

    @Transactional
    public StockMovement recordMovement(StockMovement movement) {
        return stockMovementRepository.save(movement);
    }
}
