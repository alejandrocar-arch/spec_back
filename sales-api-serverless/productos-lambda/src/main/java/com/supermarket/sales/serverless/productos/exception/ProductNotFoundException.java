package com.supermarket.sales.serverless.productos.exception;

/**
 * Exception thrown when a product is not found in DynamoDB.
 */
public class ProductNotFoundException extends RuntimeException {
    
    public ProductNotFoundException(String message) {
        super(message);
    }
    
    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
