package com.supermarket.sales.repository;

import com.supermarket.sales.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
    
    /**
     * Find all items for a specific sale
     */
    List<SaleItem> findBySaleId(Long saleId);
    
    /**
     * Find all items for a specific product
     */
    List<SaleItem> findByProductId(Long productId);
}
