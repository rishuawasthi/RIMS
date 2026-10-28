package com.hcl.inventory.service;

import com.hcl.inventory.dto.StockTransferRequest;
import com.hcl.inventory.entity.Inventory;
import com.hcl.inventory.entity.Product;
import com.hcl.inventory.entity.StockMovement;
import com.hcl.inventory.entity.Warehouse;
import com.hcl.inventory.enums.InventoryStatus;
import com.hcl.inventory.enums.MovementType;
import com.hcl.inventory.exception.DuplicateInventoryException;
import com.hcl.inventory.exception.InvalidInventoryQuantityException;
import com.hcl.inventory.exception.InventoryNotFoundException;
import com.hcl.inventory.exception.ProductNotFoundException;
import com.hcl.inventory.exception.WarehouseNotFoundException;
import com.hcl.inventory.repository.InventoryRepository;
import com.hcl.inventory.repository.ProductRepository;
import com.hcl.inventory.repository.StockMovementRepository;
import com.hcl.inventory.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockMovementRepository stockMovementRepository;

    @Autowired
    public InventoryService(
            InventoryRepository inventoryRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            @Autowired(required = false) StockMovementRepository stockMovementRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public InventoryService(
            InventoryRepository inventoryRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository
    ) {
        this(inventoryRepository, productRepository, warehouseRepository, null);
    }

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public Inventory getInventoryById(Integer inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory with id "
                                        + inventoryId
                                        + " not found"
                        )
                );
    }

    public Inventory addInventory(Inventory inventory) {
        if (inventory.getProduct() == null
                || inventory.getProduct().getProductId() == null) {
            throw new ProductNotFoundException("Product id is required");
        }

        if (inventory.getWarehouse() == null
                || inventory.getWarehouse().getWarehouseId() == null) {
            throw new WarehouseNotFoundException("Warehouse id is required");
        }

        Integer productId = inventory.getProduct().getProductId();
        Integer warehouseId = inventory.getWarehouse().getWarehouseId();

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product with id "
                                        + productId
                                        + " not found"
                        )
                );

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() ->
                        new WarehouseNotFoundException(
                                "Warehouse with id "
                                        + warehouseId
                                        + " not found"
                        )
                );

        if (inventoryRepository
                .existsByProductProductIdAndWarehouseWarehouseId(
                        productId,
                        warehouseId
                )) {
            throw new DuplicateInventoryException(
                    "Inventory for product "
                            + productId
                            + " in warehouse "
                            + warehouseId
                            + " already exists"
            );
        }

        validateReservedQuantity(
                inventory.getQuantity(),
                inventory.getReservedQuantity()
        );

        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);

        if (inventory.getStatus() == null) {
            inventory.setStatus(InventoryStatus.ACTIVE);
        }

        if (inventory.getReservedQuantity() == null) {
            inventory.setReservedQuantity(0);
        }

        return inventoryRepository.save(inventory);
    }

    public Inventory updateInventory(
            Integer inventoryId,
            Inventory updatedInventory
    ) {
        Inventory existingInventory =
                inventoryRepository.findById(inventoryId)
                        .orElseThrow(() ->
                                new InventoryNotFoundException(
                                        "Inventory with id "
                                                + inventoryId
                                                + " not found"
                                )
                        );

        if (updatedInventory.getProduct() == null
                || updatedInventory.getProduct().getProductId() == null) {
            throw new ProductNotFoundException("Product id is required");
        }

        if (updatedInventory.getWarehouse() == null
                || updatedInventory.getWarehouse().getWarehouseId() == null) {
            throw new WarehouseNotFoundException("Warehouse id is required");
        }

        Integer productId = updatedInventory.getProduct().getProductId();
        Integer warehouseId = updatedInventory.getWarehouse().getWarehouseId();

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product with id "
                                        + productId
                                        + " not found"
                        )
                );

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() ->
                        new WarehouseNotFoundException(
                                "Warehouse with id "
                                        + warehouseId
                                        + " not found"
                        )
                );

        if (inventoryRepository
                .existsByProductProductIdAndWarehouseWarehouseIdAndInventoryIdNot(
                        productId,
                        warehouseId,
                        inventoryId
                )) {
            throw new DuplicateInventoryException(
                    "Inventory for product "
                            + productId
                            + " in warehouse "
                            + warehouseId
                            + " already exists"
            );
        }

        validateReservedQuantity(
                updatedInventory.getQuantity(),
                updatedInventory.getReservedQuantity()
        );

        existingInventory.setProduct(product);
        existingInventory.setWarehouse(warehouse);

        existingInventory.setQuantity(
                updatedInventory.getQuantity()
        );

        existingInventory.setReorderLevel(
                updatedInventory.getReorderLevel()
        );

        existingInventory.setReservedQuantity(
                updatedInventory.getReservedQuantity()
        );

        if (updatedInventory.getStatus() != null) {
            existingInventory.setStatus(
                    updatedInventory.getStatus()
            );
        }

        return inventoryRepository.save(existingInventory);
    }

    public void deleteInventory(Integer inventoryId) {
        Inventory inventory = getInventoryById(inventoryId);
        inventoryRepository.delete(inventory);
    }

    public List<Inventory> getInventoryByWarehouse(
            Integer warehouseId
    ) {
        warehouseRepository.findById(warehouseId)
                .orElseThrow(() ->
                        new WarehouseNotFoundException(
                                "Warehouse with id "
                                        + warehouseId
                                        + " not found"
                        )
                );

        return inventoryRepository.findByWarehouseWarehouseId(
                warehouseId
        );
    }

    public List<Inventory> getInventoryByProduct(
            Integer productId
    ) {
        productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product with id "
                                        + productId
                                        + " not found"
                        )
                );

        return inventoryRepository.findByProductProductId(
                productId
        );
    }

    public List<Inventory> getLowStockInventory() {
        return inventoryRepository.findLowStockInventory();
    }

    @Transactional
    public Map<String, Object> transferStock(StockTransferRequest request) {
        if (request.getFromWarehouseId().equals(request.getToWarehouseId())) {
            throw new InvalidInventoryQuantityException("Source and destination warehouses cannot be the same");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product with id " + request.getProductId() + " not found"));

        Warehouse fromWarehouse = warehouseRepository.findById(request.getFromWarehouseId())
                .orElseThrow(() -> new WarehouseNotFoundException("Source warehouse with id " + request.getFromWarehouseId() + " not found"));

        Warehouse toWarehouse = warehouseRepository.findById(request.getToWarehouseId())
                .orElseThrow(() -> new WarehouseNotFoundException("Destination warehouse with id " + request.getToWarehouseId() + " not found"));

        Inventory sourceInv = inventoryRepository.findByProductProductIdAndWarehouseWarehouseId(
                request.getProductId(), request.getFromWarehouseId())
                .orElseThrow(() -> new InventoryNotFoundException("Stock record for product in source warehouse not found"));

        int available = sourceInv.getQuantity() - sourceInv.getReservedQuantity();
        if (available < request.getQuantity()) {
            throw new InvalidInventoryQuantityException("Insufficient unreserved stock in source warehouse. Available: "
                    + available + ", requested: " + request.getQuantity());
        }

        // Deduct from source warehouse
        sourceInv.setQuantity(sourceInv.getQuantity() - request.getQuantity());
        inventoryRepository.save(sourceInv);

        // Add to destination warehouse
        Inventory targetInv = inventoryRepository.findByProductProductIdAndWarehouseWarehouseId(
                request.getProductId(), request.getToWarehouseId())
                .orElseGet(() -> {
                    Inventory newInv = new Inventory();
                    newInv.setProduct(product);
                    newInv.setWarehouse(toWarehouse);
                    newInv.setQuantity(0);
                    newInv.setReservedQuantity(0);
                    newInv.setReorderLevel(sourceInv.getReorderLevel() != null ? sourceInv.getReorderLevel() : 10);
                    newInv.setStatus(InventoryStatus.ACTIVE);
                    return newInv;
                });

        targetInv.setQuantity(targetInv.getQuantity() + request.getQuantity());
        inventoryRepository.save(targetInv);

        // Audit Stock Movement Ledger
        String ref = request.getReferenceNumber() != null && !request.getReferenceNumber().isBlank()
                ? request.getReferenceNumber().trim()
                : "TRF-" + System.currentTimeMillis();
        String user = request.getPerformedBy() != null && !request.getPerformedBy().isBlank()
                ? request.getPerformedBy().trim()
                : "SYSTEM";

        if (stockMovementRepository != null) {
            stockMovementRepository.save(new StockMovement(
                    product, fromWarehouse, MovementType.WAREHOUSE_TRANSFER,
                    request.getQuantity(), ref,
                    "Transferred TO " + toWarehouse.getWarehouseCode() + (request.getReason() != null ? " - " + request.getReason() : ""),
                    user
            ));

            stockMovementRepository.save(new StockMovement(
                    product, toWarehouse, MovementType.WAREHOUSE_TRANSFER,
                    request.getQuantity(), ref,
                    "Transferred FROM " + fromWarehouse.getWarehouseCode() + (request.getReason() != null ? " - " + request.getReason() : ""),
                    user
            ));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", "SUCCESS");
        result.put("productId", product.getProductId());
        result.put("productSku", product.getSku());
        result.put("fromWarehouse", fromWarehouse.getWarehouseCode());
        result.put("toWarehouse", toWarehouse.getWarehouseCode());
        result.put("transferredQuantity", request.getQuantity());
        result.put("sourceRemainingQuantity", sourceInv.getQuantity());
        result.put("targetNewQuantity", targetInv.getQuantity());
        result.put("referenceNumber", ref);
        return result;
    }

    private void validateReservedQuantity(
            Integer quantity,
            Integer reservedQuantity
    ) {
        if (quantity == null) {
            return;
        }

        int reserved =
                reservedQuantity == null
                        ? 0
                        : reservedQuantity;

        if (reserved > quantity) {
            throw new InvalidInventoryQuantityException(
                    "Reserved quantity cannot be greater than available quantity"
            );
        }
    }
}
