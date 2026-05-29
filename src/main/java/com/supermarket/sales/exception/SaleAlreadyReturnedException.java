package com.supermarket.sales.exception;

public class SaleAlreadyReturnedException extends BusinessException {
    
    public SaleAlreadyReturnedException(String message) {
        super(message);
    }
}
