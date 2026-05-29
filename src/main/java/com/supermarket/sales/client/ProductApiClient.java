package com.supermarket.sales.client;

import com.supermarket.sales.dto.external.ProductDTO;

import java.util.List;

/**
 * Client interface for Product API integration.
 */
public interface ProductApiClient {
    
    /**
     * Search products by partial name match
     */
    List<ProductDTO> searchProductsByName(String name);
    
    /**
     * Find product by exact barcode
     */
    ProductDTO searchProductByBarcode(String barcode);
    
    /**
     * Retrieve product details by ID
     */
    ProductDTO getProductById(Long productId);
    
    /**
     * Reduce stock after checkout
     */
    void decrementStock(Long productId, Integer quantity);
    
    /**
     * Restore stock after return
     */
    void incrementStock(Long productId, Integer quantity);
}
