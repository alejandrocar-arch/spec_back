package com.supermarket.sales.serverless.ventas.service;

import com.supermarket.sales.serverless.ventas.dto.CreateSaleRequest;
import com.supermarket.sales.serverless.ventas.dto.SaleDTO;
import com.supermarket.sales.serverless.ventas.exception.ValidationException;
import com.supermarket.sales.serverless.ventas.repository.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleRegistrationServiceTest {
    
    @Mock
    private SaleRepository saleRepository;
    
    private SaleRegistrationService saleRegistrationService;
    
    @BeforeEach
    void setUp() {
        saleRegistrationService = new SaleRegistrationService(saleRepository);
    }
    
    @Test
    void registerSale_withValidRequest_shouldGenerateVentaIdInUUIDFormat() {
        // Given
        CreateSaleRequest request = new CreateSaleRequest(
                "TERM-001",
                "CASH-123",
                null,
                "101",
                2,
                new BigDecimal("57.00")
        );
        
        when(saleRepository.saveSale(any(SaleDTO.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        SaleDTO result = saleRegistrationService.registerSale(request);
        
        // Then
        assertThat(result.getVentaId()).isNotNull();
        assertThat(result.getVentaId()).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }
    
    @Test
    void registerSale_withNullTerminalId_shouldThrowValidationException() {
        // Given
        CreateSaleRequest request = new CreateSaleRequest(
                null,
                "CASH-123",
                null,
                "101",
                2,
                new BigDecimal("57.00")
        );
        
        // When / Then
        assertThatThrownBy(() -> saleRegistrationService.registerSale(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("terminalId is required");
        
        verify(saleRepository, never()).saveSale(any());
    }
    
    @Test
    void registerSale_withEmptyTerminalId_shouldThrowValidationException() {
        // Given
        CreateSaleRequest request = new CreateSaleRequest(
                "",
                "CASH-123",
                null,
                "101",
                2,
                new BigDecimal("57.00")
        );
        
        // When / Then
        assertThatThrownBy(() -> saleRegistrationService.registerSale(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("terminalId is required");
        
        verify(saleRepository, never()).saveSale(any());
    }
    
    @Test
    void registerSale_withBlankTerminalId_shouldThrowValidationException() {
        // Given
        CreateSaleRequest request = new CreateSaleRequest(
                "   ",
                "CASH-123",
                null,
                "101",
                2,
                new BigDecimal("57.00")
        );
        
        // When / Then
        assertThatThrownBy(() -> saleRegistrationService.registerSale(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("terminalId is required");
        
        verify(saleRepository, never()).saveSale(any());
    }
    
    @Test
    void registerSale_withNullCashierId_shouldThrowValidationException() {
        // Given
        CreateSaleRequest request = new CreateSaleRequest(
                "TERM-001",
                null,
                null,
                "101",
                2,
                new BigDecimal("57.00")
        );
        
        // When / Then
        assertThatThrownBy(() -> saleRegistrationService.registerSale(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("cashierId is required");
        
        verify(saleRepository, never()).saveSale(any());
    }
    
    @Test
    void registerSale_withBlankCashierId_shouldThrowValidationException() {
        // Given
        CreateSaleRequest request = new CreateSaleRequest(
                "TERM-001",
                "   ",
                null,
                "101",
                2,
                new BigDecimal("57.00")
        );
        
        // When / Then
        assertThatThrownBy(() -> saleRegistrationService.registerSale(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("cashierId is required");
        
        verify(saleRepository, never()).saveSale(any());
    }
    
    @Test
    void registerSale_withNullProductoId_shouldThrowValidationException() {
        // Given
        CreateSaleRequest request = new CreateSaleRequest(
                "TERM-001",
                "CASH-123",
                null,
                null,
                2,
                new BigDecimal("57.00")
        );
        
        // When / Then
        assertThatThrownBy(() -> saleRegistrationService.registerSale(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("productoId is required");
        
        verify(saleRepository, never()).saveSale(any());
    }
    
    @Test
    void registerSale_withNullCantidad_shouldThrowValidationException() {
        // Given
        CreateSaleRequest request = new CreateSaleRequest(
                "TERM-001",
                "CASH-123",
                null,
                "101",
                null,
                new BigDecimal("57.00")
        );
        
        // When / Then
        assertThatThrownBy(() -> saleRegistrationService.registerSale(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("cantidad must be greater than 0");
        
        verify(saleRepository, never()).saveSale(any());
    }
    
    @Test
    void registerSale_withZeroCantidad_shouldThrowValidationException() {
        // Given
        CreateSaleRequest request = new CreateSaleRequest(
                "TERM-001",
                "CASH-123",
                null,
                "101",
                0,
                new BigDecimal("57.00")
        );
        
        // When / Then
        assertThatThrownBy(() -> saleRegistrationService.registerSale(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("cantidad must be greater than 0");
        
        verify(saleRepository, never()).saveSale(any());
    }
    
    @Test
    void registerSale_withNegativeCantidad_shouldThrowValidationException() {
        // Given
        CreateSaleRequest request = new CreateSaleRequest(
                "TERM-001",
                "CASH-123",
                null,
                "101",
                -1,
                new BigDecimal("57.00")
        );
        
        // When / Then
        assertThatThrownBy(() -> saleRegistrationService.registerSale(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("cantidad must be greater than 0");
        
        verify(saleRepository, never()).saveSale(any());
    }
    
    @Test
    void registerSale_withNullTotal_shouldThrowValidationException() {
        // Given
        CreateSaleRequest request = new CreateSaleRequest(
                "TERM-001",
                "CASH-123",
                null,
                "101",
                2,
                null
        );
        
        // When / Then
        assertThatThrownBy(() -> saleRegistrationService.registerSale(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("total must be greater than 0");
        
        verify(saleRepository, never()).saveSale(any());
    }
    
    @Test
    void registerSale_withZeroTotal_shouldThrowValidationException() {
        // Given
        CreateSaleRequest request = new CreateSaleRequest(
                "TERM-001",
                "CASH-123",
                null,
                "101",
                2,
                BigDecimal.ZERO
        );
        
        // When / Then
        assertThatThrownBy(() -> saleRegistrationService.registerSale(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("total must be greater than 0");
        
        verify(saleRepository, never()).saveSale(any());
    }
    
    @Test
    void registerSale_withNegativeTotal_shouldThrowValidationException() {
        // Given
        CreateSaleRequest request = new CreateSaleRequest(
                "TERM-001",
                "CASH-123",
                null,
                "101",
                2,
                new BigDecimal("-57.00")
        );
        
        // When / Then
        assertThatThrownBy(() -> saleRegistrationService.registerSale(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("total must be greater than 0");
        
        verify(saleRepository, never()).saveSale(any());
    }
    
    @Test
    void registerSale_shouldAssignCurrentTimestampInUTC() {
        // Given
        CreateSaleRequest request = new CreateSaleRequest(
                "TERM-001",
                "CASH-123",
                null,
                "101",
                2,
                new BigDecimal("57.00")
        );
        
        LocalDateTime beforeCall = LocalDateTime.now(ZoneOffset.UTC);
        
        when(saleRepository.saveSale(any(SaleDTO.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        SaleDTO result = saleRegistrationService.registerSale(request);
        
        LocalDateTime afterCall = LocalDateTime.now(ZoneOffset.UTC);
        
        // Then
        assertThat(result.getFecha()).isNotNull();
        assertThat(result.getFecha()).isAfterOrEqualTo(beforeCall);
        assertThat(result.getFecha()).isBeforeOrEqualTo(afterCall);
    }
    
    @Test
    void registerSale_shouldCallRepositorySaveSaleWithCorrectData() {
        // Given
        CreateSaleRequest request = new CreateSaleRequest(
                "TERM-001",
                "CASH-123",
                "CUST-456",
                "101",
                2,
                new BigDecimal("57.00")
        );
        
        when(saleRepository.saveSale(any(SaleDTO.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        saleRegistrationService.registerSale(request);
        
        // Then
        ArgumentCaptor<SaleDTO> saleCaptor = ArgumentCaptor.forClass(SaleDTO.class);
        verify(saleRepository).saveSale(saleCaptor.capture());
        
        SaleDTO capturedSale = saleCaptor.getValue();
        assertThat(capturedSale.getVentaId()).isNotNull();
        assertThat(capturedSale.getProductoId()).isEqualTo("101");
        assertThat(capturedSale.getCantidad()).isEqualTo(2);
        assertThat(capturedSale.getTotal()).isEqualByComparingTo(new BigDecimal("57.00"));
        assertThat(capturedSale.getTerminalId()).isEqualTo("TERM-001");
        assertThat(capturedSale.getCashierId()).isEqualTo("CASH-123");
        assertThat(capturedSale.getFecha()).isNotNull();
    }
    
    @Test
    void registerSale_shouldReturnSavedSale() {
        // Given
        CreateSaleRequest request = new CreateSaleRequest(
                "TERM-001",
                "CASH-123",
                null,
                "101",
                2,
                new BigDecimal("57.00")
        );
        
        when(saleRepository.saveSale(any(SaleDTO.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        SaleDTO result = saleRegistrationService.registerSale(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getVentaId()).isNotNull();
        assertThat(result.getProductoId()).isEqualTo("101");
        assertThat(result.getCantidad()).isEqualTo(2);
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("57.00"));
        assertThat(result.getTerminalId()).isEqualTo("TERM-001");
        assertThat(result.getCashierId()).isEqualTo("CASH-123");
        assertThat(result.getFecha()).isNotNull();
    }
}
