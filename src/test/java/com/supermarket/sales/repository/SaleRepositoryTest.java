package com.supermarket.sales.repository;

import com.supermarket.sales.entity.Sale;
import com.supermarket.sales.enums.SaleStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for SaleRepository
 */
@DataJpaTest
class SaleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SaleRepository saleRepository;

    @Test
    void testFindByTerminalIdOrderByCreatedAtDesc() {
        // Given
        Sale sale1 = createSale("TERM-001", "CASH-001", SaleStatus.ACTIVE);
        Sale sale2 = createSale("TERM-001", "CASH-002", SaleStatus.COMPLETED);
        Sale sale3 = createSale("TERM-002", "CASH-001", SaleStatus.ACTIVE);

        entityManager.persist(sale1);
        entityManager.persist(sale2);
        entityManager.persist(sale3);
        entityManager.flush();

        // When
        List<Sale> result = saleRepository.findByTerminalIdOrderByCreatedAtDesc("TERM-001");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCreatedAt()).isAfterOrEqualTo(result.get(1).getCreatedAt());
    }

    @Test
    void testFindByTerminalIdAndStatus() {
        // Given
        Sale sale1 = createSale("TERM-001", "CASH-001", SaleStatus.ACTIVE);
        Sale sale2 = createSale("TERM-001", "CASH-002", SaleStatus.COMPLETED);
        Sale sale3 = createSale("TERM-001", "CASH-003", SaleStatus.ACTIVE);

        entityManager.persist(sale1);
        entityManager.persist(sale2);
        entityManager.persist(sale3);
        entityManager.flush();

        // When
        List<Sale> result = saleRepository.findByTerminalIdAndStatus("TERM-001", SaleStatus.ACTIVE);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(sale -> sale.getStatus() == SaleStatus.ACTIVE);
    }

    @Test
    void testFindByTerminalIdAndStatusAndCreatedAtBetween() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusHours(2);
        LocalDateTime endDate = now.plusHours(2);

        Sale sale1 = createSale("TERM-001", "CASH-001", SaleStatus.ACTIVE);
        Sale sale2 = createSale("TERM-001", "CASH-002", SaleStatus.ACTIVE);
        Sale sale3 = createSale("TERM-002", "CASH-001", SaleStatus.ACTIVE);

        entityManager.persist(sale1);
        entityManager.persist(sale2);
        entityManager.persist(sale3);
        entityManager.flush();

        // When
        List<Sale> result = saleRepository.findByTerminalIdAndStatusAndCreatedAtBetween(
                "TERM-001", SaleStatus.ACTIVE, startDate, endDate);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(sale -> 
            sale.getTerminalId().equals("TERM-001") && 
            sale.getStatus() == SaleStatus.ACTIVE
        );
    }

    @Test
    void testFindByTransactionId() {
        // Given
        Sale sale = createSale("TERM-001", "CASH-001", SaleStatus.COMPLETED);
        sale.setTransactionId("TXN-12345");

        entityManager.persist(sale);
        entityManager.flush();

        // When
        Optional<Sale> result = saleRepository.findByTransactionId("TXN-12345");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTransactionId()).isEqualTo("TXN-12345");
    }

    @Test
    void testFindByTransactionId_NotFound() {
        // When
        Optional<Sale> result = saleRepository.findByTransactionId("NON-EXISTENT");

        // Then
        assertThat(result).isEmpty();
    }

    private Sale createSale(String terminalId, String cashierId, SaleStatus status) {
        Sale sale = new Sale();
        sale.setTerminalId(terminalId);
        sale.setCashierId(cashierId);
        sale.setStatus(status);
        sale.setSubtotal(BigDecimal.ZERO);
        sale.setTax(BigDecimal.ZERO);
        sale.setDiscount(BigDecimal.ZERO);
        sale.setTotal(BigDecimal.ZERO);
        sale.setCreatedAt(LocalDateTime.now());
        return sale;
    }
}
