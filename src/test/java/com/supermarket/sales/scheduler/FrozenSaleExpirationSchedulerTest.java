package com.supermarket.sales.scheduler;

import com.supermarket.sales.entity.Sale;
import com.supermarket.sales.enums.SaleStatus;
import com.supermarket.sales.repository.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FrozenSaleExpirationScheduler.
 */
@ExtendWith(MockitoExtension.class)
class FrozenSaleExpirationSchedulerTest {
    
    @Mock
    private SaleRepository saleRepository;
    
    @InjectMocks
    private FrozenSaleExpirationScheduler scheduler;
    
    @BeforeEach
    void setUp() {
        // Set expiration hours to 2 (default)
        ReflectionTestUtils.setField(scheduler, "expirationHours", 2);
    }
    
    @Test
    void cancelExpiredFrozenSales_whenNoExpiredSales_shouldNotCancelAnything() {
        // Arrange
        when(saleRepository.findByStatusAndFrozenAtBefore(eq(SaleStatus.FROZEN), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        
        // Act
        scheduler.cancelExpiredFrozenSales();
        
        // Assert
        verify(saleRepository).findByStatusAndFrozenAtBefore(eq(SaleStatus.FROZEN), any(LocalDateTime.class));
        verify(saleRepository, never()).save(any(Sale.class));
    }
    
    @Test
    void cancelExpiredFrozenSales_whenExpiredSalesExist_shouldCancelThem() {
        // Arrange
        Sale expiredSale1 = createFrozenSale(1L, "TERM-01", LocalDateTime.now().minusHours(3));
        Sale expiredSale2 = createFrozenSale(2L, "TERM-02", LocalDateTime.now().minusHours(4));
        
        List<Sale> expiredSales = Arrays.asList(expiredSale1, expiredSale2);
        
        when(saleRepository.findByStatusAndFrozenAtBefore(eq(SaleStatus.FROZEN), any(LocalDateTime.class)))
                .thenReturn(expiredSales);
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        scheduler.cancelExpiredFrozenSales();
        
        // Assert
        verify(saleRepository).findByStatusAndFrozenAtBefore(eq(SaleStatus.FROZEN), any(LocalDateTime.class));
        
        ArgumentCaptor<Sale> saleCaptor = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepository, times(2)).save(saleCaptor.capture());
        
        List<Sale> savedSales = saleCaptor.getAllValues();
        assertThat(savedSales).hasSize(2);
        
        // Verify both sales were cancelled
        for (Sale sale : savedSales) {
            assertThat(sale.getStatus()).isEqualTo(SaleStatus.CANCELLED);
            assertThat(sale.getCancellationReason()).isEqualTo("Automatically cancelled due to freeze timeout");
            assertThat(sale.getCancelledAt()).isNotNull();
        }
    }
    
    @Test
    void cancelExpiredFrozenSales_whenExpirationHoursIs3_shouldUse3HoursThreshold() {
        // Arrange
        ReflectionTestUtils.setField(scheduler, "expirationHours", 3);
        
        when(saleRepository.findByStatusAndFrozenAtBefore(eq(SaleStatus.FROZEN), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        
        // Act
        scheduler.cancelExpiredFrozenSales();
        
        // Assert
        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(saleRepository).findByStatusAndFrozenAtBefore(eq(SaleStatus.FROZEN), timeCaptor.capture());
        
        LocalDateTime capturedTime = timeCaptor.getValue();
        LocalDateTime expectedTime = LocalDateTime.now().minusHours(3);
        
        // Allow 1 second tolerance for test execution time
        assertThat(capturedTime).isBetween(
                expectedTime.minusSeconds(1),
                expectedTime.plusSeconds(1)
        );
    }
    
    @Test
    void cancelExpiredFrozenSales_whenOneSaleFails_shouldContinueWithOthers() {
        // Arrange
        Sale expiredSale1 = createFrozenSale(1L, "TERM-01", LocalDateTime.now().minusHours(3));
        Sale expiredSale2 = createFrozenSale(2L, "TERM-02", LocalDateTime.now().minusHours(4));
        
        List<Sale> expiredSales = Arrays.asList(expiredSale1, expiredSale2);
        
        when(saleRepository.findByStatusAndFrozenAtBefore(eq(SaleStatus.FROZEN), any(LocalDateTime.class)))
                .thenReturn(expiredSales);
        
        // First save succeeds, second fails
        when(saleRepository.save(any(Sale.class)))
                .thenAnswer(invocation -> invocation.getArgument(0))
                .thenThrow(new RuntimeException("Database error"));
        
        // Act
        scheduler.cancelExpiredFrozenSales();
        
        // Assert
        verify(saleRepository, times(2)).save(any(Sale.class));
    }
    
    @Test
    void cancelExpiredFrozenSales_whenRepositoryThrowsException_shouldHandleGracefully() {
        // Arrange
        when(saleRepository.findByStatusAndFrozenAtBefore(eq(SaleStatus.FROZEN), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database connection error"));
        
        // Act - should not throw exception
        scheduler.cancelExpiredFrozenSales();
        
        // Assert
        verify(saleRepository).findByStatusAndFrozenAtBefore(eq(SaleStatus.FROZEN), any(LocalDateTime.class));
        verify(saleRepository, never()).save(any(Sale.class));
    }
    
    // Helper methods
    
    private Sale createFrozenSale(Long id, String terminalId, LocalDateTime frozenAt) {
        Sale sale = new Sale();
        ReflectionTestUtils.setField(sale, "id", id);
        sale.setTerminalId(terminalId);
        sale.setCashierId("CASHIER-01");
        sale.setStatus(SaleStatus.FROZEN);
        sale.setFrozenAt(frozenAt);
        return sale;
    }
}
