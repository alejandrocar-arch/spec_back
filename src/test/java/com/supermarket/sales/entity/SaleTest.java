package com.supermarket.sales.entity;

import com.supermarket.sales.enums.PaymentType;
import com.supermarket.sales.enums.SaleStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Sale entity.
 * Tests state transition methods, validation methods, and business logic.
 */
class SaleTest {
    
    // ========== STATE TRANSITION TESTS ==========
    
    @Test
    void freeze_onActiveSale_transitionsToFrozen() {
        // Given
        Sale sale = createActiveSale();
        
        // When
        sale.freeze();
        
        // Then
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.FROZEN);
        assertThat(sale.getFrozenAt()).isNotNull();
    }
    
    @Test
    void freeze_onNonActiveSale_throwsException() {
        // Given
        Sale sale = createActiveSale();
        sale.setStatus(SaleStatus.COMPLETED);
        
        // When/Then
        assertThatThrownBy(() -> sale.freeze())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Only active sales can be frozen");
    }
    
    @Test
    void resume_onFrozenSale_transitionsToActive() {
        // Given
        Sale sale = createActiveSale();
        sale.freeze();
        
        // When
        sale.resume();
        
        // Then
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.ACTIVE);
    }
    
    @Test
    void resume_onNonFrozenSale_throwsException() {
        // Given
        Sale sale = createActiveSale();
        
        // When/Then
        assertThatThrownBy(() -> sale.resume())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Only frozen sales can be resumed");
    }
    
    @Test
    void cancel_onActiveSale_transitionsToCancelled() {
        // Given
        Sale sale = createActiveSale();
        String reason = "Customer changed mind";
        
        // When
        sale.cancel(reason);
        
        // Then
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.CANCELLED);
        assertThat(sale.getCancellationReason()).isEqualTo(reason);
        assertThat(sale.getCancelledAt()).isNotNull();
    }
    
    @Test
    void cancel_onFrozenSale_transitionsToCancelled() {
        // Given
        Sale sale = createActiveSale();
        sale.freeze();
        String reason = "Terminal closing";
        
        // When
        sale.cancel(reason);
        
        // Then
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.CANCELLED);
        assertThat(sale.getCancellationReason()).isEqualTo(reason);
    }
    
    @Test
    void cancel_onCompletedSale_throwsException() {
        // Given
        Sale sale = createActiveSale();
        sale.addItem(createSaleItem());
        sale.complete(PaymentType.CASH);
        
        // When/Then
        assertThatThrownBy(() -> sale.cancel("Invalid"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot cancel sale in current status");
    }
    
    @Test
    void complete_onActiveSaleWithItems_transitionsToCompleted() {
        // Given
        Sale sale = createActiveSale();
        sale.addItem(createSaleItem());
        
        // When
        sale.complete(PaymentType.CASH);
        
        // Then
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.COMPLETED);
        assertThat(sale.getPaymentType()).isEqualTo(PaymentType.CASH);
        assertThat(sale.getCompletedAt()).isNotNull();
    }
    
    @Test
    void complete_onEmptySale_throwsException() {
        // Given
        Sale sale = createActiveSale();
        
        // When/Then
        assertThatThrownBy(() -> sale.complete(PaymentType.CASH))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot complete sale in current status");
    }
    
    @Test
    void markAsReturned_onCompletedSale_transitionsToReturned() {
        // Given
        Sale sale = createCompletedSale();
        
        // When
        sale.markAsReturned();
        
        // Then
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.RETURNED);
        assertThat(sale.getReturnedAt()).isNotNull();
    }
    
    @Test
    void markAsReturned_onNonCompletedSale_throwsException() {
        // Given
        Sale sale = createActiveSale();
        
        // When/Then
        assertThatThrownBy(() -> sale.markAsReturned())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Only completed sales can be returned");
    }
    
    @Test
    void markAsPartiallyReturned_onCompletedSale_transitionsToPartiallyReturned() {
        // Given
        Sale sale = createCompletedSale();
        
        // When
        sale.markAsPartiallyReturned();
        
        // Then
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.PARTIALLY_RETURNED);
        assertThat(sale.getReturnedAt()).isNotNull();
    }
    
    @Test
    void markAsPartiallyReturned_multipleTimes_keepsFirstReturnedAt() {
        // Given
        Sale sale = createCompletedSale();
        
        // When
        sale.markAsPartiallyReturned();
        var firstReturnedAt = sale.getReturnedAt();
        
        sale.markAsPartiallyReturned();
        
        // Then
        assertThat(sale.getReturnedAt()).isEqualTo(firstReturnedAt);
    }
    
    // ========== VALIDATION METHOD TESTS ==========
    
    @Test
    void canAddItems_onActiveSale_returnsTrue() {
        // Given
        Sale sale = createActiveSale();
        
        // When/Then
        assertThat(sale.canAddItems()).isTrue();
    }
    
    @Test
    void canAddItems_onNonActiveSale_returnsFalse() {
        // Given
        Sale sale = createActiveSale();
        sale.setStatus(SaleStatus.FROZEN);
        
        // When/Then
        assertThat(sale.canAddItems()).isFalse();
    }
    
    @Test
    void canCheckout_onActiveSaleWithItems_returnsTrue() {
        // Given
        Sale sale = createActiveSale();
        sale.addItem(createSaleItem());
        
        // When/Then
        assertThat(sale.canCheckout()).isTrue();
    }
    
    @Test
    void canCheckout_onActiveSaleWithoutItems_returnsFalse() {
        // Given
        Sale sale = createActiveSale();
        
        // When/Then
        assertThat(sale.canCheckout()).isFalse();
    }
    
    @Test
    void canCheckout_onNonActiveSale_returnsFalse() {
        // Given
        Sale sale = createActiveSale();
        sale.addItem(createSaleItem());
        sale.setStatus(SaleStatus.FROZEN);
        
        // When/Then
        assertThat(sale.canCheckout()).isFalse();
    }
    
    @Test
    void canFreeze_onActiveSale_returnsTrue() {
        // Given
        Sale sale = createActiveSale();
        
        // When/Then
        assertThat(sale.canFreeze()).isTrue();
    }
    
    @Test
    void canFreeze_onNonActiveSale_returnsFalse() {
        // Given
        Sale sale = createActiveSale();
        sale.setStatus(SaleStatus.COMPLETED);
        
        // When/Then
        assertThat(sale.canFreeze()).isFalse();
    }
    
    @Test
    void canResume_onFrozenSale_returnsTrue() {
        // Given
        Sale sale = createActiveSale();
        sale.freeze();
        
        // When/Then
        assertThat(sale.canResume()).isTrue();
    }
    
    @Test
    void canResume_onNonFrozenSale_returnsFalse() {
        // Given
        Sale sale = createActiveSale();
        
        // When/Then
        assertThat(sale.canResume()).isFalse();
    }
    
    @Test
    void canCancel_onActiveSale_returnsTrue() {
        // Given
        Sale sale = createActiveSale();
        
        // When/Then
        assertThat(sale.canCancel()).isTrue();
    }
    
    @Test
    void canCancel_onFrozenSale_returnsTrue() {
        // Given
        Sale sale = createActiveSale();
        sale.freeze();
        
        // When/Then
        assertThat(sale.canCancel()).isTrue();
    }
    
    @Test
    void canCancel_onCompletedSale_returnsFalse() {
        // Given
        Sale sale = createCompletedSale();
        
        // When/Then
        assertThat(sale.canCancel()).isFalse();
    }
    
    @Test
    void canReturn_onCompletedSale_returnsTrue() {
        // Given
        Sale sale = createCompletedSale();
        
        // When/Then
        assertThat(sale.canReturn()).isTrue();
    }
    
    @Test
    void canReturn_onPartiallyReturnedSale_returnsTrue() {
        // Given
        Sale sale = createCompletedSale();
        sale.markAsPartiallyReturned();
        
        // When/Then
        assertThat(sale.canReturn()).isTrue();
    }
    
    @Test
    void canReturn_onActiveSale_returnsFalse() {
        // Given
        Sale sale = createActiveSale();
        
        // When/Then
        assertThat(sale.canReturn()).isFalse();
    }
    
    // ========== ADD/REMOVE ITEM TESTS ==========
    
    @Test
    void addItem_addsItemToSale() {
        // Given
        Sale sale = createActiveSale();
        SaleItem item = createSaleItem();
        
        // When
        sale.addItem(item);
        
        // Then
        assertThat(sale.getItems()).hasSize(1);
        assertThat(sale.getItems()).contains(item);
        assertThat(item.getSale()).isEqualTo(sale);
    }
    
    @Test
    void addItem_multipleItems_addsAllItems() {
        // Given
        Sale sale = createActiveSale();
        SaleItem item1 = createSaleItem();
        SaleItem item2 = createSaleItem();
        
        // When
        sale.addItem(item1);
        sale.addItem(item2);
        
        // Then
        assertThat(sale.getItems()).hasSize(2);
        assertThat(sale.getItems()).containsExactly(item1, item2);
    }
    
    @Test
    void removeItem_removesItemFromSale() {
        // Given
        Sale sale = createActiveSale();
        SaleItem item = createSaleItem();
        sale.addItem(item);
        
        // When
        sale.removeItem(item);
        
        // Then
        assertThat(sale.getItems()).isEmpty();
        assertThat(item.getSale()).isNull();
    }
    
    @Test
    void removeItem_withMultipleItems_removesOnlySpecifiedItem() {
        // Given
        Sale sale = createActiveSale();
        SaleItem item1 = createSaleItem();
        SaleItem item2 = createSaleItem();
        sale.addItem(item1);
        sale.addItem(item2);
        
        // When
        sale.removeItem(item1);
        
        // Then
        assertThat(sale.getItems()).hasSize(1);
        assertThat(sale.getItems()).contains(item2);
        assertThat(sale.getItems()).doesNotContain(item1);
    }
    
    // ========== HELPER METHODS ==========
    
    private Sale createActiveSale() {
        Sale sale = new Sale();
        sale.setId(1L);
        sale.setTerminalId("TERM-001");
        sale.setCashierId("CASH-001");
        sale.setStatus(SaleStatus.ACTIVE);
        sale.setItems(new ArrayList<>());
        sale.setSubtotal(BigDecimal.ZERO);
        sale.setTax(BigDecimal.ZERO);
        sale.setDiscount(BigDecimal.ZERO);
        sale.setTotal(BigDecimal.ZERO);
        return sale;
    }
    
    private Sale createCompletedSale() {
        Sale sale = createActiveSale();
        sale.addItem(createSaleItem());
        sale.complete(PaymentType.CASH);
        return sale;
    }
    
    private SaleItem createSaleItem() {
        SaleItem item = new SaleItem();
        item.setId(1L);
        item.setProductId(100L);
        item.setProductName("Product A");
        item.setBarcode("BAR123");
        item.setUnitPrice(new BigDecimal("10.00"));
        item.setQuantity(1);
        item.calculateLineTotal();
        return item;
    }
}
