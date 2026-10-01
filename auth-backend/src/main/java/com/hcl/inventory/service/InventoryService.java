package com.hcl.inventory.service;

import com.hcl.inventory.entity.Inventory;
import com.hcl.inventory.entity.Product;
import com.hcl.inventory.entity.Warehouse;
import com.hcl.inventory.enums.InventoryStatus;
import com.hcl.inventory.exception.DuplicateInventoryException;
import com.hcl.inventory.exception.InvalidInventoryQuantityException;
import com.hcl.inventory.exception.InventoryNotFoundException;
import com.hcl.inventory.exception.ProductNotFoundException;
import com.hcl.inventory.exception.WarehouseNotFoundException;
import com.hcl.inventory.repository.InventoryRepository;
import com.hcl.inventory.repository.ProductRepository;
import com.hcl.inventory.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    @Autowired
    public InventoryService(
            InventoryRepository inventoryRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository
    ) {

        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
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

            throw new ProductNotFoundException(
                    "Product id is required"
            );
        }

        if (inventory.getWarehouse() == null
                || inventory.getWarehouse().getWarehouseId() == null) {

            throw new WarehouseNotFoundException(
                    "Warehouse id is required"
            );
        }

        Integer productId =
                inventory.getProduct().getProductId();

        Integer warehouseId =
                inventory.getWarehouse().getWarehouseId();

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

            throw new ProductNotFoundException(
                    "Product id is required"
            );
        }

        if (updatedInventory.getWarehouse() == null
                || updatedInventory.getWarehouse().getWarehouseId() == null) {

            throw new WarehouseNotFoundException(
                    "Warehouse id is required"
            );
        }

        Integer productId =
                updatedInventory.getProduct().getProductId();

        Integer warehouseId =
                updatedInventory.getWarehouse().getWarehouseId();

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