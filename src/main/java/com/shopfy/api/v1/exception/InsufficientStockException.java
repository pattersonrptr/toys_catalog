package com.shopfy.api.v1.exception;

/**
 * Thrown when a requested quantity exceeds the available stock.
 * Maps to HTTP 409 Conflict via {@link GlobalExceptionHandler}.
 */
public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(String productName, int requested, int available) {
        super(String.format(
                "Insufficient stock for '%s': requested %d, available %d",
                productName, requested, available));
    }
}
