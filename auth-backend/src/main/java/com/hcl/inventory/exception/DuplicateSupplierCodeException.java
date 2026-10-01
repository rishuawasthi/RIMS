package com.hcl.inventory.exception;

public class DuplicateSupplierCodeException
        extends RuntimeException {

    public DuplicateSupplierCodeException(String message) {
        super(message);
    }
}