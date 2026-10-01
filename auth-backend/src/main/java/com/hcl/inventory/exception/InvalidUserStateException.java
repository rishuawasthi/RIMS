package com.hcl.inventory.exception;

public class InvalidUserStateException
        extends RuntimeException {

    public InvalidUserStateException(String message) {
        super(message);
    }
}