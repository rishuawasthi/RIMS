package com.hcl.inventory.service;

import com.hcl.inventory.entity.Inventory;
import com.hcl.inventory.entity.Product;
import com.hcl.inventory.entity.StockMovement;
import com.hcl.inventory.entity.Warehouse;
import com.hcl.inventory.enums.MovementType;
import com.hcl.inventory.exception.ProductNotFoundException;
import com.hcl.inventory.repository.InventoryRepository;
import com.hcl.inventory.repository.ProductRepository;
import com.hcl.inventory.repository.StockMovementRepository;
import com.hcl.inventory.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BarcodeScannerService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockMovementRepository stockMovementRepository;

    public BarcodeScannerService(
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            WarehouseRepository warehouseRepository,
            StockMovementRepository stockMovementRepository
    ) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> scanBarcode(String code) {
        String queryCode = code.trim();

        // Find by SKU (our barcode identifier)
        List<Product> products = productRepository.findAll();
        Optional<Product> productOpt = products.stream()
                .filter(p -> p.getSku().equalsIgnoreCase(queryCode) || 
                             String.valueOf(p.getProductId()).equals(queryCode))
                .findFirst();

        if (productOpt.isEmpty()) {
            throw new ProductNotFoundException("No product found matching barcode / SKU: " + code);
        }

        Product product = productOpt.get();
        List<Inventory> stockLevels = inventoryRepository.findByProductProductId(product.getProductId());

        int totalOnHand = stockLevels.stream().mapToInt(Inventory::getQuantity).sum();
        int totalReserved = stockLevels.stream().mapToInt(Inventory::getReservedQuantity).sum();

        Map<String, Object> result = new HashMap<>();
        result.put("barcode", queryCode);
        result.put("product", product);
        result.put("totalOnHand", totalOnHand);
        result.put("totalAvailable", Math.max(0, totalOnHand - totalReserved));
        result.put("warehouseStock", stockLevels);
        result.put("scanTimestamp", java.time.LocalDateTime.now());
        result.put("status", "VERIFIED");

        return result;
    }

    @Transactional
    public Map<String, Object> processRfidBatch(
            List<String> rfidTags,
            Integer warehouseId,
            String operationType,
            String referenceNumber
    ) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse " + warehouseId + " not found"));

        int successfulScans = 0;
        List<Map<String, Object>> details = new ArrayList<>();

        for (String tag : rfidTags) {
            String sku = tag.contains("-") ? tag : "SKU-" + tag;
            List<Product> matching = productRepository.findAll();
            Optional<Product> pOpt = matching.stream()
                    .filter(p -> p.getSku().equalsIgnoreCase(sku) || p.getSku().equalsIgnoreCase(tag))
                    .findFirst();

            if (pOpt.isPresent()) {
                Product p = pOpt.get();
                successfulScans++;

                // Record stock movement audit
                MovementType mType = "INBOUND".equalsIgnoreCase(operationType)
                        ? MovementType.INBOUND_PO
                        : MovementType.OUTBOUND_SALE;

                stockMovementRepository.save(new StockMovement(
                        p, warehouse, mType, 1, referenceNumber, "RFID Scan: " + tag, "RFID_SYSTEM"
                ));

                details.add(Map.of(
                        "tag", tag,
                        "productId", p.getProductId(),
                        "productName", p.getProductName(),
                        "status", "PROCESSED"
                ));
            } else {
                details.add(Map.of(
                        "tag", tag,
                        "status", "UNRECOGNIZED_TAG"
                ));
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalTagsScanned", rfidTags.size());
        response.put("successfulScans", successfulScans);
        response.put("warehouse", warehouse.getWarehouseName());
        response.put("operationType", operationType);
        response.put("referenceNumber", referenceNumber);
        response.put("scannedItems", details);

        return response;
    }
}
