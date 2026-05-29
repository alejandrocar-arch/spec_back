package com.supermarket.sales.repository;

import com.supermarket.sales.entity.Sale;
import com.supermarket.sales.entity.SaleItem;
import com.supermarket.sales.enums.SaleStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for SaleItemRepository
 */
@DataJpaTest
class SaleItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Test
    void testFindBySaleId() {
        // Given
        Sale sale1 = createSale("TERM-001", "CASH-001");
        Sale sale2 = createSale("TERM-002", "CASH-002");

        entityManager.persist(sale1);
        entityManager.persist(sale2);

        SaleItem item1 = createSaleItem(sale1, 100L, "Product A", "BAR-001");
        SaleItem item2 = createSaleItem(sale1, 101L, "Product B", "BAR-002");
        SaleItem item3 = createSaleItem(sale2, 102L, "Product C", "BAR-003");

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(item3);
        entityManager.flush();

        // When
        List<SaleItem> result = saleItemRepository.findBySaleId(sale1.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(item -> item.getSale().getId().equals(sale1.getId()));
    }

    @Test
    void testFindBySaleId_NoItems() {
        // Given
        Sale sale = createSale("TERM-001", "CASH-001");
        entityManager.persist(sale);
        entityManager.flush();

        // When
        List<SaleItem> result = saleItemRepository.findBySaleId(sale.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testFindByProductId() {
        // Given
        Sale sale1 = createSale("TERM-001", "CASH-001");
        Sale sale2 = createSale("TERM-002", "CASH-002");

        entityManager.persist(sale1);
        entityManager.persist(sale2);

        SaleItem item1 = createSaleItem(sale1, 100L, "Product A", "BAR-001");
        SaleItem item2 = createSaleItem(sale2, 100L, "Product A", "BAR-001");
        SaleItem item3 = createSaleItem(sale1, 101L, "Product B", "BAR-002");

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(item3);
        entityManager.flush();

        // When
        List<SaleItem> result = saleItemRepository.findByProductId(100L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(item -> item.getProductId().equals(100L));
    }

    @Test
    void testFindByProductId_NoItems() {
        // When
        List<SaleItem> result = saleItemRepository.findByProductId(999L);

        // Then
        assertThat(result).isEmpty();
    }

    private Sale createSale(String terminalId, String cashierId) {
        Sale sale = new Sale();
        sale.setTerminalId(terminalId);
        sale.setCashierId(cashierId);
        sale.setStatus(SaleStatus.ACTIVE);
        sale.setSubtotal(BigDecimal.ZERO);
        sale.setTax(BigDecimal.ZERO);
        sale.setDiscount(BigDecimal.ZERO);
        sale.setTotal(BigDecimal.ZERO);
        sale.setCreatedAt(LocalDateTime.now());
        return sale;
    }

    private SaleItem createSaleItem(Sale sale, Long productId, String productName, String barcode) {
        SaleItem item = new SaleItem();
        item.setSale(sale);
        item.setProductId(productId);
        item.setProductName(productName);
        item.setBarcode(barcode);
        item.setUnitPrice(new BigDecimal("10.00"));
        item.setQuantity(1);
        item.setLineTotal(new BigDecimal("10.00"));
        item.setReturnedQuantity(0);
        return item;
    }
}
