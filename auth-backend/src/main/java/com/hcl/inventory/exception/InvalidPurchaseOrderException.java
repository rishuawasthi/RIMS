package com.hcl.inventory.exception;

public class InvalidPurchaseOrderException extends RuntimeException {

    public InvalidPurchaseOrderException(String message) {
        super(message);
    }
}