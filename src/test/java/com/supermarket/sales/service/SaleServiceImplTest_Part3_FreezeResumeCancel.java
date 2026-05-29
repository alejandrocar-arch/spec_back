package com.supermarket.sales.service;

import com.supermarket.sales.dto.response.FrozenSaleDTO;
import com.supermarket.sales.dto.response.SaleDTO;
import com.supermarket.sales.entity.Sale;
import com.supermarket.sales.entity.SaleItem;
import com.supermarket.sales.enums.SaleStatus;
import com.supermarket.sales.exception.BusinessException;
import com.supermarket.sales.exception.SaleAlreadyCancelledException;
import com.supermarket.sales.exception.SaleNotFrozenException;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SaleServiceImpl - Part 3: Freeze, Resume, and Cancel.
 * Tests freezeSale, resumeSale, getFrozenSalesByTerminal, and cancelSale methods.
 */
@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest_Part3_FreezeResumeCancel {
    
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
    
    // ========== FREEZE SALE TESTS ==========
    
    @Test
    void freezeSale_onActiveSale_freezesSuccessfully() {
        // Given
        Long saleId = 1L;
        Sale sale = createSale(saleId, SaleStatus.ACTIVE);
        SaleDTO saleDTO = createSaleDTO(saleId, SaleStatus.FROZEN);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(saleDTO);
        
        // When
        SaleDTO result = saleService.freezeSale(saleId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.FROZEN);
        assertThat(sale.getFrozenAt()).isNotNull();
        verify(saleRepository).save(sale);
    }
    
    @Test
    void freezeSale_preservesItemsAndTotals() {
        // Given
        Long saleId = 1L;
        Sale sale = createSaleWithItems(saleId, SaleStatus.ACTIVE);
        BigDecimal originalSubtotal = sale.getSubtotal();
        BigDecimal originalTotal = sale.getTotal();
        int originalItemCount = sale.getItems().size();
        
        SaleDTO saleDTO = createSaleDTO(saleId, SaleStatus.FROZEN);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(saleDTO);
        
        // When
        saleService.freezeSale(saleId);
        
        // Then
        assertThat(sale.getSubtotal()).isEqualByComparingTo(originalSubtotal);
        assertThat(sale.getTotal()).isEqualByComparingTo(originalTotal);
        assertThat(sale.getItems()).hasSize(originalItemCount);
    }
    
    @Test
    void freezeSale_onNonActiveSale_throwsException() {
        // Given
        Long saleId = 1L;
        Sale sale = createSale(saleId, SaleStatus.COMPLETED);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.freezeSale(saleId))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Only active sales can be frozen");
        
        verify(saleRepository, never()).save(any());
    }
    
    // ========== RESUME SALE TESTS ==========
    
    @Test
    void resumeSale_onFrozenSale_resumesSuccessfully() {
        // Given
        Long saleId = 1L;
        Sale sale = createSale(saleId, SaleStatus.FROZEN);
        sale.setFrozenAt(LocalDateTime.now().minusMinutes(10));
        
        SaleDTO saleDTO = createSaleDTO(saleId, SaleStatus.ACTIVE);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(saleDTO);
        
        // When
        SaleDTO result = saleService.resumeSale(saleId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.ACTIVE);
        verify(saleRepository).save(sale);
    }
    
    @Test
    void resumeSale_preservesItemsAndTotals() {
        // Given
        Long saleId = 1L;
        Sale sale = createSaleWithItems(saleId, SaleStatus.FROZEN);
        sale.setFrozenAt(LocalDateTime.now().minusMinutes(10));
        
        BigDecimal originalSubtotal = sale.getSubtotal();
        BigDecimal originalTotal = sale.getTotal();
        int originalItemCount = sale.getItems().size();
        
        SaleDTO saleDTO = createSaleDTO(saleId, SaleStatus.ACTIVE);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(saleDTO);
        
        // When
        saleService.resumeSale(saleId);
        
        // Then
        assertThat(sale.getSubtotal()).isEqualByComparingTo(originalSubtotal);
        assertThat(sale.getTotal()).isEqualByComparingTo(originalTotal);
        assertThat(sale.getItems()).hasSize(originalItemCount);
    }
    
    @Test
    void resumeSale_onNonFrozenSale_throwsException() {
        // Given
        Long saleId = 1L;
        Sale sale = createSale(saleId, SaleStatus.ACTIVE);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.resumeSale(saleId))
            .isInstanceOf(SaleNotFrozenException.class)
            .hasMessage("Only frozen sales can be resumed");
        
        verify(saleRepository, never()).save(any());
    }
    
    // ========== GET FROZEN SALES BY TERMINAL TESTS ==========
    
    @Test
    void getFrozenSalesByTerminal_returnsAllFrozenSales() {
        // Given
        String terminalId = "TERM-001";
        List<Sale> frozenSales = Arrays.asList(
            createSaleWithItems(1L, SaleStatus.FROZEN),
            createSaleWithItems(2L, SaleStatus.FROZEN)
        );
        frozenSales.get(0).setFrozenAt(LocalDateTime.now().minusMinutes(20));
        frozenSales.get(1).setFrozenAt(LocalDateTime.now().minusMinutes(10));
        
        when(saleRepository.findFrozenSalesByTerminal(terminalId)).thenReturn(frozenSales);
        
        // When
        List<FrozenSaleDTO> result = saleService.getFrozenSalesByTerminal(terminalId);
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSaleId()).isEqualTo(1L);
        assertThat(result.get(0).getItemCount()).isEqualTo(2);
        assertThat(result.get(0).getFrozenAt()).isNotNull();
        assertThat(result.get(1).getSaleId()).isEqualTo(2L);
        
        verify(saleRepository).findFrozenSalesByTerminal(terminalId);
    }
    
    @Test
    void getFrozenSalesByTerminal_withNoFrozenSales_returnsEmptyList() {
        // Given
        String terminalId = "TERM-001";
        when(saleRepository.findFrozenSalesByTerminal(terminalId)).thenReturn(new ArrayList<>());
        
        // When
        List<FrozenSaleDTO> result = saleService.getFrozenSalesByTerminal(terminalId);
        
        // Then
        assertThat(result).isEmpty();
        verify(saleRepository).findFrozenSalesByTerminal(terminalId);
    }
    
    // ========== CANCEL SALE TESTS ==========
    
    @Test
    void cancelSale_onActiveSale_cancelsSuccessfully() {
        // Given
        Long saleId = 1L;
        String reason = "Customer changed mind";
        Sale sale = createSale(saleId, SaleStatus.ACTIVE);
        SaleDTO saleDTO = createSaleDTO(saleId, SaleStatus.CANCELLED);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(saleDTO);
        
        // When
        SaleDTO result = saleService.cancelSale(saleId, reason);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.CANCELLED);
        assertThat(sale.getCancellationReason()).isEqualTo(reason);
        assertThat(sale.getCancelledAt()).isNotNull();
        verify(saleRepository).save(sale);
    }
    
    @Test
    void cancelSale_onFrozenSale_cancelsSuccessfully() {
        // Given
        Long saleId = 1L;
        String reason = "Terminal closing";
        Sale sale = createSale(saleId, SaleStatus.FROZEN);
        sale.setFrozenAt(LocalDateTime.now().minusMinutes(30));
        
        SaleDTO saleDTO = createSaleDTO(saleId, SaleStatus.CANCELLED);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(saleDTO);
        
        // When
        SaleDTO result = saleService.cancelSale(saleId, reason);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.CANCELLED);
        assertThat(sale.getCancellationReason()).isEqualTo(reason);
        verify(saleRepository).save(sale);
    }
    
    @Test
    void cancelSale_onCompletedSale_throwsException() {
        // Given
        Long saleId = 1L;
        String reason = "Invalid reason";
        Sale sale = createSale(saleId, SaleStatus.COMPLETED);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.cancelSale(saleId, reason))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Cannot cancel completed or returned sale");
        
        verify(saleRepository, never()).save(any());
    }
    
    @Test
    void cancelSale_onReturnedSale_throwsException() {
        // Given
        Long saleId = 1L;
        String reason = "Invalid reason";
        Sale sale = createSale(saleId, SaleStatus.RETURNED);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.cancelSale(saleId, reason))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Cannot cancel completed or returned sale");
        
        verify(saleRepository, never()).save(any());
    }
    
    @Test
    void cancelSale_onPartiallyReturnedSale_throwsException() {
        // Given
        Long saleId = 1L;
        String reason = "Invalid reason";
        Sale sale = createSale(saleId, SaleStatus.PARTIALLY_RETURNED);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.cancelSale(saleId, reason))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Cannot cancel completed or returned sale");
        
        verify(saleRepository, never()).save(any());
    }
    
    @Test
    void cancelSale_onAlreadyCancelledSale_throwsException() {
        // Given
        Long saleId = 1L;
        String reason = "Another reason";
        Sale sale = createSale(saleId, SaleStatus.CANCELLED);
        sale.setCancellationReason("Already cancelled");
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.cancelSale(saleId, reason))
            .isInstanceOf(SaleAlreadyCancelledException.class)
            .hasMessage("Sale is already cancelled");
        
        verify(saleRepository, never()).save(any());
    }
    
    @Test
    void cancelSale_withValidReason_recordsReason() {
        // Given
        Long saleId = 1L;
        String reason = "Customer requested cancellation due to pricing error";
        Sale sale = createSale(saleId, SaleStatus.ACTIVE);
        SaleDTO saleDTO = createSaleDTO(saleId, SaleStatus.CANCELLED);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(saleDTO);
        
        // When
        saleService.cancelSale(saleId, reason);
        
        // Then
        assertThat(sale.getCancellationReason()).isEqualTo(reason);
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
    
    private Sale createSaleWithItems(Long id, SaleStatus status) {
        Sale sale = createSale(id, status);
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
        item1.calculateLineTotal();
        
        SaleItem item2 = new SaleItem();
        item2.setId(2L);
        item2.setProductId(101L);
        item2.setProductName("Product B");
        item2.setBarcode("BAR456");
        item2.setUnitPrice(new BigDecimal("50.00"));
        item2.setQuantity(1);
        item2.calculateLineTotal();
        
        sale.getItems().add(item1);
        sale.getItems().add(item2);
        
        return sale;
    }
    
    private SaleDTO createSaleDTO(Long id, SaleStatus status) {
        SaleDTO dto = new SaleDTO();
        dto.setId(id);
        dto.setTerminalId("TERM-001");
        dto.setCashierId("CASH-001");
        dto.setStatus(status);
        dto.setItems(new ArrayList<>());
        return dto;
    }
}
