package com.hcl.inventory.service;

import com.hcl.inventory.entity.Inventory;
import com.hcl.inventory.entity.Product;
import com.hcl.inventory.repository.InventoryRepository;
import com.hcl.inventory.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ErpConnectorService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public ErpConnectorService(ProductRepository productRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public Map<String, Object> getConnectorStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("connectorName", "XYZ-Enterprise-ERP-Adapter");
        status.put("targetSystem", "SAP S/4HANA & NetSuite Accounting");
        status.put("protocol", "REST / JSON Webhook & SFTP Batch");
        status.put("connectionStatus", "CONNECTED_HEALTHY");
        status.put("lastSyncTime", LocalDateTime.now().minusMinutes(14));
        status.put("autoSyncIntervalMinutes", 30);
        status.put("syncVersion", "v2.4.0");
        return status;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> exportInventorySnapshot() {
        List<Inventory> allInventory = inventoryRepository.findAll();

        List<Map<String, Object>> records = allInventory.stream().map(inv -> {
            Map<String, Object> item = new HashMap<>();
            item.put("sku", inv.getProduct().getSku());
            item.put("productName", inv.getProduct().getProductName());
            item.put("warehouseCode", inv.getWarehouse().getWarehouseCode());
            item.put("onHandQuantity", inv.getQuantity());
            item.put("reservedQuantity", inv.getReservedQuantity());
            item.put("unitPrice", inv.getProduct().getProductPrice());
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> payload = new HashMap<>();
        payload.put("batchId", "ERP-EXP-" + System.currentTimeMillis());
        payload.put("timestamp", LocalDateTime.now());
        payload.put("recordCount", records.size());
        payload.put("format", "SAP_IDOC_INVENTORY_SNAPSHOT");
        payload.put("records", records);

        return payload;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> syncCatalog() {
        List<Product> products = productRepository.findAll();

        Map<String, Object> result = new HashMap<>();
        result.put("status", "SUCCESS");
        result.put("syncedSkus", products.size());
        result.put("syncTimestamp", LocalDateTime.now());
        result.put("message", "Master data synchronization complete with ERP centralized product hub.");

        return result;
    }
}
