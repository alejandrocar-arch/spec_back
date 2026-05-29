package com.supermarket.sales.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SaleItem entity.
 * Tests business methods: updateQuantity, calculateLineTotal, getAvailableForReturn, incrementReturnedQuantity.
 */
class SaleItemTest {
    
    // ========== UPDATE QUANTITY AND CALCULATE LINE TOTAL TESTS ==========
    
    @Test
    void updateQuantity_withNewQuantity_updatesQuantityAndRecalculatesLineTotal() {
        // Given
        SaleItem item = createSaleItem(new BigDecimal("10.00"), 2);
        
        // When
        item.updateQuantity(5);
        
        // Then
        assertThat(item.getQuantity()).isEqualTo(5);
        assertThat(item.getLineTotal()).isEqualByComparingTo(new BigDecimal("50.00"));
    }
    
    @Test
    void updateQuantity_withDecimalPrice_roundsCorrectly() {
        // Given
        SaleItem item = createSaleItem(new BigDecimal("10.333"), 1);
        
        // When
        item.updateQuantity(3);
        
        // Then
        assertThat(item.getQuantity()).isEqualTo(3);
        // 10.333 * 3 = 30.999, rounds to 31.00 with HALF_UP
        assertThat(item.getLineTotal()).isEqualByComparingTo(new BigDecimal("31.00"));
        assertThat(item.getLineTotal().scale()).isEqualTo(2);
    }
    
