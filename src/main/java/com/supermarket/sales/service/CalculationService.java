package com.supermarket.sales.service;

import com.supermarket.sales.entity.Sale;
import com.supermarket.sales.entity.SaleItem;
import com.supermarket.sales.enums.DiscountType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for handling all monetary calculations with precision.
 */
public interface CalculationService {
    
    /**
     * Calculate line item total
     */
    BigDecimal calculateLineTotal(BigDecimal unitPrice, Integer quantity);
    
    /**
     * Calculate subtotal from all items
     */
    BigDecimal calculateSubtotal(List<SaleItem> items);
    
    /**
     * Calculate tax based on configurable rate
     */
    BigDecimal calculateTax(BigDecimal subtotal, BigDecimal taxRate);
    
    /**
     * Calculate discount amount (percentage or fixed)
     */
    BigDecimal calculateDiscountAmount(BigDecimal subtotal, DiscountType type, BigDecimal value);
    
    /**
     * Calculate final total
     */
    BigDecimal calculateTotal(BigDecimal subtotal, BigDecimal tax, BigDecimal discount);
    
    /**
     * Calculate change for cash payments
     */
    BigDecimal calculateChange(BigDecimal total, BigDecimal amountReceived);
    
    /**
     * Recalculate all sale totals
     */
    void recalculateSaleTotals(Sale sale);
}
