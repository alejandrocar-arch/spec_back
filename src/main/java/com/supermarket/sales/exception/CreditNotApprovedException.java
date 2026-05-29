package com.supermarket.sales.exception;

public class CreditNotApprovedException extends BusinessException {
    
    public CreditNotApprovedException(String message) {
        super(message);
    }
}
