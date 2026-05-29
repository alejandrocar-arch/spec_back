package com.supermarket.sales.exception;

public class SaleNotFoundException extends ResourceNotFoundException {
    
    public SaleNotFoundException(String message) {
        super(message);
    }
}