    @Test
    void updateQuantity_toZero_setsLineTotalToZero() {
        // Given
        SaleItem item = createSaleItem(new BigDecimal("10.00"), 5);
        
        // When
        item.updateQuantity(0);
        
        // Then
        assertThat(item.getQuantity()).isEqualTo(0);
        assertThat(item.getLineTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }
    
    @Test
    void calculateLineTotal_withValidPriceAndQuantity_calculatesCorrectly() {
        // Given
        SaleItem item = new SaleItem();
        item.setUnitPrice(new BigDecimal("15.50"));
        item.setQuantity(4);
        
        // When
        item.calculateLineTotal();
        
        // Then
        assertThat(item.getLineTotal()).isEqualByComparingTo(new BigDecimal("62.00"));
        assertThat(item.getLineTotal().scale()).isEqualTo(2);
    }
    
    @Test
    void calculateLineTotal_withRoundingNeeded_roundsHalfUp() {
        // Given
        SaleItem item = new SaleItem();
        item.setUnitPrice(new BigDecimal("10.335"));
        item.setQuantity(1);
        
        // When
        item.calculateLineTotal();
        
        // Then
        // 10.335 rounds to 10.34 with HALF_UP
        assertThat(item.getLineTotal()).isEqualByComparingTo(new BigDecimal("10.34"));
    }
    
    @Test
    void calculateLineTotal_withLargeQuantity_handlesCorrectly() {
        // Given
        SaleItem item = new SaleItem();
        item.setUnitPrice(new BigDecimal("99.99"));
        item.setQuantity(100);
        
        // When
        item.calculateLineTotal();
        
        // Then
        assertThat(item.getLineTotal()).isEqualByComparingTo(new BigDecimal("9999.00"));
    }
    
    @Test
    void calculateLineTotal_ensuresTwoDecimalPlaces() {
        // Given
        SaleItem item = new SaleItem();
        item.setUnitPrice(new BigDecimal("10"));
        item.setQuantity(3);
        
        // When
        item.calculateLineTotal();
        
        // Then
        assertThat(item.getLineTotal()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(item.getLineTotal().scale()).isEqualTo(2);
    }
    
    // ========== GET AVAILABLE FOR RETURN TESTS ==========
    
    @Test
    void getAvailableForReturn_withNoReturns_returnsFullQuantity() {
        // Given
        SaleItem item = createSaleItem(new BigDecimal("10.00"), 5);
        item.setReturnedQuantity(0);
        
        // When
        Integer available = item.getAvailableForReturn();
        
        // Then
        assertThat(available).isEqualTo(5);
    }
    
    @Test
    void getAvailableForReturn_withPartialReturn_returnsRemainingQuantity() {
        // Given
        SaleItem item = createSaleItem(new BigDecimal("10.00"), 10);
        item.setReturnedQuantity(3);
        
        // When
        Integer available = item.getAvailableForReturn();
        
        // Then
        assertThat(available).isEqualTo(7);
    }
    
    @Test
    void getAvailableForReturn_withFullReturn_returnsZero() {
        // Given
        SaleItem item = createSaleItem(new BigDecimal("10.00"), 5);
        item.setReturnedQuantity(5);
        
        // When
        Integer available = item.getAvailableForReturn();
        
        // Then
        assertThat(available).isEqualTo(0);
    }
    
    @Test
    void getAvailableForReturn_afterMultiplePartialReturns_calculatesCorrectly() {
        // Given
        SaleItem item = createSaleItem(new BigDecimal("10.00"), 20);
        item.setReturnedQuantity(5);
        
        // When
        Integer available1 = item.getAvailableForReturn();
        item.setReturnedQuantity(item.getReturnedQuantity() + 3);
        Integer available2 = item.getAvailableForReturn();
        
        // Then
        assertThat(available1).isEqualTo(15);
        assertThat(available2).isEqualTo(12);
    }
    
    // ========== INCREMENT RETURNED QUANTITY TESTS ==========
    
    @Test
    void incrementReturnedQuantity_withValidAmount_incrementsCorrectly() {
        // Given
        SaleItem item = createSaleItem(new BigDecimal("10.00"), 10);
        item.setReturnedQuantity(0);
        
        // When
        item.incrementReturnedQuantity(3);
        
        // Then
        assertThat(item.getReturnedQuantity()).isEqualTo(3);
    }
    
    @Test
    void incrementReturnedQuantity_multipleTimes_accumulatesCorrectly() {
        // Given
        SaleItem item = createSaleItem(new BigDecimal("10.00"), 10);
        item.setReturnedQuantity(0);
        
        // When
        item.incrementReturnedQuantity(2);
        item.incrementReturnedQuantity(3);
        item.incrementReturnedQuantity(1);
        
        // Then
        assertThat(item.getReturnedQuantity()).isEqualTo(6);
    }
    
    @Test
    void incrementReturnedQuantity_withZeroAmount_doesNotChange() {
        // Given
        SaleItem item = createSaleItem(new BigDecimal("10.00"), 10);
        item.setReturnedQuantity(5);
        
        // When
        item.incrementReturnedQuantity(0);
        
        // Then
        assertThat(item.getReturnedQuantity()).isEqualTo(5);
    }
    
    @Test
    void incrementReturnedQuantity_updatesAvailableForReturn() {
        // Given
        SaleItem item = createSaleItem(new BigDecimal("10.00"), 10);
        item.setReturnedQuantity(0);
        
        // When
        item.incrementReturnedQuantity(4);
        
        // Then
        assertThat(item.getReturnedQuantity()).isEqualTo(4);
        assertThat(item.getAvailableForReturn()).isEqualTo(6);
    }
    
    @Test
    void incrementReturnedQuantity_toFullQuantity_leavesNothingAvailable() {
        // Given
        SaleItem item = createSaleItem(new BigDecimal("10.00"), 8);
        item.setReturnedQuantity(0);
        
        // When
        item.incrementReturnedQuantity(8);
        
        // Then
        assertThat(item.getReturnedQuantity()).isEqualTo(8);
        assertThat(item.getAvailableForReturn()).isEqualTo(0);
    }
    
    // ========== INTEGRATION TESTS ==========
    
    @Test
    void updateQuantity_andIncrementReturned_workTogether() {
        // Given
        SaleItem item = createSaleItem(new BigDecimal("10.00"), 5);
        item.setReturnedQuantity(0);
        
        // When - update quantity to 10
        item.updateQuantity(10);
        // Then
        assertThat(item.getQuantity()).isEqualTo(10);
        assertThat(item.getLineTotal()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(item.getAvailableForReturn()).isEqualTo(10);
        
        // When - return 3 items
        item.incrementReturnedQuantity(3);
        // Then
        assertThat(item.getReturnedQuantity()).isEqualTo(3);
        assertThat(item.getAvailableForReturn()).isEqualTo(7);
        
        // When - return 4 more items
        item.incrementReturnedQuantity(4);
        // Then
        assertThat(item.getReturnedQuantity()).isEqualTo(7);
        assertThat(item.getAvailableForReturn()).isEqualTo(3);
    }
    
    // ========== HELPER METHODS ==========
    
    private SaleItem createSaleItem(BigDecimal unitPrice, Integer quantity) {
        SaleItem item = new SaleItem();
        item.setId(1L);
        item.setProductId(100L);
        item.setProductName("Test Product");
        item.setBarcode("BAR-123");
        item.setUnitPrice(unitPrice);
        item.setQuantity(quantity);
        item.setReturnedQuantity(0);
        item.calculateLineTotal();
        return item;
    }
}
