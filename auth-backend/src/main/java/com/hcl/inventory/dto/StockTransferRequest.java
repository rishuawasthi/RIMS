package com.hcl.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StockTransferRequest {

    @NotNull(message = "Product ID is required")
    private Integer productId;

    @NotNull(message = "Source warehouse ID is required")
    private Integer fromWarehouseId;

    @NotNull(message = "Destination warehouse ID is required")
    private Integer toWarehouseId;

    @NotNull(message = "Transfer quantity is required")
    @Min(value = 1, message = "Transfer quantity must be at least 1")
    private Integer quantity;

    private String referenceNumber;
    private String reason;
    private String performedBy;

    public StockTransferRequest() {
    }

    public StockTransferRequest(Integer productId, Integer fromWarehouseId, Integer toWarehouseId, Integer quantity) {
        this.productId = productId;
        this.fromWarehouseId = fromWarehouseId;
        this.toWarehouseId = toWarehouseId;
        this.quantity = quantity;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getFromWarehouseId() {
        return fromWarehouseId;
    }

    public void setFromWarehouseId(Integer fromWarehouseId) {
        this.fromWarehouseId = fromWarehouseId;
    }

    public Integer getToWarehouseId() {
        return toWarehouseId;
    }

    public void setToWarehouseId(Integer toWarehouseId) {
        this.toWarehouseId = toWarehouseId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }
}
