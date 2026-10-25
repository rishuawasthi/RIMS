package com.hcl.inventory.controller;

import com.hcl.inventory.service.BarcodeScannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scanner")
@Tag(name = "Barcode & RFID Scanner", description = "Hardware emulation endpoints for handheld barcode scanners and RFID readers")
public class BarcodeScannerController {

    private final BarcodeScannerService barcodeScannerService;

    public BarcodeScannerController(BarcodeScannerService barcodeScannerService) {
        this.barcodeScannerService = barcodeScannerService;
    }

    @GetMapping("/scan")
    @Operation(summary = "Scan Barcode / SKU", description = "Simulate handheld scanner reading a product barcode or SKU")
    public ResponseEntity<Map<String, Object>> scanBarcode(@RequestParam String barcode) {
        return ResponseEntity.ok(barcodeScannerService.scanBarcode(barcode));
    }

    @PostMapping("/rfid-batch")
    @Operation(summary = "Process RFID Batch", description = "Simulate portal RFID antenna batch read for inbound receiving or outbound fulfillment")
    public ResponseEntity<Map<String, Object>> processRfidBatch(@RequestBody Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) payload.get("tags");
        Integer warehouseId = Integer.valueOf(String.valueOf(payload.get("warehouseId")));
        String operationType = String.valueOf(payload.getOrDefault("operationType", "INBOUND"));
        String referenceNumber = String.valueOf(payload.getOrDefault("referenceNumber", "RFID-" + System.currentTimeMillis()));

        return ResponseEntity.ok(barcodeScannerService.processRfidBatch(tags, warehouseId, operationType, referenceNumber));
    }
}
