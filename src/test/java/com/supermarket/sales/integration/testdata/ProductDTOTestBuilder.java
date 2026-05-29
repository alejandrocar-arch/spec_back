package com.supermarket.sales.integration.testdata;

import com.supermarket.sales.dto.external.ProductDTO;

import java.math.BigDecimal;

/**
 * Test data builder for ProductDTO objects.
 */
public class ProductDTOTestBuilder {
    
    public static ProductDTO createProduct(Long id, String name, String barcode, BigDecimal price, Integer stock) {
        ProductDTO product = new ProductDTO();
        product.setId(id);
        product.setName(name);
        product.setBarcode(barcode);
        product.setUnitPrice(price);
        product.setAvailableStock(stock);
        product.setCategory("General");
        return product;
    }
    
    public static ProductDTO createProduct1() {
        return createProduct(1L, "Product 1", "BAR001", new BigDecimal("10.00"), 100);
    }
    
    public static ProductDTO createProduct2() {
        return createProduct(2L, "Product 2", "BAR002", new BigDecimal("20.00"), 50);
    }
    
    public static ProductDTO createProduct3() {
        return createProduct(3L, "Product 3", "BAR003", new BigDecimal("15.50"), 75);
    }
    
    public static ProductDTO createProduct4() {
        return createProduct(4L, "Product 4", "BAR004", new BigDecimal("5.00"), 200);
    }
    
    public static ProductDTO createProduct5() {
        return createProduct(5L, "Product 5", "BAR005", new BigDecimal("25.00"), 30);
    }
}
