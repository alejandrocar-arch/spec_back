package com.supermarket.sales.exception;

public class CustomerNotFoundException extends ResourceNotFoundException {
    
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
