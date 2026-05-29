package com.supermarket.sales.scheduler;

import com.supermarket.sales.entity.Sale;
import com.supermarket.sales.enums.SaleStatus;
import com.supermarket.sales.repository.SaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled task to automatically cancel frozen sales that exceed the expiration timeout.
 * Implements Requirement 18: Automatic Cancellation of Expired Frozen Sales.
 */
@Component
public class FrozenSaleExpirationScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(FrozenSaleExpirationScheduler.class);
    private static final String AUTO_CANCEL_REASON = "Automatically cancelled due to freeze timeout";
    
    private final SaleRepository saleRepository;
    
    @Value("${sales.frozen-sale-expiration-hours:2}")
    private int expirationHours;
    
    public FrozenSaleExpirationScheduler(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }
    
    /**
     * Checks for expired frozen sales and cancels them automatically.
     * Runs every 5 minutes (300,000 milliseconds).
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void cancelExpiredFrozenSales() {
        logger.debug("Running frozen sale expiration check");
        
        try {
            // Calculate expiration threshold
            LocalDateTime expirationThreshold = LocalDateTime.now().minusHours(expirationHours);
            
            // Find all frozen sales that have exceeded the expiration timeout
            List<Sale> expiredSales = saleRepository.findByStatusAndFrozenAtBefore(
                    SaleStatus.FROZEN, expirationThreshold);
            
            if (expiredSales.isEmpty()) {
                logger.debug("No expired frozen sales found");
                return;
            }
            
            logger.info("Found {} expired frozen sales to cancel", expiredSales.size());
            
            // Cancel each expired sale
            for (Sale sale : expiredSales) {
                try {
                    sale.cancel(AUTO_CANCEL_REASON);
                    saleRepository.save(sale);
                    logger.info("Auto-cancelled expired frozen sale: {} (terminal: {}, frozen at: {})",
                            sale.getId(), sale.getTerminalId(), sale.getFrozenAt());
                } catch (Exception e) {
                    logger.error("Failed to cancel expired frozen sale {}: {}", 
                            sale.getId(), e.getMessage(), e);
                }
            }
            
            logger.info("Completed frozen sale expiration check. Cancelled {} sales", expiredSales.size());
            
        } catch (Exception e) {
            logger.error("Error during frozen sale expiration check: {}", e.getMessage(), e);
        }
    }
}
