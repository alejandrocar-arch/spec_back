package com.supermarket.sales.exception;

public class ProductServiceUnavailableException extends ExternalServiceException {
    
    public ProductServiceUnavailableException(String message) {
        super(message);
    }
    
    public ProductServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
