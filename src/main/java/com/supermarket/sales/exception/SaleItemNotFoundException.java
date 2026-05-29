package com.supermarket.sales.exception;

public class SaleItemNotFoundException extends ResourceNotFoundException {
    
    public SaleItemNotFoundException(String message) {
        super(message);
    }
}
