package com.supermarket.sales.serverless.productos.service;

import com.supermarket.sales.serverless.productos.dto.ProductDTO;
import com.supermarket.sales.serverless.productos.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for product search business logic.
 * Determines search type (barcode vs name) and delegates to repository.
 */
public class ProductSearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductSearchService.class);
    
    private final ProductRepository productRepository;
    
    public ProductSearchService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    /**
     * Search for a product by query string.
     * If query is numeric, searches by barcode.
     * If query is alphabetic, searches by name.
     *
     * @param query the search query
     * @return ProductDTO if found
     * @throws com.supermarket.sales.serverless.productos.exception.ProductNotFoundException if not found
     */
    public ProductDTO searchProduct(String query) {
        logger.info("Searching product with query: {}", query);
        
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query parameter cannot be empty");
        }
        
        String trimmedQuery = query.trim();
        
        if (isNumeric(trimmedQuery)) {
            logger.info("Query is numeric, searching by codigo_barras");
            return productRepository.queryByCodigoBarras(trimmedQuery);
        } else {
            logger.info("Query is alphabetic, searching by nombre");
            return productRepository.queryByNombre(trimmedQuery);
        }
    }
    
    /**
     * Check if a string contains only numeric characters.
     *
     * @param str the string to check
     * @return true if numeric, false otherwise
     */
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        
        return true;
    }
}
