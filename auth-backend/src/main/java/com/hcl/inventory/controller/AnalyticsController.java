package com.hcl.inventory.controller;

import com.hcl.inventory.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
@Tag(name = "Reporting & Analytics", description = "Executive KPI dashboards, valuation, category breakdowns, and turnover reports")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/kpis")
    @Operation(summary = "Get Executive KPIs", description = "Returns high-level retail metrics: inventory valuation, stockout rate, order fulfillments, and stock totals")
    public ResponseEntity<Map<String, Object>> getExecutiveKpis() {
        return ResponseEntity.ok(analyticsService.getExecutiveKpis());
    }

    @GetMapping("/categories")
    @Operation(summary = "Get Category Breakdown", description = "Returns stock levels and valuation aggregated by product category (Apparel, Electronics, Home Goods)")
    public ResponseEntity<List<Map<String, Object>>> getCategoryDistribution() {
        return ResponseEntity.ok(analyticsService.getCategoryDistribution());
    }
}
