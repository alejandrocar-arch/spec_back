package com.supermarket.sales.exception;

public class CustomerServiceUnavailableException extends ExternalServiceException {
    
    public CustomerServiceUnavailableException(String message) {
        super(message);
    }
    
    public CustomerServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
