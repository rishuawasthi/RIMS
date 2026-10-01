package com.hcl.inventory.exception;

public class DuplicateSupplierEmailException
        extends RuntimeException {

    public DuplicateSupplierEmailException(String message) {
        super(message);
    }
}