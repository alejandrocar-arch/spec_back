package com.supermarket.sales.serverless.productos.service;

import com.supermarket.sales.serverless.productos.dto.ProductDTO;
import com.supermarket.sales.serverless.productos.exception.ProductNotFoundException;
import com.supermarket.sales.serverless.productos.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductSearchServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    private ProductSearchService productSearchService;
    
    @BeforeEach
    void setUp() {
        productSearchService = new ProductSearchService(productRepository);
    }
    
    @Test
    void searchProduct_withNumericQuery_shouldCallQueryByCodigoBarras() {
        // Given
        String numericQuery = "7501234567890";
        ProductDTO expectedProduct = new ProductDTO("101", "Coca Cola 2L", numericQuery, new BigDecimal("28.50"));
        when(productRepository.queryByCodigoBarras(numericQuery)).thenReturn(expectedProduct);
        
        // When
        ProductDTO result = productSearchService.searchProduct(numericQuery);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("101");
        assertThat(result.getCodigoBarras()).isEqualTo(numericQuery);
        verify(productRepository).queryByCodigoBarras(numericQuery);
        verify(productRepository, never()).queryByNombre(anyString());
    }
    
    @Test
    void searchProduct_withAlphabeticQuery_shouldCallQueryByNombre() {
        // Given
        String alphabeticQuery = "Coca";
        ProductDTO expectedProduct = new ProductDTO("101", "Coca Cola 2L", "7501234567890", new BigDecimal("28.50"));
        when(productRepository.queryByNombre(alphabeticQuery)).thenReturn(expectedProduct);
        
        // When
        ProductDTO result = productSearchService.searchProduct(alphabeticQuery);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("101");
        assertThat(result.getNombre()).isEqualTo("Coca Cola 2L");
        verify(productRepository).queryByNombre(alphabeticQuery);
        verify(productRepository, never()).queryByCodigoBarras(anyString());
    }
    
    @Test
    void searchProduct_withMixedAlphanumericQuery_shouldCallQueryByNombre() {
        // Given
        String mixedQuery = "Coca123";
        ProductDTO expectedProduct = new ProductDTO("101", "Coca Cola 2L", "7501234567890", new BigDecimal("28.50"));
        when(productRepository.queryByNombre(mixedQuery)).thenReturn(expectedProduct);
        
        // When
        ProductDTO result = productSearchService.searchProduct(mixedQuery);
        
        // Then
        assertThat(result).isNotNull();
        verify(productRepository).queryByNombre(mixedQuery);
        verify(productRepository, never()).queryByCodigoBarras(anyString());
    }
    
    @Test
    void searchProduct_whenRepositoryThrowsProductNotFoundException_shouldPropagateException() {
        // Given
        String query = "99999";
        when(productRepository.queryByCodigoBarras(query))
                .thenThrow(new ProductNotFoundException("Product not found"));
        
        // When / Then
        assertThatThrownBy(() -> productSearchService.searchProduct(query))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found");
        
        verify(productRepository).queryByCodigoBarras(query);
    }
    
    @Test
    void searchProduct_withNullQuery_shouldThrowIllegalArgumentException() {
        // When / Then
        assertThatThrownBy(() -> productSearchService.searchProduct(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Query parameter cannot be empty");
        
        verify(productRepository, never()).queryByCodigoBarras(anyString());
        verify(productRepository, never()).queryByNombre(anyString());
    }
    
    @Test
    void searchProduct_withEmptyQuery_shouldThrowIllegalArgumentException() {
        // When / Then
        assertThatThrownBy(() -> productSearchService.searchProduct(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Query parameter cannot be empty");
        
        verify(productRepository, never()).queryByCodigoBarras(anyString());
        verify(productRepository, never()).queryByNombre(anyString());
    }
    
    @Test
    void searchProduct_withWhitespaceQuery_shouldThrowIllegalArgumentException() {
        // When / Then
        assertThatThrownBy(() -> productSearchService.searchProduct("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Query parameter cannot be empty");
        
        verify(productRepository, never()).queryByCodigoBarras(anyString());
        verify(productRepository, never()).queryByNombre(anyString());
    }
    
    @Test
    void searchProduct_withQueryContainingLeadingTrailingSpaces_shouldTrimAndSearch() {
        // Given
        String queryWithSpaces = "  7501234567890  ";
        String trimmedQuery = "7501234567890";
        ProductDTO expectedProduct = new ProductDTO("101", "Coca Cola 2L", trimmedQuery, new BigDecimal("28.50"));
        when(productRepository.queryByCodigoBarras(trimmedQuery)).thenReturn(expectedProduct);
        
        // When
        ProductDTO result = productSearchService.searchProduct(queryWithSpaces);
        
        // Then
        assertThat(result).isNotNull();
        verify(productRepository).queryByCodigoBarras(trimmedQuery);
    }
    
    @Test
    void searchProduct_withSingleDigit_shouldCallQueryByCodigoBarras() {
        // Given
        String singleDigit = "5";
        ProductDTO expectedProduct = new ProductDTO("105", "Arroz 1kg", "5", new BigDecimal("18.00"));
        when(productRepository.queryByCodigoBarras(singleDigit)).thenReturn(expectedProduct);
        
        // When
        ProductDTO result = productSearchService.searchProduct(singleDigit);
        
        // Then
        assertThat(result).isNotNull();
        verify(productRepository).queryByCodigoBarras(singleDigit);
        verify(productRepository, never()).queryByNombre(anyString());
    }
    
    @Test
    void searchProduct_withSingleLetter_shouldCallQueryByNombre() {
        // Given
        String singleLetter = "A";
        ProductDTO expectedProduct = new ProductDTO("106", "Aceite Vegetal 1L", "7501234567895", new BigDecimal("35.00"));
        when(productRepository.queryByNombre(singleLetter)).thenReturn(expectedProduct);
        
        // When
        ProductDTO result = productSearchService.searchProduct(singleLetter);
        
        // Then
        assertThat(result).isNotNull();
        verify(productRepository).queryByNombre(singleLetter);
        verify(productRepository, never()).queryByCodigoBarras(anyString());
    }
}
