package com.supermarket.sales.exception;

public class SaleAlreadyCancelledException extends BusinessException {
    
    public SaleAlreadyCancelledException(String message) {
        super(message);
    }
}
