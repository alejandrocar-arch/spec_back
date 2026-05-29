package com.supermarket.sales.service;

import com.supermarket.sales.entity.Sale;
import com.supermarket.sales.entity.SaleItem;
import com.supermarket.sales.enums.DiscountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CalculationServiceImpl.
 * Tests all calculation methods with various inputs, BigDecimal precision, and rounding.
 */
@ExtendWith(MockitoExtension.class)
class CalculationServiceImplTest {
    
    @InjectMocks
    private CalculationServiceImpl calculationService;
    
    private static final BigDecimal TAX_RATE = new BigDecimal("0.19");
    
    @BeforeEach
    void setUp() {
        // Set the tax rate using reflection since it's a @Value field
        ReflectionTestUtils.setField(calculationService, "taxRate", TAX_RATE);
    }
    
    @Test
    void calculateLineTotal_withValidPriceAndQuantity_returnsCorrectTotal() {
        // Given
        BigDecimal unitPrice = new BigDecimal("10.50");
        Integer quantity = 3;
        
        // When
        BigDecimal result = calculationService.calculateLineTotal(unitPrice, quantity);
        
        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("31.50"));
        assertThat(result.scale()).isEqualTo(2);
    }
    
    @Test
    void calculateLineTotal_withDecimalPrice_roundsCorrectly() {
        // Given
        BigDecimal unitPrice = new BigDecimal("10.333");
        Integer quantity = 3;
        
        // When
        BigDecimal result = calculationService.calculateLineTotal(unitPrice, quantity);
        
        // Then
        // 10.333 * 3 = 30.999, rounds to 31.00
        assertThat(result).isEqualByComparingTo(new BigDecimal("31.00"));
        assertThat(result.scale()).isEqualTo(2);
    }
    
    @Test
    void calculateLineTotal_withRoundingHalfUp_roundsCorrectly() {
        // Given
        BigDecimal unitPrice = new BigDecimal("10.335");
        Integer quantity = 1;
        
        // When
        BigDecimal result = calculationService.calculateLineTotal(unitPrice, quantity);
        
        // Then
        // 10.335 rounds to 10.34 with HALF_UP
        assertThat(result).isEqualByComparingTo(new BigDecimal("10.34"));
    }
    
    @Test
    void calculateLineTotal_withZeroQuantity_returnsZero() {
        // Given
        BigDecimal unitPrice = new BigDecimal("10.50");
        Integer quantity = 0;
        
        // When
        BigDecimal result = calculationService.calculateLineTotal(unitPrice, quantity);
        
        // Then
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }
    
    @Test
    void calculateLineTotal_withLargeQuantity_handlesCorrectly() {
        // Given
        BigDecimal unitPrice = new BigDecimal("99.99");
        Integer quantity = 100;
        
        // When
        BigDecimal result = calculationService.calculateLineTotal(unitPrice, quantity);
        
        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("9999.00"));
    }
    
    @Test
    void calculateSubtotal_withMultipleItems_returnsSum() {
        // Given
        List<SaleItem> items = new ArrayList<>();
        items.add(createSaleItem(new BigDecimal("10.50")));
        items.add(createSaleItem(new BigDecimal("20.30")));
        items.add(createSaleItem(new BigDecimal("5.20")));
        
        // When
        BigDecimal result = calculationService.calculateSubtotal(items);
        
        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("36.00"));
        assertThat(result.scale()).isEqualTo(2);
    }
    
    @Test
    void calculateSubtotal_withEmptyList_returnsZero() {
        // Given
        List<SaleItem> items = new ArrayList<>();
        
        // When
        BigDecimal result = calculationService.calculateSubtotal(items);
        
        // Then
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }
    
    @Test
    void calculateSubtotal_withSingleItem_returnsItemTotal() {
        // Given
        List<SaleItem> items = new ArrayList<>();
        items.add(createSaleItem(new BigDecimal("15.75")));
        
        // When
        BigDecimal result = calculationService.calculateSubtotal(items);
        
        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("15.75"));
    }
    
    @Test
    void calculateTax_withStandardRate_calculatesCorrectly() {
        // Given
        BigDecimal subtotal = new BigDecimal("100.00");
        BigDecimal taxRate = new BigDecimal("0.19");
        
        // When
        BigDecimal result = calculationService.calculateTax(subtotal, taxRate);
        
        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("19.00"));
        assertThat(result.scale()).isEqualTo(2);
    }
    
    @Test
    void calculateTax_withDifferentRate_calculatesCorrectly() {
        // Given
        BigDecimal subtotal = new BigDecimal("50.00");
        BigDecimal taxRate = new BigDecimal("0.10");
        
        // When
        BigDecimal result = calculationService.calculateTax(subtotal, taxRate);
        
        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("5.00"));
    }
    
    @Test
    void calculateTax_withDecimalSubtotal_roundsCorrectly() {
        // Given
        BigDecimal subtotal = new BigDecimal("33.33");
        BigDecimal taxRate = new BigDecimal("0.19");
        
        // When
        BigDecimal result = calculationService.calculateTax(subtotal, taxRate);
        
        // Then
        // 33.33 * 0.19 = 6.3327, rounds to 6.33
        assertThat(result).isEqualByComparingTo(new BigDecimal("6.33"));
    }
    
    @Test
    void calculateTax_withZeroSubtotal_returnsZero() {
        // Given
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxRate = new BigDecimal("0.19");
        
        // When
        BigDecimal result = calculationService.calculateTax(subtotal, taxRate);
        
        // Then
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }
    
    @Test
    void calculateDiscountAmount_withPercentageDiscount_calculatesCorrectly() {
        // Given
        BigDecimal subtotal = new BigDecimal("100.00");
        DiscountType type = DiscountType.PERCENTAGE;
        BigDecimal value = new BigDecimal("10");
        
        // When
        BigDecimal result = calculationService.calculateDiscountAmount(subtotal, type, value);
        
        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(result.scale()).isEqualTo(2);
    }
    
    @Test
    void calculateDiscountAmount_withPercentageDiscount_roundsCorrectly() {
        // Given
        BigDecimal subtotal = new BigDecimal("33.33");
        DiscountType type = DiscountType.PERCENTAGE;
        BigDecimal value = new BigDecimal("15");
        
        // When
        BigDecimal result = calculationService.calculateDiscountAmount(subtotal, type, value);
        
        // Then
        // 33.33 * 0.15 = 4.9995, rounds to 5.00
        assertThat(result).isEqualByComparingTo(new BigDecimal("5.00"));
    }
    
    @Test
    void calculateDiscountAmount_withFixedAmountDiscount_returnsValue() {
        // Given
        BigDecimal subtotal = new BigDecimal("100.00");
        DiscountType type = DiscountType.FIXED_AMOUNT;
        BigDecimal value = new BigDecimal("15.50");
        
        // When
        BigDecimal result = calculationService.calculateDiscountAmount(subtotal, type, value);
        
        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("15.50"));
        assertThat(result.scale()).isEqualTo(2);
    }
    
    @Test
    void calculateDiscountAmount_withFixedAmount_ensuresTwoDecimalPlaces() {
        // Given
        BigDecimal subtotal = new BigDecimal("100.00");
        DiscountType type = DiscountType.FIXED_AMOUNT;
        BigDecimal value = new BigDecimal("10");
        
        // When
        BigDecimal result = calculationService.calculateDiscountAmount(subtotal, type, value);
        
        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(result.scale()).isEqualTo(2);
    }
    
    @Test
    void calculateDiscountAmount_withZeroPercentage_returnsZero() {
        // Given
        BigDecimal subtotal = new BigDecimal("100.00");
        DiscountType type = DiscountType.PERCENTAGE;
        BigDecimal value = BigDecimal.ZERO;
        
        // When
        BigDecimal result = calculationService.calculateDiscountAmount(subtotal, type, value);
        
        // Then
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }
    
    @Test
    void calculateTotal_withAllComponents_calculatesCorrectly() {
        // Given
        BigDecimal subtotal = new BigDecimal("100.00");
        BigDecimal tax = new BigDecimal("19.00");
        BigDecimal discount = new BigDecimal("10.00");
        
        // When
        BigDecimal result = calculationService.calculateTotal(subtotal, tax, discount);
        
        // Then
        // 100.00 + 19.00 - 10.00 = 109.00
        assertThat(result).isEqualByComparingTo(new BigDecimal("109.00"));
        assertThat(result.scale()).isEqualTo(2);
    }
    
    @Test
    void calculateTotal_withNoDiscount_calculatesCorrectly() {
        // Given
        BigDecimal subtotal = new BigDecimal("50.00");
        BigDecimal tax = new BigDecimal("9.50");
        BigDecimal discount = BigDecimal.ZERO;
        
        // When
        BigDecimal result = calculationService.calculateTotal(subtotal, tax, discount);
        
        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("59.50"));
    }
    
    @Test
    void calculateTotal_withDecimalValues_roundsCorrectly() {
        // Given
        BigDecimal subtotal = new BigDecimal("33.335");
        BigDecimal tax = new BigDecimal("6.334");
        BigDecimal discount = new BigDecimal("5.005");
        
        // When
        BigDecimal result = calculationService.calculateTotal(subtotal, tax, discount);
        
        // Then
        assertThat(result.scale()).isEqualTo(2);
    }
    
    @Test
    void calculateChange_withExactAmount_returnsZero() {
        // Given
        BigDecimal total = new BigDecimal("50.00");
        BigDecimal amountReceived = new BigDecimal("50.00");
        
        // When
        BigDecimal result = calculationService.calculateChange(total, amountReceived);
        
        // Then
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }
    
    @Test
    void calculateChange_withExcessAmount_calculatesCorrectly() {
        // Given
        BigDecimal total = new BigDecimal("47.50");
        BigDecimal amountReceived = new BigDecimal("50.00");
        
        // When
        BigDecimal result = calculationService.calculateChange(total, amountReceived);
        
        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("2.50"));
        assertThat(result.scale()).isEqualTo(2);
    }
    
    @Test
    void calculateChange_withLargeAmount_handlesCorrectly() {
        // Given
        BigDecimal total = new BigDecimal("123.45");
        BigDecimal amountReceived = new BigDecimal("200.00");
        
        // When
        BigDecimal result = calculationService.calculateChange(total, amountReceived);
        
        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("76.55"));
    }
    
    @Test
    void recalculateSaleTotals_withItemsAndNoDiscount_calculatesAllTotals() {
        // Given
        Sale sale = new Sale();
        List<SaleItem> items = new ArrayList<>();
        items.add(createSaleItem(new BigDecimal("10.00")));
        items.add(createSaleItem(new BigDecimal("20.00")));
        sale.setItems(items);
        
        // When
        calculationService.recalculateSaleTotals(sale);
        
        // Then
        assertThat(sale.getSubtotal()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(sale.getTax()).isEqualByComparingTo(new BigDecimal("5.70")); // 30 * 0.19
        assertThat(sale.getDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(sale.getTotal()).isEqualByComparingTo(new BigDecimal("35.70")); // 30 + 5.70
    }
    
    @Test
    void recalculateSaleTotals_withItemsAndPercentageDiscount_calculatesAllTotals() {
        // Given
        Sale sale = new Sale();
        List<SaleItem> items = new ArrayList<>();
        items.add(createSaleItem(new BigDecimal("100.00")));
        sale.setItems(items);
        sale.setDiscountType(DiscountType.PERCENTAGE);
        sale.setDiscountValue(new BigDecimal("10"));
        
        // When
        calculationService.recalculateSaleTotals(sale);
        
        // Then
        assertThat(sale.getSubtotal()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(sale.getTax()).isEqualByComparingTo(new BigDecimal("19.00")); // 100 * 0.19
        assertThat(sale.getDiscount()).isEqualByComparingTo(new BigDecimal("10.00")); // 100 * 0.10
        assertThat(sale.getTotal()).isEqualByComparingTo(new BigDecimal("109.00")); // 100 + 19 - 10
    }
    
    @Test
    void recalculateSaleTotals_withItemsAndFixedDiscount_calculatesAllTotals() {
        // Given
        Sale sale = new Sale();
        List<SaleItem> items = new ArrayList<>();
        items.add(createSaleItem(new BigDecimal("50.00")));
        sale.setItems(items);
        sale.setDiscountType(DiscountType.FIXED_AMOUNT);
        sale.setDiscountValue(new BigDecimal("5.00"));
        
        // When
        calculationService.recalculateSaleTotals(sale);
        
        // Then
        assertThat(sale.getSubtotal()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(sale.getTax()).isEqualByComparingTo(new BigDecimal("9.50")); // 50 * 0.19
        assertThat(sale.getDiscount()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(sale.getTotal()).isEqualByComparingTo(new BigDecimal("54.50")); // 50 + 9.50 - 5
    }
    
    @Test
    void recalculateSaleTotals_withEmptyItems_setsZeroTotals() {
        // Given
        Sale sale = new Sale();
        sale.setItems(new ArrayList<>());
        
        // When
        calculationService.recalculateSaleTotals(sale);
        
        // Then
        assertThat(sale.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(sale.getTax()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(sale.getDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(sale.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }
    
    @Test
    void recalculateSaleTotals_ensuresPrecisionAndRounding() {
        // Given
        Sale sale = new Sale();
        List<SaleItem> items = new ArrayList<>();
        items.add(createSaleItem(new BigDecimal("33.33")));
        sale.setItems(items);
        sale.setDiscountType(DiscountType.PERCENTAGE);
        sale.setDiscountValue(new BigDecimal("15"));
        
        // When
        calculationService.recalculateSaleTotals(sale);
        
        // Then
        assertThat(sale.getSubtotal().scale()).isEqualTo(2);
        assertThat(sale.getTax().scale()).isEqualTo(2);
        assertThat(sale.getDiscount().scale()).isEqualTo(2);
        assertThat(sale.getTotal().scale()).isEqualTo(2);
    }
    
    // Helper method to create a SaleItem with a specific line total
    private SaleItem createSaleItem(BigDecimal lineTotal) {
        SaleItem item = new SaleItem();
        item.setLineTotal(lineTotal.setScale(2, RoundingMode.HALF_UP));
        return item;
    }
}
