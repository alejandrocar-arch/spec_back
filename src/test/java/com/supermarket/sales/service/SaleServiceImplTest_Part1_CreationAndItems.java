package com.supermarket.sales.service;

import com.supermarket.sales.dto.external.ProductDTO;
import com.supermarket.sales.dto.request.CreateSaleRequest;
import com.supermarket.sales.dto.response.SaleDTO;
import com.supermarket.sales.entity.Sale;
import com.supermarket.sales.entity.SaleItem;
import com.supermarket.sales.enums.SaleStatus;
import com.supermarket.sales.exception.InsufficientStockException;
import com.supermarket.sales.exception.ProductNotFoundException;
import com.supermarket.sales.exception.SaleItemNotFoundException;
import com.supermarket.sales.exception.SaleNotActiveException;
import com.supermarket.sales.exception.SaleNotFoundException;
import com.supermarket.sales.mapper.SaleMapper;
import com.supermarket.sales.repository.SaleItemRepository;
import com.supermarket.sales.repository.SaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SaleServiceImpl - Part 1: Sale Creation and Item Management.
 * Tests createSale, addItem, updateQuantity, removeItem, and getSale methods.
 */
@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest_Part1_CreationAndItems {
    
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
    
    // ========== CREATE SALE TESTS ==========
    
    @Test
    void createSale_withValidRequest_returnsSaleWithActiveStatus() {
        // Given
        CreateSaleRequest request = new CreateSaleRequest();
        request.setTerminalId("TERM-001");
        request.setCashierId("CASH-001");
        request.setCustomerId(null);
        
        Sale sale = createSale(1L, "TERM-001", "CASH-001", null, SaleStatus.ACTIVE);
        SaleDTO saleDTO = createSaleDTO(1L, "TERM-001", "CASH-001", SaleStatus.ACTIVE);
        
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(saleDTO);
        
        // When
        SaleDTO result = saleService.createSale(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTerminalId()).isEqualTo("TERM-001");
        assertThat(result.getCashierId()).isEqualTo("CASH-001");
        assertThat(result.getStatus()).isEqualTo(SaleStatus.ACTIVE);
        
        ArgumentCaptor<Sale> saleCaptor = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepository).save(saleCaptor.capture());
        Sale capturedSale = saleCaptor.getValue();
        assertThat(capturedSale.getTerminalId()).isEqualTo("TERM-001");
        assertThat(capturedSale.getCashierId()).isEqualTo("CASH-001");
        assertThat(capturedSale.getStatus()).isEqualTo(SaleStatus.ACTIVE);
    }
    
    @Test
    void createSale_withCustomerId_associatesCustomer() {
        // Given
        CreateSaleRequest request = new CreateSaleRequest();
        request.setTerminalId("TERM-001");
        request.setCashierId("CASH-001");
        request.setCustomerId(100L);
        
        Sale sale = createSale(1L, "TERM-001", "CASH-001", 100L, SaleStatus.ACTIVE);
        SaleDTO saleDTO = createSaleDTO(1L, "TERM-001", "CASH-001", SaleStatus.ACTIVE);
        saleDTO.setCustomerId(100L);
        
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(saleDTO);
        
        // When
        SaleDTO result = saleService.createSale(request);
        
        // Then
        assertThat(result.getCustomerId()).isEqualTo(100L);
        verify(saleRepository).save(any(Sale.class));
    }
    
    // ========== GET SALE BY ID TESTS ==========
    
    @Test
    void getSaleById_withValidId_returnsSale() {
        // Given
        Long saleId = 1L;
        Sale sale = createSale(saleId, "TERM-001", "CASH-001", null, SaleStatus.ACTIVE);
        SaleDTO saleDTO = createSaleDTO(saleId, "TERM-001", "CASH-001", SaleStatus.ACTIVE);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(saleMapper.toDTO(sale)).thenReturn(saleDTO);
        
        // When
        SaleDTO result = saleService.getSaleById(saleId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(saleId);
        verify(saleRepository).findById(saleId);
    }
    
    @Test
    void getSaleById_withNonExistentId_throwsNotFoundException() {
        // Given
        Long saleId = 999L;
        when(saleRepository.findById(saleId)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> saleService.getSaleById(saleId))
            .isInstanceOf(SaleNotFoundException.class)
            .hasMessage("Sale not found");
        verify(saleRepository).findById(saleId);
    }
    
    // ========== GET SALES BY TERMINAL TESTS ==========
    
    @Test
    void getSalesByTerminal_withNoFilters_returnsAllSales() {
        // Given
        String terminalId = "TERM-001";
        List<Sale> sales = Arrays.asList(
            createSale(1L, terminalId, "CASH-001", null, SaleStatus.ACTIVE),
            createSale(2L, terminalId, "CASH-001", null, SaleStatus.COMPLETED)
        );
        List<SaleDTO> saleDTOs = Arrays.asList(
            createSaleDTO(1L, terminalId, "CASH-001", SaleStatus.ACTIVE),
            createSaleDTO(2L, terminalId, "CASH-001", SaleStatus.COMPLETED)
        );
        
        when(saleRepository.findByTerminalIdOrderByCreatedAtDesc(terminalId)).thenReturn(sales);
        when(saleMapper.toDTO(sales.get(0))).thenReturn(saleDTOs.get(0));
        when(saleMapper.toDTO(sales.get(1))).thenReturn(saleDTOs.get(1));
        
        // When
        List<SaleDTO> result = saleService.getSalesByTerminal(terminalId, null, null, null);
        
        // Then
        assertThat(result).hasSize(2);
        verify(saleRepository).findByTerminalIdOrderByCreatedAtDesc(terminalId);
    }
    
    @Test
    void getSalesByTerminal_withStatusFilter_returnsFilteredSales() {
        // Given
        String terminalId = "TERM-001";
        SaleStatus status = SaleStatus.ACTIVE;
        List<Sale> sales = Arrays.asList(
            createSale(1L, terminalId, "CASH-001", null, SaleStatus.ACTIVE)
        );
        List<SaleDTO> saleDTOs = Arrays.asList(
            createSaleDTO(1L, terminalId, "CASH-001", SaleStatus.ACTIVE)
        );
        
        when(saleRepository.findByTerminalIdAndStatus(terminalId, status)).thenReturn(sales);
        when(saleMapper.toDTO(sales.get(0))).thenReturn(saleDTOs.get(0));
        
        // When
        List<SaleDTO> result = saleService.getSalesByTerminal(terminalId, status, null, null);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(SaleStatus.ACTIVE);
        verify(saleRepository).findByTerminalIdAndStatus(terminalId, status);
    }
    
    // ========== ADD ITEM BY PRODUCT ID TESTS ==========
    
    @Test
    void addItemByProductId_withNewProduct_addsItemToSale() {
        // Given
        Long saleId = 1L;
        Long productId = 100L;
        Integer quantity = 2;
        
        Sale sale = createSale(saleId, "TERM-001", "CASH-001", null, SaleStatus.ACTIVE);
        ProductDTO product = createProductDTO(productId, "Product A", "BAR123", new BigDecimal("10.00"), 50);
        SaleDTO saleDTO = createSaleDTO(saleId, "TERM-001", "CASH-001", SaleStatus.ACTIVE);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(productService.getById(productId)).thenReturn(product);
        doNothing().when(productService).validateStockAvailability(productId, quantity);
        doNothing().when(calculationService).recalculateSaleTotals(sale);
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(saleDTO);
        
        // When
        SaleDTO result = saleService.addItemByProductId(saleId, productId, quantity);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(sale.getItems()).hasSize(1);
        SaleItem addedItem = sale.getItems().get(0);
        assertThat(addedItem.getProductId()).isEqualTo(productId);
        assertThat(addedItem.getQuantity()).isEqualTo(quantity);
        assertThat(addedItem.getProductName()).isEqualTo("Product A");
        
        verify(productService).getById(productId);
        verify(productService).validateStockAvailability(productId, quantity);
        verify(calculationService).recalculateSaleTotals(sale);
        verify(saleRepository).save(sale);
    }
    
    @Test
    void addItemByProductId_withExistingProduct_incrementsQuantity() {
        // Given
        Long saleId = 1L;
        Long productId = 100L;
        Integer additionalQuantity = 3;
        
        Sale sale = createSale(saleId, "TERM-001", "CASH-001", null, SaleStatus.ACTIVE);
        SaleItem existingItem = createSaleItem(1L, productId, "Product A", "BAR123", new BigDecimal("10.00"), 2);
        sale.getItems().add(existingItem);
        
        ProductDTO product = createProductDTO(productId, "Product A", "BAR123", new BigDecimal("10.00"), 50);
        SaleDTO saleDTO = createSaleDTO(saleId, "TERM-001", "CASH-001", SaleStatus.ACTIVE);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(productService.getById(productId)).thenReturn(product);
        doNothing().when(productService).validateStockAvailability(productId, 5); // 2 + 3
        doNothing().when(calculationService).recalculateSaleTotals(sale);
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(saleDTO);
        
        // When
        SaleDTO result = saleService.addItemByProductId(saleId, productId, additionalQuantity);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(sale.getItems()).hasSize(1);
        assertThat(existingItem.getQuantity()).isEqualTo(5);
        
        verify(productService).validateStockAvailability(productId, 5);
        verify(calculationService).recalculateSaleTotals(sale);
    }
    
    @Test
    void addItemByProductId_withInsufficientStock_throwsException() {
        // Given
        Long saleId = 1L;
        Long productId = 100L;
        Integer quantity = 100;
        
        Sale sale = createSale(saleId, "TERM-001", "CASH-001", null, SaleStatus.ACTIVE);
        ProductDTO product = createProductDTO(productId, "Product A", "BAR123", new BigDecimal("10.00"), 50);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(productService.getById(productId)).thenReturn(product);
        doThrow(new InsufficientStockException("Insufficient stock available"))
            .when(productService).validateStockAvailability(productId, quantity);
        
        // When/Then
        assertThatThrownBy(() -> saleService.addItemByProductId(saleId, productId, quantity))
            .isInstanceOf(InsufficientStockException.class)
            .hasMessage("Insufficient stock available");
        
        verify(productService).validateStockAvailability(productId, quantity);
        verify(saleRepository, never()).save(any());
    }
    
    @Test
    void addItemByProductId_withNonActiveSale_throwsException() {
        // Given
        Long saleId = 1L;
        Long productId = 100L;
        Integer quantity = 2;
        
        Sale sale = createSale(saleId, "TERM-001", "CASH-001", null, SaleStatus.COMPLETED);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.addItemByProductId(saleId, productId, quantity))
            .isInstanceOf(SaleNotActiveException.class)
            .hasMessage("Sale is not active");
        
        verify(productService, never()).getById(any());
    }
    
    @Test
    void addItemByProductId_withNonExistentProduct_throwsException() {
        // Given
        Long saleId = 1L;
        Long productId = 999L;
        Integer quantity = 2;
        
        Sale sale = createSale(saleId, "TERM-001", "CASH-001", null, SaleStatus.ACTIVE);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(productService.getById(productId)).thenThrow(new ProductNotFoundException("Product not found"));
        
        // When/Then
        assertThatThrownBy(() -> saleService.addItemByProductId(saleId, productId, quantity))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessage("Product not found");
        
        verify(productService).getById(productId);
    }
    
    // ========== ADD ITEM BY BARCODE TESTS ==========
    
    @Test
    void addItemByBarcode_withValidBarcode_addsItemToSale() {
        // Given
        Long saleId = 1L;
        String barcode = "BAR123";
        Integer quantity = 2;
        
        Sale sale = createSale(saleId, "TERM-001", "CASH-001", null, SaleStatus.ACTIVE);
        ProductDTO product = createProductDTO(100L, "Product A", barcode, new BigDecimal("10.00"), 50);
        SaleDTO saleDTO = createSaleDTO(saleId, "TERM-001", "CASH-001", SaleStatus.ACTIVE);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(productService.searchByBarcode(barcode)).thenReturn(product);
        doNothing().when(productService).validateStockAvailability(100L, quantity);
        doNothing().when(calculationService).recalculateSaleTotals(sale);
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(saleDTO);
        
        // When
        SaleDTO result = saleService.addItemByBarcode(saleId, barcode, quantity);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(sale.getItems()).hasSize(1);
        
        verify(productService).searchByBarcode(barcode);
        verify(calculationService).recalculateSaleTotals(sale);
    }
    
    // ========== UPDATE ITEM QUANTITY TESTS ==========
    
    @Test
    void updateItemQuantity_withValidQuantity_updatesItem() {
        // Given
        Long saleId = 1L;
        Long itemId = 10L;
        Integer newQuantity = 5;
        
        Sale sale = createSale(saleId, "TERM-001", "CASH-001", null, SaleStatus.ACTIVE);
        SaleItem item = createSaleItem(itemId, 100L, "Product A", "BAR123", new BigDecimal("10.00"), 2);
        sale.getItems().add(item);
        
        SaleDTO saleDTO = createSaleDTO(saleId, "TERM-001", "CASH-001", SaleStatus.ACTIVE);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        doNothing().when(productService).validateStockAvailability(100L, newQuantity);
        doNothing().when(calculationService).recalculateSaleTotals(sale);
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(saleDTO);
        
        // When
        SaleDTO result = saleService.updateItemQuantity(saleId, itemId, newQuantity);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(item.getQuantity()).isEqualTo(newQuantity);
        
        verify(productService).validateStockAvailability(100L, newQuantity);
        verify(calculationService).recalculateSaleTotals(sale);
    }
    
    @Test
    void updateItemQuantity_withNonExistentItem_throwsException() {
        // Given
        Long saleId = 1L;
        Long itemId = 999L;
        Integer newQuantity = 5;
        
        Sale sale = createSale(saleId, "TERM-001", "CASH-001", null, SaleStatus.ACTIVE);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.updateItemQuantity(saleId, itemId, newQuantity))
            .isInstanceOf(SaleItemNotFoundException.class)
            .hasMessage("Item not found in sale");
        
        verify(saleRepository, never()).save(any());
    }
    
    @Test
    void updateItemQuantity_withNonActiveSale_throwsException() {
        // Given
        Long saleId = 1L;
        Long itemId = 10L;
        Integer newQuantity = 5;
        
        Sale sale = createSale(saleId, "TERM-001", "CASH-001", null, SaleStatus.FROZEN);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.updateItemQuantity(saleId, itemId, newQuantity))
            .isInstanceOf(SaleNotActiveException.class)
            .hasMessage("Sale is not active");
    }
    
    // ========== REMOVE ITEM TESTS ==========
    
    @Test
    void removeItem_withValidItem_removesFromSale() {
        // Given
        Long saleId = 1L;
        Long itemId = 10L;
        
        Sale sale = createSale(saleId, "TERM-001", "CASH-001", null, SaleStatus.ACTIVE);
        SaleItem item = createSaleItem(itemId, 100L, "Product A", "BAR123", new BigDecimal("10.00"), 2);
        sale.getItems().add(item);
        
        SaleDTO saleDTO = createSaleDTO(saleId, "TERM-001", "CASH-001", SaleStatus.ACTIVE);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        doNothing().when(calculationService).recalculateSaleTotals(sale);
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(saleDTO);
        
        // When
        SaleDTO result = saleService.removeItem(saleId, itemId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(sale.getItems()).isEmpty();
        
        verify(calculationService).recalculateSaleTotals(sale);
        verify(saleRepository).save(sale);
    }
    
    @Test
    void removeItem_withNonExistentItem_throwsException() {
        // Given
        Long saleId = 1L;
        Long itemId = 999L;
        
        Sale sale = createSale(saleId, "TERM-001", "CASH-001", null, SaleStatus.ACTIVE);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.removeItem(saleId, itemId))
            .isInstanceOf(SaleItemNotFoundException.class)
            .hasMessage("Item not found in sale");
    }
    
    @Test
    void removeItem_withNonActiveSale_throwsException() {
        // Given
        Long saleId = 1L;
        Long itemId = 10L;
        
        Sale sale = createSale(saleId, "TERM-001", "CASH-001", null, SaleStatus.COMPLETED);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.removeItem(saleId, itemId))
            .isInstanceOf(SaleNotActiveException.class)
            .hasMessage("Sale is not active");
    }
    
    // ========== HELPER METHODS ==========
    
    private Sale createSale(Long id, String terminalId, String cashierId, Long customerId, SaleStatus status) {
        Sale sale = new Sale();
        sale.setId(id);
        sale.setTerminalId(terminalId);
        sale.setCashierId(cashierId);
        sale.setCustomerId(customerId);
        sale.setStatus(status);
        sale.setItems(new ArrayList<>());
        sale.setSubtotal(BigDecimal.ZERO);
        sale.setTax(BigDecimal.ZERO);
        sale.setDiscount(BigDecimal.ZERO);
        sale.setTotal(BigDecimal.ZERO);
        return sale;
    }
    
    private SaleDTO createSaleDTO(Long id, String terminalId, String cashierId, SaleStatus status) {
        SaleDTO dto = new SaleDTO();
        dto.setId(id);
        dto.setTerminalId(terminalId);
        dto.setCashierId(cashierId);
        dto.setStatus(status);
        dto.setItems(new ArrayList<>());
        dto.setSubtotal(BigDecimal.ZERO);
        dto.setTax(BigDecimal.ZERO);
        dto.setDiscount(BigDecimal.ZERO);
        dto.setTotal(BigDecimal.ZERO);
        return dto;
    }
    
    private ProductDTO createProductDTO(Long id, String name, String barcode, BigDecimal unitPrice, Integer availableStock) {
        ProductDTO product = new ProductDTO();
        product.setId(id);
        product.setName(name);
        product.setBarcode(barcode);
        product.setUnitPrice(unitPrice);
        product.setAvailableStock(availableStock);
        product.setCategory("Test Category");
        return product;
    }
    
    private SaleItem createSaleItem(Long id, Long productId, String productName, String barcode, 
                                    BigDecimal unitPrice, Integer quantity) {
        SaleItem item = new SaleItem();
        item.setId(id);
        item.setProductId(productId);
        item.setProductName(productName);
        item.setBarcode(barcode);
        item.setUnitPrice(unitPrice);
        item.setQuantity(quantity);
        item.calculateLineTotal();
        return item;
    }
}
