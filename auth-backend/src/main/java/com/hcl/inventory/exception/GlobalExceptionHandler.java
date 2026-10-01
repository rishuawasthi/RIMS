package com.hcl.inventory.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>>
    handleValidationException(
            MethodArgumentNotValidException exception
    ) {

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>>
    handleMessageNotReadableException(
            HttpMessageNotReadableException exception
    ) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                "Invalid request data. Please check the data types and JSON format."
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>>
    handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception
    ) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                "Invalid value for request parameter or path variable."
        );
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>>
    handleProductNotFoundException(
            ProductNotFoundException exception
    ) {

        return buildError(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicateSkuException.class)
    public ResponseEntity<Map<String, String>>
    handleDuplicateSkuException(
            DuplicateSkuException exception
    ) {

        return buildError(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(WarehouseNotFoundException.class)
    public ResponseEntity<Map<String, String>>
    handleWarehouseNotFoundException(
            WarehouseNotFoundException exception
    ) {

        return buildError(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicateWarehouseCodeException.class)
    public ResponseEntity<Map<String, String>>
    handleDuplicateWarehouseCodeException(
            DuplicateWarehouseCodeException exception
    ) {

        return buildError(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<Map<String, String>>
    handleInventoryNotFoundException(
            InventoryNotFoundException exception
    ) {

        return buildError(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicateInventoryException.class)
    public ResponseEntity<Map<String, String>>
    handleDuplicateInventoryException(
            DuplicateInventoryException exception
    ) {

        return buildError(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidInventoryQuantityException.class)
    public ResponseEntity<Map<String, String>>
    handleInvalidInventoryQuantityException(
            InvalidInventoryQuantityException exception
    ) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    @ExceptionHandler(SupplierNotFoundException.class)
    public ResponseEntity<Map<String, String>>
    handleSupplierNotFoundException(
            SupplierNotFoundException exception
    ) {

        return buildError(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicateSupplierCodeException.class)
    public ResponseEntity<Map<String, String>>
    handleDuplicateSupplierCodeException(
            DuplicateSupplierCodeException exception
    ) {

        return buildError(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicateSupplierEmailException.class)
    public ResponseEntity<Map<String, String>>
    handleDuplicateSupplierEmailException(
            DuplicateSupplierEmailException exception
    ) {

        return buildError(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(PurchaseOrderNotFoundException.class)
    public ResponseEntity<Map<String, String>>
    handlePurchaseOrderNotFoundException(
            PurchaseOrderNotFoundException exception
    ) {

        return buildError(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicatePurchaseOrderException.class)
    public ResponseEntity<Map<String, String>>
    handleDuplicatePurchaseOrderException(
            DuplicatePurchaseOrderException exception
    ) {

        return buildError(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidPurchaseOrderException.class)
    public ResponseEntity<Map<String, String>>
    handleInvalidPurchaseOrderException(
            InvalidPurchaseOrderException exception
    ) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, String>>
    handleOrderNotFoundException(
            OrderNotFoundException exception
    ) {

        return buildError(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicateOrderException.class)
    public ResponseEntity<Map<String, String>>
    handleDuplicateOrderException(
            DuplicateOrderException exception
    ) {

        return buildError(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<Map<String, String>>
    handleInvalidOrderException(
            InvalidOrderException exception
    ) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    @ExceptionHandler(OrderItemNotFoundException.class)
    public ResponseEntity<Map<String, String>>
    handleOrderItemNotFoundException(
            OrderItemNotFoundException exception
    ) {

        return buildError(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicateOrderItemException.class)
    public ResponseEntity<Map<String, String>>
    handleDuplicateOrderItemException(
            DuplicateOrderItemException exception
    ) {

        return buildError(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidOrderItemException.class)
    public ResponseEntity<Map<String, String>>
    handleInvalidOrderItemException(
            InvalidOrderItemException exception
    ) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>>
    handleUserNotFoundException(
            UserNotFoundException exception
    ) {

        return buildError(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<Map<String, String>>
    handleDuplicateUsernameException(
            DuplicateUsernameException exception
    ) {

        return buildError(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicateUserEmailException.class)
    public ResponseEntity<Map<String, String>>
    handleDuplicateUserEmailException(
            DuplicateUserEmailException exception
    ) {

        return buildError(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidUserStateException.class)
    public ResponseEntity<Map<String, String>>
    handleInvalidUserStateException(
            InvalidUserStateException exception
    ) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>>
    handleIllegalStateException(
            IllegalStateException exception
    ) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>>
    handleDataIntegrityViolationException(
            DataIntegrityViolationException exception
    ) {

        return buildError(
                HttpStatus.CONFLICT,
                "Database constraint violation. Please check for duplicate or invalid data."
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>>
    handleGenericException(
            Exception exception
    ) {

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred."
        );
    }

    private ResponseEntity<Map<String, String>>
    buildError(
            HttpStatus status,
            String message
    ) {

        Map<String, String> error = new HashMap<>();

        error.put(
                "message",
                message
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }
}