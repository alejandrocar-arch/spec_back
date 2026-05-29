package com.supermarket.sales.service;

import com.supermarket.sales.client.ProductApiClient;
import com.supermarket.sales.dto.external.ProductDTO;
import com.supermarket.sales.exception.InsufficientStockException;
import com.supermarket.sales.exception.ProductNotFoundException;
import com.supermarket.sales.exception.ProductServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductServiceImpl.
 * Tests all methods with mocked ProductApiClient, including success and error scenarios.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    
    @Mock
    private ProductApiClient productApiClient;
    
    @InjectMocks
    private ProductServiceImpl productService;
    
    @Test
    void searchByName_withValidName_returnsProductList() {
        // Given
        String searchName = "Milk";
        List<ProductDTO> expectedProducts = Arrays.asList(
            createProductDTO(1L, "Whole Milk", "123456", new BigDecimal("2.50"), 100),
            createProductDTO(2L, "Skim Milk", "123457", new BigDecimal("2.30"), 50)
        );
        when(productApiClient.searchProductsByName(searchName)).thenReturn(expectedProducts);
        
        // When
        List<ProductDTO> result = productService.searchByName(searchName);
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Whole Milk");
        assertThat(result.get(1).getName()).isEqualTo("Skim Milk");
        verify(productApiClient).searchProductsByName(searchName);
    }
    
    @Test
    void searchByName_withNoMatches_returnsEmptyList() {
        // Given
        String searchName = "NonExistent";
        when(productApiClient.searchProductsByName(searchName)).thenReturn(Collections.emptyList());
        
        // When
        List<ProductDTO> result = productService.searchByName(searchName);
        
        // Then
        assertThat(result).isEmpty();
        verify(productApiClient).searchProductsByName(searchName);
    }
    
    @Test
    void searchByName_whenServiceUnavailable_throwsException() {
        // Given
        String searchName = "Milk";
        when(productApiClient.searchProductsByName(searchName))
            .thenThrow(new ProductServiceUnavailableException("Product service unavailable"));
        
        // When/Then
        assertThatThrownBy(() -> productService.searchByName(searchName))
            .isInstanceOf(ProductServiceUnavailableException.class)
            .hasMessage("Product service unavailable");
        verify(productApiClient).searchProductsByName(searchName);
    }
    
    @Test
    void searchByBarcode_withValidBarcode_returnsProduct() {
        // Given
        String barcode = "123456";
        ProductDTO expectedProduct = createProductDTO(1L, "Whole Milk", barcode, new BigDecimal("2.50"), 100);
        when(productApiClient.searchProductByBarcode(barcode)).thenReturn(expectedProduct);
        
        // When
        ProductDTO result = productService.searchByBarcode(barcode);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBarcode()).isEqualTo(barcode);
        assertThat(result.getName()).isEqualTo("Whole Milk");
        verify(productApiClient).searchProductByBarcode(barcode);
    }
    
    @Test
    void searchByBarcode_withNonExistentBarcode_throwsNotFoundException() {
        // Given
        String barcode = "999999";
        when(productApiClient.searchProductByBarcode(barcode))
            .thenThrow(new ProductNotFoundException("Product not found"));
        
        // When/Then
        assertThatThrownBy(() -> productService.searchByBarcode(barcode))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessage("Product not found");
        verify(productApiClient).searchProductByBarcode(barcode);
    }
    
    @Test
    void searchByBarcode_whenServiceUnavailable_throwsException() {
        // Given
        String barcode = "123456";
        when(productApiClient.searchProductByBarcode(barcode))
            .thenThrow(new ProductServiceUnavailableException("Product service unavailable"));
        
        // When/Then
        assertThatThrownBy(() -> productService.searchByBarcode(barcode))
            .isInstanceOf(ProductServiceUnavailableException.class)
            .hasMessage("Product service unavailable");
        verify(productApiClient).searchProductByBarcode(barcode);
    }
    
    @Test
    void getById_withValidId_returnsProduct() {
        // Given
        Long productId = 1L;
        ProductDTO expectedProduct = createProductDTO(productId, "Whole Milk", "123456", new BigDecimal("2.50"), 100);
        when(productApiClient.getProductById(productId)).thenReturn(expectedProduct);
        
        // When
        ProductDTO result = productService.getById(productId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("Whole Milk");
        verify(productApiClient).getProductById(productId);
    }
    
    @Test
    void getById_withNonExistentId_throwsNotFoundException() {
        // Given
        Long productId = 999L;
        when(productApiClient.getProductById(productId))
            .thenThrow(new ProductNotFoundException("Product not found"));
        
        // When/Then
        assertThatThrownBy(() -> productService.getById(productId))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessage("Product not found");
        verify(productApiClient).getProductById(productId);
    }
    
    @Test
    void getById_whenServiceUnavailable_throwsException() {
        // Given
        Long productId = 1L;
        when(productApiClient.getProductById(productId))
            .thenThrow(new ProductServiceUnavailableException("Product service unavailable"));
        
        // When/Then
        assertThatThrownBy(() -> productService.getById(productId))
            .isInstanceOf(ProductServiceUnavailableException.class)
            .hasMessage("Product service unavailable");
        verify(productApiClient).getProductById(productId);
    }
    
    @Test
    void validateStockAvailability_withSufficientStock_doesNotThrowException() {
        // Given
        Long productId = 1L;
        Integer requestedQuantity = 10;
        ProductDTO product = createProductDTO(productId, "Whole Milk", "123456", new BigDecimal("2.50"), 100);
        when(productApiClient.getProductById(productId)).thenReturn(product);
        
        // When/Then
        productService.validateStockAvailability(productId, requestedQuantity);
        
        // Then
        verify(productApiClient).getProductById(productId);
    }
    
    @Test
    void validateStockAvailability_withExactStock_doesNotThrowException() {
        // Given
        Long productId = 1L;
        Integer requestedQuantity = 50;
        ProductDTO product = createProductDTO(productId, "Whole Milk", "123456", new BigDecimal("2.50"), 50);
        when(productApiClient.getProductById(productId)).thenReturn(product);
        
        // When/Then
        productService.validateStockAvailability(productId, requestedQuantity);
        
        // Then
        verify(productApiClient).getProductById(productId);
    }
    
    @Test
    void validateStockAvailability_withInsufficientStock_throwsException() {
        // Given
        Long productId = 1L;
        Integer requestedQuantity = 150;
        ProductDTO product = createProductDTO(productId, "Whole Milk", "123456", new BigDecimal("2.50"), 100);
        when(productApiClient.getProductById(productId)).thenReturn(product);
        
        // When/Then
        assertThatThrownBy(() -> productService.validateStockAvailability(productId, requestedQuantity))
            .isInstanceOf(InsufficientStockException.class)
            .hasMessage("Insufficient stock available");
        verify(productApiClient).getProductById(productId);
    }
    
    @Test
    void validateStockAvailability_withZeroStock_throwsException() {
        // Given
        Long productId = 1L;
        Integer requestedQuantity = 1;
        ProductDTO product = createProductDTO(productId, "Whole Milk", "123456", new BigDecimal("2.50"), 0);
        when(productApiClient.getProductById(productId)).thenReturn(product);
        
        // When/Then
        assertThatThrownBy(() -> productService.validateStockAvailability(productId, requestedQuantity))
            .isInstanceOf(InsufficientStockException.class)
            .hasMessage("Insufficient stock available");
        verify(productApiClient).getProductById(productId);
    }
    
    @Test
    void decrementStock_withValidParameters_callsClient() {
        // Given
        Long productId = 1L;
        Integer quantity = 5;
        doNothing().when(productApiClient).decrementStock(productId, quantity);
        
        // When
        productService.decrementStock(productId, quantity);
        
        // Then
        verify(productApiClient).decrementStock(productId, quantity);
    }
    
    @Test
    void decrementStock_whenServiceUnavailable_throwsException() {
        // Given
        Long productId = 1L;
        Integer quantity = 5;
        doThrow(new ProductServiceUnavailableException("Product service unavailable"))
            .when(productApiClient).decrementStock(productId, quantity);
        
        // When/Then
        assertThatThrownBy(() -> productService.decrementStock(productId, quantity))
            .isInstanceOf(ProductServiceUnavailableException.class)
            .hasMessage("Product service unavailable");
        verify(productApiClient).decrementStock(productId, quantity);
    }
    
    @Test
    void incrementStock_withValidParameters_callsClient() {
        // Given
        Long productId = 1L;
        Integer quantity = 3;
        doNothing().when(productApiClient).incrementStock(productId, quantity);
        
        // When
        productService.incrementStock(productId, quantity);
        
        // Then
        verify(productApiClient).incrementStock(productId, quantity);
    }
    
    @Test
    void incrementStock_whenServiceUnavailable_throwsException() {
        // Given
        Long productId = 1L;
        Integer quantity = 3;
        doThrow(new ProductServiceUnavailableException("Product service unavailable"))
            .when(productApiClient).incrementStock(productId, quantity);
        
        // When/Then
        assertThatThrownBy(() -> productService.incrementStock(productId, quantity))
            .isInstanceOf(ProductServiceUnavailableException.class)
            .hasMessage("Product service unavailable");
        verify(productApiClient).incrementStock(productId, quantity);
    }
    
    // Helper method to create ProductDTO test data
    private ProductDTO createProductDTO(Long id, String name, String barcode, BigDecimal unitPrice, Integer availableStock) {
        ProductDTO product = new ProductDTO();
        product.setId(id);
        product.setName(name);
        product.setBarcode(barcode);
        product.setUnitPrice(unitPrice);
        product.setAvailableStock(availableStock);
        product.setCategory("Test Category");
        return product;
    }
}
