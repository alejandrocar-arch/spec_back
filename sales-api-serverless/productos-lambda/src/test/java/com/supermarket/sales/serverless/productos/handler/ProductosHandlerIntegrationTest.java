package com.supermarket.sales.serverless.productos.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.supermarket.sales.serverless.productos.dto.ErrorResponse;
import com.supermarket.sales.serverless.productos.dto.ProductDTO;
import com.supermarket.sales.serverless.productos.repository.ProductRepository;
import com.supermarket.sales.serverless.productos.service.ProductSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Integration tests for ProductosHandler.
 * These tests verify the complete flow from handler to service to repository.
 * 
 * Note: For true integration tests with DynamoDB Local, see the README.md
 * for instructions on setting up DynamoDB Local and running SAM local tests.
 */
@ExtendWith(MockitoExtension.class)
class ProductosHandlerIntegrationTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private Context context;
    
    private ProductosHandler handler;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        ProductSearchService searchService = new ProductSearchService(productRepository);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new ProductosHandler(searchService, objectMapper);
        
        when(context.getAwsRequestId()).thenReturn("test-request-id");
    }
    
    @Test
    void handleRequest_withValidBarcodeQuery_shouldReturn200WithProduct() throws Exception {
        // Given
        String barcode = "7501234567890";
        ProductDTO product = new ProductDTO("101", "Coca Cola 2L", barcode, new BigDecimal("28.50"));
        when(productRepository.queryByCodigoBarras(barcode)).thenReturn(product);
        
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", barcode);
        
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withQueryStringParameters(queryParams);
        
        // When
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getHeaders()).containsEntry("Content-Type", "application/json");
        
        ProductDTO responseProduct = objectMapper.readValue(response.getBody(), ProductDTO.class);
        assertThat(responseProduct.getId()).isEqualTo("101");
        assertThat(responseProduct.getNombre()).isEqualTo("Coca Cola 2L");
        assertThat(responseProduct.getCodigoBarras()).isEqualTo(barcode);
        assertThat(responseProduct.getPrecio()).isEqualByComparingTo(new BigDecimal("28.50"));
    }
    
    @Test
    void handleRequest_withValidNameQuery_shouldReturn200WithProduct() throws Exception {
        // Given
        String name = "Coca";
        ProductDTO product = new ProductDTO("101", "Coca Cola 2L", "7501234567890", new BigDecimal("28.50"));
        when(productRepository.queryByNombre(name)).thenReturn(product);
        
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", name);
        
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withQueryStringParameters(queryParams);
        
        // When
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getHeaders()).containsEntry("Content-Type", "application/json");
        
        ProductDTO responseProduct = objectMapper.readValue(response.getBody(), ProductDTO.class);
        assertThat(responseProduct.getId()).isEqualTo("101");
        assertThat(responseProduct.getNombre()).isEqualTo("Coca Cola 2L");
    }
    
    @Test
    void handleRequest_withMissingQueryParameter_shouldReturn400() throws Exception {
        // Given
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withQueryStringParameters(null);
        
        // When
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getHeaders()).containsEntry("Content-Type", "application/json");
        
        ErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ErrorResponse.class);
        assertThat(errorResponse.getStatusCode()).isEqualTo(400);
        assertThat(errorResponse.getMessage()).contains("Query parameter 'q' is required");
        assertThat(errorResponse.getTimestamp()).isNotNull();
    }
    
    @Test
    void handleRequest_withEmptyQueryParameter_shouldReturn400() throws Exception {
        // Given
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", "");
        
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withQueryStringParameters(queryParams);
        
        // When
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(400);
        
        ErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ErrorResponse.class);
        assertThat(errorResponse.getMessage()).contains("cannot be empty");
    }
    
    @Test
    void handleRequest_withNonExistentProduct_shouldReturn404() throws Exception {
        // Given
        String query = "99999";
        when(productRepository.queryByCodigoBarras(query))
                .thenThrow(new com.supermarket.sales.serverless.productos.exception.ProductNotFoundException("Product not found"));
        
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", query);
        
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withQueryStringParameters(queryParams);
        
        // When
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(response.getHeaders()).containsEntry("Content-Type", "application/json");
        
        ErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ErrorResponse.class);
        assertThat(errorResponse.getStatusCode()).isEqualTo(404);
        assertThat(errorResponse.getMessage()).isEqualTo("Product not found");
    }
    
    @Test
    void handleRequest_withRepositoryException_shouldReturn500() throws Exception {
        // Given
        String query = "7501234567890";
        when(productRepository.queryByCodigoBarras(query))
                .thenThrow(new RuntimeException("DynamoDB connection error"));
        
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", query);
        
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withQueryStringParameters(queryParams);
        
        // When
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(500);
        assertThat(response.getHeaders()).containsEntry("Content-Type", "application/json");
        
        ErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ErrorResponse.class);
        assertThat(errorResponse.getStatusCode()).isEqualTo(500);
        assertThat(errorResponse.getMessage()).isEqualTo("Internal server error");
    }
    
    @Test
    void handleRequest_shouldIncludeCorsHeaders() {
        // Given
        String barcode = "7501234567890";
        ProductDTO product = new ProductDTO("101", "Coca Cola 2L", barcode, new BigDecimal("28.50"));
        when(productRepository.queryByCodigoBarras(barcode)).thenReturn(product);
        
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", barcode);
        
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withQueryStringParameters(queryParams);
        
        // When
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        
        // Then
        assertThat(response.getHeaders()).containsEntry("Access-Control-Allow-Origin", "*");
        assertThat(response.getHeaders()).containsEntry("Access-Control-Allow-Methods", "GET,OPTIONS");
    }
    
    @Test
    void handleRequest_withWhitespaceInQuery_shouldTrimAndSearch() throws Exception {
        // Given
        String queryWithSpaces = "  7501234567890  ";
        String trimmedQuery = "7501234567890";
        ProductDTO product = new ProductDTO("101", "Coca Cola 2L", trimmedQuery, new BigDecimal("28.50"));
        when(productRepository.queryByCodigoBarras(trimmedQuery)).thenReturn(product);
        
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", queryWithSpaces);
        
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withQueryStringParameters(queryParams);
        
        // When
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(200);
        
        ProductDTO responseProduct = objectMapper.readValue(response.getBody(), ProductDTO.class);
        assertThat(responseProduct.getCodigoBarras()).isEqualTo(trimmedQuery);
    }
}
