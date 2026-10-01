package com.hcl.inventory.repository;

import com.hcl.inventory.entity.PurchaseOrder;
import com.hcl.inventory.enums.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Integer> {

    // Find purchase order by exact PO number
    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    // Check whether a PO number already exists
    boolean existsByPoNumber(String poNumber);

    // Check duplicate PO number while updating an existing purchase order
    boolean existsByPoNumberAndPoIdNot(String poNumber, Integer poId);

    // Search purchase orders by partial PO number
    List<PurchaseOrder> findByPoNumberContainingIgnoreCase(String poNumber);

    // Find purchase orders belonging to a supplier
    List<PurchaseOrder> findBySupplierSupplierId(Integer supplierId);

    // Find purchase orders for a particular product
    List<PurchaseOrder> findByProductProductId(Integer productId);

    // Find purchase orders for a particular warehouse
    List<PurchaseOrder> findByWarehouseWarehouseId(Integer warehouseId);

    // Find purchase orders by status
    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);
}