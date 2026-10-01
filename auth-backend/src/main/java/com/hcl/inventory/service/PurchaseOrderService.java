package com.hcl.inventory.service;

import com.hcl.inventory.entity.Inventory;
import com.hcl.inventory.entity.Product;
import com.hcl.inventory.entity.PurchaseOrder;
import com.hcl.inventory.entity.Supplier;
import com.hcl.inventory.entity.Warehouse;
import com.hcl.inventory.enums.InventoryStatus;
import com.hcl.inventory.enums.PurchaseOrderStatus;
import com.hcl.inventory.exception.DuplicatePurchaseOrderException;
import com.hcl.inventory.exception.InvalidPurchaseOrderException;
import com.hcl.inventory.exception.ProductNotFoundException;
import com.hcl.inventory.exception.PurchaseOrderNotFoundException;
import com.hcl.inventory.exception.SupplierNotFoundException;
import com.hcl.inventory.exception.WarehouseNotFoundException;
import com.hcl.inventory.repository.InventoryRepository;
import com.hcl.inventory.repository.ProductRepository;
import com.hcl.inventory.repository.PurchaseOrderRepository;
import com.hcl.inventory.repository.SupplierRepository;
import com.hcl.inventory.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;

    @Autowired
    public PurchaseOrderService(
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            InventoryRepository inventoryRepository
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getAllPurchaseOrders() {

        return purchaseOrderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PurchaseOrder getPurchaseOrderById(Integer poId) {

        return purchaseOrderRepository.findById(poId)
                .orElseThrow(() ->
                        new PurchaseOrderNotFoundException(
                                "Purchase order with id "
                                        + poId
                                        + " not found"
                        )
                );
    }

    @Transactional
    public PurchaseOrder addPurchaseOrder(
            PurchaseOrder purchaseOrder
    ) {

        validateBasicPurchaseOrderData(purchaseOrder);

        String poNumber = purchaseOrder.getPoNumber()
                .trim()
                .toUpperCase();

        if (purchaseOrderRepository.existsByPoNumber(poNumber)) {

            throw new DuplicatePurchaseOrderException(
                    "Purchase order with number "
                            + poNumber
                            + " already exists"
            );
        }

        Supplier supplier = getSupplier(
                purchaseOrder.getSupplier().getSupplierId()
        );

        Product product = getProduct(
                purchaseOrder.getProduct().getProductId()
        );

        Warehouse warehouse = getWarehouse(
                purchaseOrder.getWarehouse().getWarehouseId()
        );

        purchaseOrder.setPoNumber(poNumber);
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setProduct(product);
        purchaseOrder.setWarehouse(warehouse);
        purchaseOrder.setReceivedQuantity(0);
        purchaseOrder.setStatus(PurchaseOrderStatus.DRAFT);
        purchaseOrder.setReceivedDate(null);

        return purchaseOrderRepository.save(purchaseOrder);
    }

    @Transactional
    public PurchaseOrder updatePurchaseOrder(
            Integer poId,
            PurchaseOrder updatedPurchaseOrder
    ) {

        PurchaseOrder existingPurchaseOrder =
                getPurchaseOrderById(poId);

        if (existingPurchaseOrder.getStatus()
                != PurchaseOrderStatus.DRAFT) {

            throw new InvalidPurchaseOrderException(
                    "Only DRAFT purchase orders can be updated"
            );
        }

        validateBasicPurchaseOrderData(updatedPurchaseOrder);

        String poNumber = updatedPurchaseOrder.getPoNumber()
                .trim()
                .toUpperCase();

        if (purchaseOrderRepository
                .existsByPoNumberAndPoIdNot(
                        poNumber,
                        poId
                )) {

            throw new DuplicatePurchaseOrderException(
                    "Purchase order with number "
                            + poNumber
                            + " already exists"
            );
        }

        Supplier supplier = getSupplier(
                updatedPurchaseOrder
                        .getSupplier()
                        .getSupplierId()
        );

        Product product = getProduct(
                updatedPurchaseOrder
                        .getProduct()
                        .getProductId()
        );

        Warehouse warehouse = getWarehouse(
                updatedPurchaseOrder
                        .getWarehouse()
                        .getWarehouseId()
        );

        existingPurchaseOrder.setPoNumber(poNumber);
        existingPurchaseOrder.setSupplier(supplier);
        existingPurchaseOrder.setProduct(product);
        existingPurchaseOrder.setWarehouse(warehouse);
        existingPurchaseOrder.setQuantity(
                updatedPurchaseOrder.getQuantity()
        );
        existingPurchaseOrder.setUnitPrice(
                updatedPurchaseOrder.getUnitPrice()
        );
        existingPurchaseOrder.setExpectedDeliveryDate(
                updatedPurchaseOrder.getExpectedDeliveryDate()
        );
        existingPurchaseOrder.setNotes(
                updatedPurchaseOrder.getNotes()
        );

        return purchaseOrderRepository.save(
                existingPurchaseOrder
        );
    }

    @Transactional
    public PurchaseOrder placePurchaseOrder(
            Integer poId
    ) {

        PurchaseOrder purchaseOrder =
                getPurchaseOrderById(poId);

        if (purchaseOrder.getStatus()
                != PurchaseOrderStatus.DRAFT) {

            throw new InvalidPurchaseOrderException(
                    "Only DRAFT purchase orders can be placed"
            );
        }

        purchaseOrder.setStatus(
                PurchaseOrderStatus.PLACED
        );

        return purchaseOrderRepository.save(
                purchaseOrder
        );
    }

    @Transactional
    public PurchaseOrder cancelPurchaseOrder(
            Integer poId
    ) {

        PurchaseOrder purchaseOrder =
                getPurchaseOrderById(poId);

        PurchaseOrderStatus status =
                purchaseOrder.getStatus();

        if (status == PurchaseOrderStatus.RECEIVED) {

            throw new InvalidPurchaseOrderException(
                    "A RECEIVED purchase order cannot be cancelled"
            );
        }

        if (status == PurchaseOrderStatus.CANCELLED) {

            throw new InvalidPurchaseOrderException(
                    "Purchase order is already cancelled"
            );
        }

        purchaseOrder.setStatus(
                PurchaseOrderStatus.CANCELLED
        );

        return purchaseOrderRepository.save(
                purchaseOrder
        );
    }

    @Transactional
    public PurchaseOrder receivePurchaseOrder(
            Integer poId,
            Integer receivingQuantity
    ) {

        PurchaseOrder purchaseOrder =
                getPurchaseOrderById(poId);

        if (receivingQuantity == null
                || receivingQuantity <= 0) {

            throw new InvalidPurchaseOrderException(
                    "Receiving quantity must be greater than 0"
            );
        }

        PurchaseOrderStatus status =
                purchaseOrder.getStatus();

        if (status != PurchaseOrderStatus.PLACED
                && status != PurchaseOrderStatus.PARTIALLY_RECEIVED) {

            throw new InvalidPurchaseOrderException(
                    "Only PLACED or PARTIALLY_RECEIVED purchase orders can be received"
            );
        }

        int currentReceived =
                purchaseOrder.getReceivedQuantity();

        int totalReceived =
                currentReceived + receivingQuantity;

        if (totalReceived > purchaseOrder.getQuantity()) {

            throw new InvalidPurchaseOrderException(
                    "Received quantity cannot exceed ordered quantity"
            );
        }

        updateInventory(
                purchaseOrder.getProduct().getProductId(),
                purchaseOrder.getWarehouse().getWarehouseId(),
                receivingQuantity
        );

        purchaseOrder.setReceivedQuantity(
                totalReceived
        );

        purchaseOrder.setReceivedDate(
                LocalDateTime.now()
        );

        if (totalReceived == purchaseOrder.getQuantity()) {

            purchaseOrder.setStatus(
                    PurchaseOrderStatus.RECEIVED
            );

        } else {

            purchaseOrder.setStatus(
                    PurchaseOrderStatus.PARTIALLY_RECEIVED
            );
        }

        return purchaseOrderRepository.save(
                purchaseOrder
        );
    }

    @Transactional
    public void deletePurchaseOrder(Integer poId) {

        PurchaseOrder purchaseOrder =
                getPurchaseOrderById(poId);

        PurchaseOrderStatus status =
                purchaseOrder.getStatus();

        if (status != PurchaseOrderStatus.DRAFT
                && status != PurchaseOrderStatus.CANCELLED) {

            throw new InvalidPurchaseOrderException(
                    "Only DRAFT or CANCELLED purchase orders can be deleted"
            );
        }

        purchaseOrderRepository.delete(
                purchaseOrder
        );
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> searchByPoNumber(
            String poNumber
    ) {

        return purchaseOrderRepository
                .findByPoNumberContainingIgnoreCase(
                        poNumber
                );
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getBySupplier(
            Integer supplierId
    ) {

        getSupplier(supplierId);

        return purchaseOrderRepository
                .findBySupplierSupplierId(
                        supplierId
                );
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getByProduct(
            Integer productId
    ) {

        getProduct(productId);

        return purchaseOrderRepository
                .findByProductProductId(
                        productId
                );
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getByWarehouse(
            Integer warehouseId
    ) {

        getWarehouse(warehouseId);

        return purchaseOrderRepository
                .findByWarehouseWarehouseId(
                        warehouseId
                );
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getByStatus(
            PurchaseOrderStatus status
    ) {

        return purchaseOrderRepository.findByStatus(
                status
        );
    }

    private void validateBasicPurchaseOrderData(
            PurchaseOrder purchaseOrder
    ) {

        if (purchaseOrder.getSupplier() == null
                || purchaseOrder.getSupplier().getSupplierId() == null) {

            throw new SupplierNotFoundException(
                    "Supplier id is required"
            );
        }

        if (purchaseOrder.getProduct() == null
                || purchaseOrder.getProduct().getProductId() == null) {

            throw new ProductNotFoundException(
                    "Product id is required"
            );
        }

        if (purchaseOrder.getWarehouse() == null
                || purchaseOrder.getWarehouse().getWarehouseId() == null) {

            throw new WarehouseNotFoundException(
                    "Warehouse id is required"
            );
        }

        if (purchaseOrder.getQuantity() == null
                || purchaseOrder.getQuantity() <= 0) {

            throw new InvalidPurchaseOrderException(
                    "Purchase order quantity must be greater than 0"
            );
        }

        if (purchaseOrder.getUnitPrice() == null
                || purchaseOrder.getUnitPrice().signum() <= 0) {

            throw new InvalidPurchaseOrderException(
                    "Unit price must be greater than 0"
            );
        }
    }

    private Supplier getSupplier(
            Integer supplierId
    ) {

        return supplierRepository.findById(supplierId)
                .orElseThrow(() ->
                        new SupplierNotFoundException(
                                "Supplier with id "
                                        + supplierId
                                        + " not found"
                        )
                );
    }

    private Product getProduct(
            Integer productId
    ) {

        return productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product with id "
                                        + productId
                                        + " not found"
                        )
                );
    }

    private Warehouse getWarehouse(
            Integer warehouseId
    ) {

        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() ->
                        new WarehouseNotFoundException(
                                "Warehouse with id "
                                        + warehouseId
                                        + " not found"
                        )
                );
    }

    private void updateInventory(
            Integer productId,
            Integer warehouseId,
            Integer receivingQuantity
    ) {

        Inventory inventory =
                inventoryRepository
                        .findByProductProductIdAndWarehouseWarehouseId(
                                productId,
                                warehouseId
                        )
                        .orElse(null);

        if (inventory == null) {

            Product product =
                    getProduct(productId);

            Warehouse warehouse =
                    getWarehouse(warehouseId);

            inventory = new Inventory();

            inventory.setProduct(product);
            inventory.setWarehouse(warehouse);
            inventory.setQuantity(receivingQuantity);
            inventory.setReorderLevel(10);
            inventory.setReservedQuantity(0);
            inventory.setStatus(
                    InventoryStatus.ACTIVE
            );

        } else {

            int newQuantity =
                    inventory.getQuantity()
                            + receivingQuantity;

            inventory.setQuantity(newQuantity);

            if (inventory.getStatus() == null) {
                inventory.setStatus(
                        InventoryStatus.ACTIVE
                );
            }
        }

        inventoryRepository.save(inventory);
    }
}