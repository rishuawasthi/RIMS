package com.hcl.inventory.exception;

public class DuplicatePurchaseOrderException extends RuntimeException {

    public DuplicatePurchaseOrderException(String message) {
        super(message);
    }
}