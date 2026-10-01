package com.hcl.inventory.exception;

public class DuplicateWarehouseCodeException extends RuntimeException {

    public DuplicateWarehouseCodeException(String message) {
        super(message);
    }
}