package com.supermarket.sales.service;

import com.supermarket.sales.dto.external.CustomerDTO;
import com.supermarket.sales.dto.request.ApplyDiscountRequest;
import com.supermarket.sales.dto.request.CheckoutRequest;
import com.supermarket.sales.dto.response.CheckoutResponse;
import com.supermarket.sales.dto.response.ReceiptDTO;
import com.supermarket.sales.dto.response.SaleDTO;
import com.supermarket.sales.entity.Sale;
import com.supermarket.sales.entity.SaleItem;
import com.supermarket.sales.enums.CreditStatus;
import com.supermarket.sales.enums.DiscountType;
import com.supermarket.sales.enums.PaymentType;
import com.supermarket.sales.enums.SaleStatus;
import com.supermarket.sales.exception.*;
import com.supermarket.sales.mapper.SaleMapper;
import com.supermarket.sales.repository.SaleItemRepository;
import com.supermarket.sales.repository.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SaleServiceImpl - Part 2: Discount and Checkout.
 * Tests applyDiscount and checkout methods with various payment types.
 */
@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest_Part2_DiscountAndCheckout {
    
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
    
    private static final BigDecimal TAX_RATE = new BigDecimal("0.19");
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(saleService, "taxRate", TAX_RATE);
        ReflectionTestUtils.setField(saleService, "storeName", "Test Store");
    }
    
    // ========== APPLY DISCOUNT TESTS ==========
    
    @Test
    void applyDiscount_withPercentageDiscount_appliesSuccessfully() {
        // Given
        Long saleId = 1L;
        ApplyDiscountRequest request = new ApplyDiscountRequest();
        request.setDiscountType(DiscountType.PERCENTAGE);
        request.setDiscountValue(new BigDecimal("10"));
        
        Sale sale = createSaleWithItems(saleId, new BigDecimal("100.00"));
        SaleDTO saleDTO = createSaleDTO(saleId);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(calculationService.calculateTax(any(), any())).thenReturn(new BigDecimal("19.00"));
        doNothing().when(calculationService).recalculateSaleTotals(sale);
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(saleDTO);
        
        // When
        SaleDTO result = saleService.applyDiscount(saleId, request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(sale.getDiscountType()).isEqualTo(DiscountType.PERCENTAGE);
        assertThat(sale.getDiscountValue()).isEqualByComparingTo(new BigDecimal("10"));
        verify(calculationService).recalculateSaleTotals(sale);
        verify(saleRepository).save(sale);
    }
    
    @Test
    void applyDiscount_withFixedAmountDiscount_appliesSuccessfully() {
        // Given
        Long saleId = 1L;
        ApplyDiscountRequest request = new ApplyDiscountRequest();
        request.setDiscountType(DiscountType.FIXED_AMOUNT);
        request.setDiscountValue(new BigDecimal("15.00"));
        
        Sale sale = createSaleWithItems(saleId, new BigDecimal("100.00"));
        SaleDTO saleDTO = createSaleDTO(saleId);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(calculationService.calculateTax(any(), any())).thenReturn(new BigDecimal("19.00"));
        doNothing().when(calculationService).recalculateSaleTotals(sale);
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(saleDTO);
        
        // When
        SaleDTO result = saleService.applyDiscount(saleId, request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(sale.getDiscountType()).isEqualTo(DiscountType.FIXED_AMOUNT);
        assertThat(sale.getDiscountValue()).isEqualByComparingTo(new BigDecimal("15.00"));
        verify(calculationService).recalculateSaleTotals(sale);
    }
    
    @Test
    void applyDiscount_withNegativePercentage_throwsException() {
        // Given
        Long saleId = 1L;
        ApplyDiscountRequest request = new ApplyDiscountRequest();
        request.setDiscountType(DiscountType.PERCENTAGE);
        request.setDiscountValue(new BigDecimal("-5"));
        
        Sale sale = createSaleWithItems(saleId, new BigDecimal("100.00"));
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.applyDiscount(saleId, request))
            .isInstanceOf(InvalidDiscountException.class)
            .hasMessage("Discount amount cannot be negative");
    }
    
    @Test
    void applyDiscount_withPercentageOver100_throwsException() {
        // Given
        Long saleId = 1L;
        ApplyDiscountRequest request = new ApplyDiscountRequest();
        request.setDiscountType(DiscountType.PERCENTAGE);
        request.setDiscountValue(new BigDecimal("150"));
        
        Sale sale = createSaleWithItems(saleId, new BigDecimal("100.00"));
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.applyDiscount(saleId, request))
            .isInstanceOf(InvalidDiscountException.class)
            .hasMessage("Discount percentage must be between 0 and 100");
    }
    
    @Test
    void applyDiscount_withFixedAmountExceedingTotal_throwsException() {
        // Given
        Long saleId = 1L;
        ApplyDiscountRequest request = new ApplyDiscountRequest();
        request.setDiscountType(DiscountType.FIXED_AMOUNT);
        request.setDiscountValue(new BigDecimal("200.00"));
        
        Sale sale = createSaleWithItems(saleId, new BigDecimal("100.00"));
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(calculationService.calculateTax(any(), any())).thenReturn(new BigDecimal("19.00"));
        
        // When/Then
        assertThatThrownBy(() -> saleService.applyDiscount(saleId, request))
            .isInstanceOf(InvalidDiscountException.class)
            .hasMessage("Discount cannot exceed sale total");
    }
    
    @Test
    void applyDiscount_onNonActiveSale_throwsException() {
        // Given
        Long saleId = 1L;
        ApplyDiscountRequest request = new ApplyDiscountRequest();
        request.setDiscountType(DiscountType.PERCENTAGE);
        request.setDiscountValue(new BigDecimal("10"));
        
        Sale sale = createSaleWithItems(saleId, new BigDecimal("100.00"));
        sale.setStatus(SaleStatus.COMPLETED);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.applyDiscount(saleId, request))
            .isInstanceOf(SaleNotActiveException.class)
            .hasMessage("Sale is not active");
    }
    
    // ========== CHECKOUT WITH CASH TESTS ==========
    
    @Test
    void checkout_withCashPayment_completesSuccessfully() {
        // Given
        Long saleId = 1L;
        CheckoutRequest request = new CheckoutRequest();
        request.setPaymentType(PaymentType.CASH);
        request.setAmountReceived(new BigDecimal("150.00"));
        
        Sale sale = createSaleWithItems(saleId, new BigDecimal("100.00"));
        sale.setTotal(new BigDecimal("119.00"));
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(calculationService.calculateChange(any(), any())).thenReturn(new BigDecimal("31.00"));
        doNothing().when(productService).validateStockAvailability(any(), any());
        doNothing().when(productService).decrementStock(any(), any());
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(createSaleDTO(saleId));
        
        // When
        CheckoutResponse result = saleService.checkout(saleId, request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSale()).isNotNull();
        assertThat(result.getReceipt()).isNotNull();
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.COMPLETED);
        assertThat(sale.getPaymentType()).isEqualTo(PaymentType.CASH);
        assertThat(sale.getAmountReceived()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(sale.getChange()).isEqualByComparingTo(new BigDecimal("31.00"));
        assertThat(sale.getTransactionId()).isNotNull();
        
        verify(productService, times(1)).decrementStock(any(), any());
        verify(saleRepository).save(sale);
    }
    
    @Test
    void checkout_withCashPayment_insufficientAmount_throwsException() {
        // Given
        Long saleId = 1L;
        CheckoutRequest request = new CheckoutRequest();
        request.setPaymentType(PaymentType.CASH);
        request.setAmountReceived(new BigDecimal("50.00"));
        
        Sale sale = createSaleWithItems(saleId, new BigDecimal("100.00"));
        sale.setTotal(new BigDecimal("119.00"));
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.checkout(saleId, request))
            .isInstanceOf(InvalidPaymentException.class)
            .hasMessage("Amount received is insufficient");
        
        verify(productService, never()).decrementStock(any(), any());
    }
    
    @Test
    void checkout_withEmptySale_throwsException() {
        // Given
        Long saleId = 1L;
        CheckoutRequest request = new CheckoutRequest();
        request.setPaymentType(PaymentType.CASH);
        request.setAmountReceived(new BigDecimal("100.00"));
        
        Sale sale = createSale(saleId);
        sale.setItems(new ArrayList<>());
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.checkout(saleId, request))
            .isInstanceOf(InvalidPaymentException.class)
            .hasMessage("Cannot checkout empty sale");
    }
    
    // ========== CHECKOUT WITH CREDIT TESTS ==========
    
    @Test
    void checkout_withCreditPayment_completesSuccessfully() {
        // Given
        Long saleId = 1L;
        Long customerId = 100L;
        CheckoutRequest request = new CheckoutRequest();
        request.setPaymentType(PaymentType.CREDIT);
        request.setCustomerId(customerId);
        
        Sale sale = createSaleWithItems(saleId, new BigDecimal("100.00"));
        sale.setTotal(new BigDecimal("119.00"));
        sale.setCustomerId(customerId);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        doNothing().when(customerService).validateCreditApproval(customerId);
        doNothing().when(productService).validateStockAvailability(any(), any());
        doNothing().when(productService).decrementStock(any(), any());
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(createSaleDTO(saleId));
        
        // When
        CheckoutResponse result = saleService.checkout(saleId, request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.COMPLETED);
        assertThat(sale.getPaymentType()).isEqualTo(PaymentType.CREDIT);
        assertThat(sale.getCreditReferenceNumber()).isNotNull();
        assertThat(sale.getTransactionId()).isNotNull();
        
        verify(customerService).validateCreditApproval(customerId);
        verify(productService, times(1)).decrementStock(any(), any());
    }
    
    @Test
    void checkout_withCreditPayment_noCustomer_throwsException() {
        // Given
        Long saleId = 1L;
        CheckoutRequest request = new CheckoutRequest();
        request.setPaymentType(PaymentType.CREDIT);
        request.setCustomerId(null);
        
        Sale sale = createSaleWithItems(saleId, new BigDecimal("100.00"));
        sale.setCustomerId(null);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        
        // When/Then
        assertThatThrownBy(() -> saleService.checkout(saleId, request))
            .isInstanceOf(CustomerRequiredException.class)
            .hasMessage("Customer required for credit sales");
        
        verify(productService, never()).decrementStock(any(), any());
    }
    
    @Test
    void checkout_withCreditPayment_creditNotApproved_throwsException() {
        // Given
        Long saleId = 1L;
        Long customerId = 100L;
        CheckoutRequest request = new CheckoutRequest();
        request.setPaymentType(PaymentType.CREDIT);
        request.setCustomerId(customerId);
        
        Sale sale = createSaleWithItems(saleId, new BigDecimal("100.00"));
        sale.setCustomerId(customerId);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        doThrow(new CreditNotApprovedException("Customer credit not approved"))
            .when(customerService).validateCreditApproval(customerId);
        
        // When/Then
        assertThatThrownBy(() -> saleService.checkout(saleId, request))
            .isInstanceOf(CreditNotApprovedException.class)
            .hasMessage("Customer credit not approved");
        
        verify(productService, never()).decrementStock(any(), any());
    }
    
    @Test
    void checkout_withInsufficientStock_throwsException() {
        // Given
        Long saleId = 1L;
        CheckoutRequest request = new CheckoutRequest();
        request.setPaymentType(PaymentType.CASH);
        request.setAmountReceived(new BigDecimal("150.00"));
        
        Sale sale = createSaleWithItems(saleId, new BigDecimal("100.00"));
        sale.setTotal(new BigDecimal("119.00"));
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        doThrow(new InsufficientStockException("Insufficient stock available"))
            .when(productService).validateStockAvailability(any(), any());
        
        // When/Then
        assertThatThrownBy(() -> saleService.checkout(saleId, request))
            .isInstanceOf(InsufficientStockException.class)
            .hasMessage("Insufficient stock available");
        
        verify(productService, never()).decrementStock(any(), any());
    }
    
    @Test
    void checkout_verifiesTransactionIdGeneration() {
        // Given
        Long saleId = 1L;
        CheckoutRequest request = new CheckoutRequest();
        request.setPaymentType(PaymentType.CASH);
        request.setAmountReceived(new BigDecimal("150.00"));
        
        Sale sale = createSaleWithItems(saleId, new BigDecimal("100.00"));
        sale.setTotal(new BigDecimal("119.00"));
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(calculationService.calculateChange(any(), any())).thenReturn(new BigDecimal("31.00"));
        doNothing().when(productService).validateStockAvailability(any(), any());
        doNothing().when(productService).decrementStock(any(), any());
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(createSaleDTO(saleId));
        
        // When
        CheckoutResponse result = saleService.checkout(saleId, request);
        
        // Then
        assertThat(sale.getTransactionId()).startsWith("TXN-");
        assertThat(result.getReceipt().getTransactionId()).isEqualTo(sale.getTransactionId());
    }
    
    @Test
    void checkout_verifiesReceiptGeneration() {
        // Given
        Long saleId = 1L;
        CheckoutRequest request = new CheckoutRequest();
        request.setPaymentType(PaymentType.CASH);
        request.setAmountReceived(new BigDecimal("150.00"));
        
        Sale sale = createSaleWithItems(saleId, new BigDecimal("100.00"));
        sale.setTotal(new BigDecimal("119.00"));
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(calculationService.calculateChange(any(), any())).thenReturn(new BigDecimal("31.00"));
        doNothing().when(productService).validateStockAvailability(any(), any());
        doNothing().when(productService).decrementStock(any(), any());
        when(saleRepository.save(sale)).thenReturn(sale);
        when(saleMapper.toDTO(sale)).thenReturn(createSaleDTO(saleId));
        
        // When
        CheckoutResponse result = saleService.checkout(saleId, request);
        
        // Then
        ReceiptDTO receipt = result.getReceipt();
        assertThat(receipt).isNotNull();
        assertThat(receipt.getStoreName()).isEqualTo("Test Store");
        assertThat(receipt.getTerminalId()).isEqualTo("TERM-001");
        assertThat(receipt.getPaymentType()).isEqualTo(PaymentType.CASH);
        assertThat(receipt.getItems()).hasSize(1);
    }
    
    // ========== HELPER METHODS ==========
    
    private Sale createSale(Long id) {
        Sale sale = new Sale();
        sale.setId(id);
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
    
    private Sale createSaleWithItems(Long id, BigDecimal subtotal) {
        Sale sale = createSale(id);
        sale.setSubtotal(subtotal);
        
        SaleItem item = new SaleItem();
        item.setId(1L);
        item.setProductId(100L);
        item.setProductName("Product A");
        item.setBarcode("BAR123");
        item.setUnitPrice(new BigDecimal("10.00"));
        item.setQuantity(10);
        item.calculateLineTotal();
        
        sale.getItems().add(item);
        return sale;
    }
    
    private SaleDTO createSaleDTO(Long id) {
        SaleDTO dto = new SaleDTO();
        dto.setId(id);
        dto.setTerminalId("TERM-001");
        dto.setCashierId("CASH-001");
        dto.setStatus(SaleStatus.ACTIVE);
        dto.setItems(new ArrayList<>());
        return dto;
    }
}
