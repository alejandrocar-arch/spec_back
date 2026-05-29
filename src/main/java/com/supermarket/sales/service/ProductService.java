package com.supermarket.sales.service;

import com.supermarket.sales.dto.external.ProductDTO;

import java.util.List;

/**
 * Service for Product API integration.
 */
public interface ProductService {
    
    /**
     * Search products by name
     */
    List<ProductDTO> searchByName(String name);
    
    /**
     * Search product by barcode
     */
    ProductDTO searchByBarcode(String barcode);
    
    /**
     * Get product by ID
     */
    ProductDTO getById(Long productId);
    
    /**
     * Validate stock availability
     */
    void validateStockAvailability(Long productId, Integer requestedQuantity);
    
    /**
     * Decrement stock
     */
    void decrementStock(Long productId, Integer quantity);
    
    /**
     * Increment stock
     */
    void incrementStock(Long productId, Integer quantity);
}
