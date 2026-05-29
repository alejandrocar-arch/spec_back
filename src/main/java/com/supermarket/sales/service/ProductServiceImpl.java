package com.supermarket.sales.service;

import com.supermarket.sales.client.ProductApiClient;
import com.supermarket.sales.dto.external.ProductDTO;
import com.supermarket.sales.exception.InsufficientStockException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of ProductService.
 */
@Service
public class ProductServiceImpl implements ProductService {
    
    private final ProductApiClient productApiClient;
    
    public ProductServiceImpl(ProductApiClient productApiClient) {
        this.productApiClient = productApiClient;
    }
    
    @Override
    public List<ProductDTO> searchByName(String name) {
        return productApiClient.searchProductsByName(name);
    }
    
    @Override
    public ProductDTO searchByBarcode(String barcode) {
        return productApiClient.searchProductByBarcode(barcode);
    }
    
    @Override
    public ProductDTO getById(Long productId) {
        return productApiClient.getProductById(productId);
    }
    
    @Override
    public void validateStockAvailability(Long productId, Integer requestedQuantity) {
        ProductDTO product = getById(productId);
        if (product.getAvailableStock() < requestedQuantity) {
            throw new InsufficientStockException("Insufficient stock available");
        }
    }
    
    @Override
    public void decrementStock(Long productId, Integer quantity) {
        productApiClient.decrementStock(productId, quantity);
    }
    
    @Override
    public void incrementStock(Long productId, Integer quantity) {
        productApiClient.incrementStock(productId, quantity);
    }
}
