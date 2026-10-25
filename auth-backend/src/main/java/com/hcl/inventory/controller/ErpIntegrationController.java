package com.hcl.inventory.controller;

import com.hcl.inventory.service.ErpConnectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/integration/erp")
@Tag(name = "ERP & Accounting Integration", description = "Connectors and synchronization adapters for enterprise ERP and accounting platforms")
public class ErpIntegrationController {

    private final ErpConnectorService erpConnectorService;

    public ErpIntegrationController(ErpConnectorService erpConnectorService) {
        this.erpConnectorService = erpConnectorService;
    }

    @GetMapping("/status")
    @Operation(summary = "Connector Health Status", description = "Check status of external ERP adapter (SAP / NetSuite)")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(erpConnectorService.getConnectorStatus());
    }

    @GetMapping("/export-inventory")
    @Operation(summary = "Export Inventory Snapshot", description = "Generates standardized ERP inventory batch export payload")
    public ResponseEntity<Map<String, Object>> exportInventory() {
        return ResponseEntity.ok(erpConnectorService.exportInventorySnapshot());
    }

    @PostMapping("/sync-catalog")
    @Operation(summary = "Sync Product Catalog", description = "Triggers bidirectional master data catalog sync with ERP")
    public ResponseEntity<Map<String, Object>> syncCatalog() {
        return ResponseEntity.ok(erpConnectorService.syncCatalog());
    }
}
