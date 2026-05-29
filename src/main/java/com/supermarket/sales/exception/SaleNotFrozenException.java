package com.supermarket.sales.exception;

public class SaleNotFrozenException extends BusinessException {
    
    public SaleNotFrozenException(String message) {
        super(message);
    }
}
