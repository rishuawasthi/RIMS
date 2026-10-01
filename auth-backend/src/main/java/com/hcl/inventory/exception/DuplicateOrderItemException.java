package com.hcl.inventory.exception;

public class DuplicateOrderItemException extends RuntimeException {

    public DuplicateOrderItemException(String message) {
        super(message);
    }
}