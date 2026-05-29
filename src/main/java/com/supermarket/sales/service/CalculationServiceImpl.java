package com.supermarket.sales.service;

import com.supermarket.sales.entity.Sale;
import com.supermarket.sales.entity.SaleItem;
import com.supermarket.sales.enums.DiscountType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Implementation of CalculationService with precise BigDecimal arithmetic.
 */
@Service
public class CalculationServiceImpl implements CalculationService {
    
    @Value("${sales.tax-rate}")
    private BigDecimal taxRate;
    
    @Override
    public BigDecimal calculateLineTotal(BigDecimal unitPrice, Integer quantity) {
        return unitPrice.multiply(new BigDecimal(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public BigDecimal calculateSubtotal(List<SaleItem> items) {
        return items.stream()
                .map(SaleItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public BigDecimal calculateTax(BigDecimal subtotal, BigDecimal taxRate) {
        return subtotal.multiply(taxRate)
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public BigDecimal calculateDiscountAmount(BigDecimal subtotal, DiscountType type, BigDecimal value) {
        if (type == DiscountType.PERCENTAGE) {
            // Convert percentage to decimal (e.g., 10% -> 0.10)
            BigDecimal percentage = value.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            return subtotal.multiply(percentage)
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            // Fixed amount
            return value.setScale(2, RoundingMode.HALF_UP);
        }
    }
    
    @Override
    public BigDecimal calculateTotal(BigDecimal subtotal, BigDecimal tax, BigDecimal discount) {
        return subtotal.add(tax).subtract(discount)
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public BigDecimal calculateChange(BigDecimal total, BigDecimal amountReceived) {
        return amountReceived.subtract(total)
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public void recalculateSaleTotals(Sale sale) {
        // Calculate subtotal
        BigDecimal subtotal = calculateSubtotal(sale.getItems());
        sale.setSubtotal(subtotal);
        
        // Calculate tax
        BigDecimal tax = calculateTax(subtotal, taxRate);
        sale.setTax(tax);
        
        // Calculate discount if applicable
        BigDecimal discount = BigDecimal.ZERO;
        if (sale.getDiscountType() != null && sale.getDiscountValue() != null) {
            discount = calculateDiscountAmount(subtotal, sale.getDiscountType(), sale.getDiscountValue());
        }
        sale.setDiscount(discount);
        
        // Calculate total
        BigDecimal total = calculateTotal(subtotal, tax, discount);
        sale.setTotal(total);
    }
}
