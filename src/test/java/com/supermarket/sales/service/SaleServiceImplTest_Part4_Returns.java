package com.supermarket.sales.service;

import com.supermarket.sales.dto.request.FullReturnRequest;
import com.supermarket.sales.dto.request.PartialReturnRequest;
import com.supermarket.sales.dto.request.ReturnItemRequest;
import com.supermarket.sales.dto.response.CreditNoteDTO;
import com.supermarket.sales.dto.response.ReturnResponse;
import com.supermarket.sales.entity.Sale;
import com.supermarket.sales.entity.SaleItem;
import com.supermarket.sales.enums.PaymentType;
import com.supermarket.sales.enums.SaleStatus;
import com.supermarket.sales.exception.BusinessException;
import com.supermarket.sales.exception.InvalidReturnQuantityException;
import com.supermarket.sales.exception.SaleAlreadyReturnedException;
import com.supermarket.sales.mapper.SaleMapper;
import com.supermarket.sales.repository.SaleItemRepository;
import com.supermarket.sales.repository.SaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SaleServiceImpl - Part 4: Returns.
 * Tests processFullReturn and processPartialReturn methods.
 */
@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest_Part4_Returns {
    
    @Mock
    private SaleRepository saleRepository;
    
    @Mock
    private SaleItemRepository saleItemRepository;
    
    @Mock
    private ProductService productService;
    
    @Mock
    private CustomerService customerService;
    
    @Mock
    private CalculationService calculationService;
    
    @Mock
    private SaleMapper saleMapper;
    
    @InjectMocks
    private SaleServiceImpl saleService;
    
    // ========== FULL RETURN TESTS ==========
    
    @Test
    void processFullReturn_onCompletedSale_returnsSuccessfully() {
        // Given
        Long saleId = 1L;
        FullReturnRequest request = new FullReturnRequest();
        request.setReturnReason("Defective products");
        
        Sale sale = createCompletedSale(saleId, PaymentType.CASH);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        doNothing().when(productService).incrementStock(any(), any());
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(any());
        
        // When
        ReturnResponse result = saleService.processFullReturn(saleId, request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReturnReceipt()).isNotNull();
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.RETURNED);
        assertThat(sale.getReturnReason()).isEqualTo("Defective products");
        assertThat(sale.getReturnedAt()).isNotNull();
        
        // Verify stock increment for all items
        verify(productService).incrementStock(100L, 2);
        verify(productService).incrementStock(101L, 1);
        verify(saleRepository).save(sale);
    }
    
    @Test
    void processFullReturn_onNonCompletedSale_throwsException() {
        // Given
        Long saleId = 1L;
        FullReturnRequest request = new FullReturnRequest();
        request.setReturnReason("Invalid return");
        
        Sale sale = createSale(saleId, SaleStatus.ACTIVE);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.processFullReturn(saleId, request))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Only completed sales can be returned");
        
        verify(productService, never()).incrementStock(any(), any());
    }
    
    @Test
    void processFullReturn_onAlreadyReturnedSale_throwsException() {
        // Given
        Long saleId = 1L;
        FullReturnRequest request = new FullReturnRequest();
        request.setReturnReason("Another return");
        
        Sale sale = createCompletedSale(saleId, PaymentType.CASH);
        sale.setStatus(SaleStatus.RETURNED);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.processFullReturn(saleId, request))
            .isInstanceOf(SaleAlreadyReturnedException.class)
            .hasMessage("Sale has already been fully returned");
        
        verify(productService, never()).incrementStock(any(), any());
    }
    
    @Test
    void processFullReturn_withCreditSale_generatesCreditNote() {
        // Given
        Long saleId = 1L;
        FullReturnRequest request = new FullReturnRequest();
        request.setReturnReason("Customer request");
        
        Sale sale = createCompletedSale(saleId, PaymentType.CREDIT);
        sale.setCreditReferenceNumber("CRD-12345678");
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        doNothing().when(productService).incrementStock(any(), any());
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(any());
        
        // When
        ReturnResponse result = saleService.processFullReturn(saleId, request);
        
        // Then
        assertThat(result.getCreditNote()).isNotNull();
        CreditNoteDTO creditNote = result.getCreditNote();
        assertThat(creditNote.getOriginalCreditReference()).isEqualTo("CRD-12345678");
        assertThat(creditNote.getCreditNoteNumber()).startsWith("CN-");
        assertThat(creditNote.getTotalCreditAmount()).isNotNull();
    }
    
    @Test
    void processFullReturn_withCashSale_doesNotGenerateCreditNote() {
        // Given
        Long saleId = 1L;
        FullReturnRequest request = new FullReturnRequest();
        request.setReturnReason("Customer request");
        
        Sale sale = createCompletedSale(saleId, PaymentType.CASH);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        doNothing().when(productService).incrementStock(any(), any());
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(any());
        
        // When
        ReturnResponse result = saleService.processFullReturn(saleId, request);
        
        // Then
        assertThat(result.getCreditNote()).isNull();
    }
    
    @Test
    void processFullReturn_verifiesStockIncrement() {
        // Given
        Long saleId = 1L;
        FullReturnRequest request = new FullReturnRequest();
        request.setReturnReason("Return all items");
        
        Sale sale = createCompletedSale(saleId, PaymentType.CASH);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        doNothing().when(productService).incrementStock(any(), any());
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(any());
        
        // When
        saleService.processFullReturn(saleId, request);
        
        // Then
        verify(productService).incrementStock(100L, 2);
        verify(productService).incrementStock(101L, 1);
        verifyNoMoreInteractions(productService);
    }
    
    // ========== PARTIAL RETURN TESTS ==========
    
    @Test
    void processPartialReturn_withValidQuantities_returnsSuccessfully() {
        // Given
        Long saleId = 1L;
        PartialReturnRequest request = new PartialReturnRequest();
        request.setReturnReason("One item defective");
        
        ReturnItemRequest returnItem = new ReturnItemRequest();
        returnItem.setItemId(1L);
        returnItem.setQuantity(1);
        request.setReturnItems(Arrays.asList(returnItem));
        
        Sale sale = createCompletedSale(saleId, PaymentType.CASH);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        doNothing().when(productService).incrementStock(any(), any());
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(any());
        
        // When
        ReturnResponse result = saleService.processPartialReturn(saleId, request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReturnReceipt()).isNotNull();
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.PARTIALLY_RETURNED);
        assertThat(sale.getReturnReason()).isEqualTo("One item defective");
        
        SaleItem item = sale.getItems().get(0);
        assertThat(item.getReturnedQuantity()).isEqualTo(1);
        assertThat(item.getAvailableForReturn()).isEqualTo(1);
        
        verify(productService).incrementStock(100L, 1);
        verify(saleRepository).save(sale);
    }
    
    @Test
    void processPartialReturn_multipleItems_returnsSuccessfully() {
        // Given
        Long saleId = 1L;
        PartialReturnRequest request = new PartialReturnRequest();
        request.setReturnReason("Multiple items defective");
        
        ReturnItemRequest returnItem1 = new ReturnItemRequest();
        returnItem1.setItemId(1L);
        returnItem1.setQuantity(1);
        
        ReturnItemRequest returnItem2 = new ReturnItemRequest();
        returnItem2.setItemId(2L);
        returnItem2.setQuantity(1);
        
        request.setReturnItems(Arrays.asList(returnItem1, returnItem2));
        
        Sale sale = createCompletedSale(saleId, PaymentType.CASH);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        doNothing().when(productService).incrementStock(any(), any());
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(any());
        
        // When
        ReturnResponse result = saleService.processPartialReturn(saleId, request);
        
        // Then
        assertThat(result).isNotNull();
        verify(productService).incrementStock(100L, 1);
        verify(productService).incrementStock(101L, 1);
    }
    
    @Test
    void processPartialReturn_withExcessiveQuantity_throwsException() {
        // Given
        Long saleId = 1L;
        PartialReturnRequest request = new PartialReturnRequest();
        request.setReturnReason("Invalid return");
        
        ReturnItemRequest returnItem = new ReturnItemRequest();
        returnItem.setItemId(1L);
        returnItem.setQuantity(5); // More than purchased (2)
        request.setReturnItems(Arrays.asList(returnItem));
        
        Sale sale = createCompletedSale(saleId, PaymentType.CASH);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.processPartialReturn(saleId, request))
            .isInstanceOf(InvalidReturnQuantityException.class)
            .hasMessageContaining("Return quantity exceeds purchased quantity");
        
        verify(productService, never()).incrementStock(any(), any());
    }
    
    @Test
    void processPartialReturn_multipleTimes_tracksReturnedQuantity() {
        // Given
        Long saleId = 1L;
        
        // First partial return
        PartialReturnRequest request1 = new PartialReturnRequest();
        request1.setReturnReason("First return");
        ReturnItemRequest returnItem1 = new ReturnItemRequest();
        returnItem1.setItemId(1L);
        returnItem1.setQuantity(1);
        request1.setReturnItems(Arrays.asList(returnItem1));
        
        Sale sale = createCompletedSale(saleId, PaymentType.CASH);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        doNothing().when(productService).incrementStock(any(), any());
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(any());
        
        // When - First return
        saleService.processPartialReturn(saleId, request1);
        
        // Then - Verify first return
        SaleItem item = sale.getItems().get(0);
        assertThat(item.getReturnedQuantity()).isEqualTo(1);
        assertThat(item.getAvailableForReturn()).isEqualTo(1);
        
        // Given - Second partial return
        PartialReturnRequest request2 = new PartialReturnRequest();
        request2.setReturnReason("Second return");
        ReturnItemRequest returnItem2 = new ReturnItemRequest();
        returnItem2.setItemId(1L);
        returnItem2.setQuantity(1);
        request2.setReturnItems(Arrays.asList(returnItem2));
        
        // When - Second return
        saleService.processPartialReturn(saleId, request2);
        
        // Then - Verify second return
        assertThat(item.getReturnedQuantity()).isEqualTo(2);
        assertThat(item.getAvailableForReturn()).isEqualTo(0);
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.RETURNED); // All items returned
    }
    
    @Test
    void processPartialReturn_returningAllItems_changesStatusToReturned() {
        // Given
        Long saleId = 1L;
        PartialReturnRequest request = new PartialReturnRequest();
        request.setReturnReason("Return all items");
        
        ReturnItemRequest returnItem1 = new ReturnItemRequest();
        returnItem1.setItemId(1L);
        returnItem1.setQuantity(2);
        
        ReturnItemRequest returnItem2 = new ReturnItemRequest();
        returnItem2.setItemId(2L);
        returnItem2.setQuantity(1);
        
        request.setReturnItems(Arrays.asList(returnItem1, returnItem2));
        
        Sale sale = createCompletedSale(saleId, PaymentType.CASH);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        doNothing().when(productService).incrementStock(any(), any());
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(any());
        
        // When
        ReturnResponse result = saleService.processPartialReturn(saleId, request);
        
        // Then
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.RETURNED);
    }
    
    @Test
    void processPartialReturn_onPartiallyReturnedSale_continuesSuccessfully() {
        // Given
        Long saleId = 1L;
        PartialReturnRequest request = new PartialReturnRequest();
        request.setReturnReason("Return more items");
        
        ReturnItemRequest returnItem = new ReturnItemRequest();
        returnItem.setItemId(2L);
        returnItem.setQuantity(1);
        request.setReturnItems(Arrays.asList(returnItem));
        
        Sale sale = createCompletedSale(saleId, PaymentType.CASH);
        sale.setStatus(SaleStatus.PARTIALLY_RETURNED);
        sale.getItems().get(0).incrementReturnedQuantity(1); // Already returned 1 of item 1
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        doNothing().when(productService).incrementStock(any(), any());
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(any());
        
        // When
        ReturnResponse result = saleService.processPartialReturn(saleId, request);
        
        // Then
        assertThat(result).isNotNull();
        verify(productService).incrementStock(101L, 1);
    }
    
    @Test
    void processPartialReturn_onNonCompletedSale_throwsException() {
        // Given
        Long saleId = 1L;
        PartialReturnRequest request = new PartialReturnRequest();
        request.setReturnReason("Invalid return");
        
        ReturnItemRequest returnItem = new ReturnItemRequest();
        returnItem.setItemId(1L);
        returnItem.setQuantity(1);
        request.setReturnItems(Arrays.asList(returnItem));
        
        Sale sale = createSale(saleId, SaleStatus.ACTIVE);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.processPartialReturn(saleId, request))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Only completed sales can be returned");
    }
    
    @Test
    void processPartialReturn_onFullyReturnedSale_throwsException() {
        // Given
        Long saleId = 1L;
        PartialReturnRequest request = new PartialReturnRequest();
        request.setReturnReason("Invalid return");
        
        ReturnItemRequest returnItem = new ReturnItemRequest();
        returnItem.setItemId(1L);
        returnItem.setQuantity(1);
        request.setReturnItems(Arrays.asList(returnItem));
        
        Sale sale = createCompletedSale(saleId, PaymentType.CASH);
        sale.setStatus(SaleStatus.RETURNED);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.processPartialReturn(saleId, request))
            .isInstanceOf(SaleAlreadyReturnedException.class)
            .hasMessage("Sale has already been fully returned");
    }
    
    @Test
    void processPartialReturn_withCreditSale_generatesCreditNote() {
        // Given
        Long saleId = 1L;
        PartialReturnRequest request = new PartialReturnRequest();
        request.setReturnReason("Partial return");
        
        ReturnItemRequest returnItem = new ReturnItemRequest();
        returnItem.setItemId(1L);
        returnItem.setQuantity(1);
        request.setReturnItems(Arrays.asList(returnItem));
        
        Sale sale = createCompletedSale(saleId, PaymentType.CREDIT);
        sale.setCreditReferenceNumber("CRD-87654321");
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        doNothing().when(productService).incrementStock(any(), any());
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(any());
        
        // When
        ReturnResponse result = saleService.processPartialReturn(saleId, request);
        
        // Then
        assertThat(result.getCreditNote()).isNotNull();
        assertThat(result.getCreditNote().getOriginalCreditReference()).isEqualTo("CRD-87654321");
    }
    
    // ========== HELPER METHODS ==========
    
    private Sale createSale(Long id, SaleStatus status) {
        Sale sale = new Sale();
        sale.setId(id);
        sale.setTerminalId("TERM-001");
        sale.setCashierId("CASH-001");
        sale.setStatus(status);
        sale.setItems(new ArrayList<>());
        sale.setSubtotal(BigDecimal.ZERO);
        sale.setTax(BigDecimal.ZERO);
        sale.setDiscount(BigDecimal.ZERO);
        sale.setTotal(BigDecimal.ZERO);
        return sale;
    }
    
    private Sale createCompletedSale(Long id, PaymentType paymentType) {
        Sale sale = createSale(id, SaleStatus.COMPLETED);
        sale.setPaymentType(paymentType);
        sale.setTransactionId("TXN-12345678");
        sale.setCompletedAt(LocalDateTime.now().minusDays(1));
        sale.setSubtotal(new BigDecimal("100.00"));
        sale.setTax(new BigDecimal("19.00"));
        sale.setTotal(new BigDecimal("119.00"));
        
        SaleItem item1 = new SaleItem();
        item1.setId(1L);
        item1.setProductId(100L);
        item1.setProductName("Product A");
        item1.setBarcode("BAR123");
        item1.setUnitPrice(new BigDecimal("25.00"));
        item1.setQuantity(2);
        item1.setReturnedQuantity(0);
        item1.calculateLineTotal();
        
        SaleItem item2 = new SaleItem();
        item2.setId(2L);
        item2.setProductId(101L);
        item2.setProductName("Product B");
        item2.setBarcode("BAR456");
        item2.setUnitPrice(new BigDecimal("50.00"));
        item2.setQuantity(1);
        item2.setReturnedQuantity(0);
        item2.calculateLineTotal();
        
        sale.getItems().add(item1);
        sale.getItems().add(item2);
        
        return sale;
    }
}
