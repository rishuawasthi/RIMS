package com.hcl.inventory.exception;

public class DuplicateInventoryException extends RuntimeException {

    public DuplicateInventoryException(String message) {
        super(message);
    }
}