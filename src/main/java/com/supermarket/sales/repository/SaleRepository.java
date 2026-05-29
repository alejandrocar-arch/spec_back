package com.supermarket.sales.repository;

import com.supermarket.sales.entity.Sale;
import com.supermarket.sales.enums.SaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    
    /**
     * Find all sales for a terminal ordered by creation date descending
     */
    List<Sale> findByTerminalIdOrderByCreatedAtDesc(String terminalId);
    
    /**
     * Find sales by terminal and status
     */
    List<Sale> findByTerminalIdAndStatus(String terminalId, SaleStatus status);
    
    /**
     * Find sales by terminal, status, and date range
     */
    @Query("SELECT s FROM Sale s WHERE s.terminalId = :terminalId " +
           "AND s.status = :status " +
           "AND s.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY s.createdAt DESC")
    List<Sale> findByTerminalIdAndStatusAndCreatedAtBetween(
            @Param("terminalId") String terminalId,
            @Param("status") SaleStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find sale by transaction ID
     */
    Optional<Sale> findByTransactionId(String transactionId);
    
    /**
     * Find frozen sales by terminal ordered by freeze timestamp descending
     */
    @Query("SELECT s FROM Sale s WHERE s.terminalId = :terminalId " +
           "AND s.status = 'FROZEN' " +
           "ORDER BY s.frozenAt DESC")
    List<Sale> findFrozenSalesByTerminal(@Param("terminalId") String terminalId);
    
    /**
     * Find expired frozen sales for automatic cancellation
     */
    @Query("SELECT s FROM Sale s WHERE s.status = 'FROZEN' " +
           "AND s.frozenAt < :expirationTime")
    List<Sale> findExpiredFrozenSales(@Param("expirationTime") LocalDateTime expirationTime);
    
    /**
     * Find sales by status and frozen before a specific time
     */
    List<Sale> findByStatusAndFrozenAtBefore(SaleStatus status, LocalDateTime frozenAt);
}
