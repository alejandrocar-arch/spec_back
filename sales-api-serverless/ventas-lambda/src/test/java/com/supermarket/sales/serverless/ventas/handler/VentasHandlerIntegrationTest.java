package com.supermarket.sales.serverless.ventas.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.supermarket.sales.serverless.ventas.dto.ErrorResponse;
import com.supermarket.sales.serverless.ventas.dto.SaleDTO;
import com.supermarket.sales.serverless.ventas.repository.SaleRepository;
import com.supermarket.sales.serverless.ventas.service.SaleRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests for VentasHandler.
 * These tests verify the complete flow from handler to service to repository.
 * 
 * Note: For true integration tests with DynamoDB Local, see the README.md
 * for instructions on setting up DynamoDB Local and running SAM local tests.
 */
@ExtendWith(MockitoExtension.class)
class VentasHandlerIntegrationTest {
    
    @Mock
    private SaleRepository saleRepository;
    
    @Mock
    private Context context;
    
    private VentasHandler handler;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        SaleRegistrationService registrationService = new SaleRegistrationService(saleRepository);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new VentasHandler(registrationService, objectMapper);
        
        when(context.getAwsRequestId()).thenReturn("test-request-id");
    }
    
    @Test
    void handleRequest_withValidSale_shouldReturn201AndSaveInRepository() throws Exception {
        // Given
        String requestBody = """
                {
                    "terminalId": "TERM-001",
                    "cashierId": "CASH-123",
                    "productoId": "101",
                    "cantidad": 2,
                    "total": "57.00"
                }
                """;
        
        when(saleRepository.saveSale(any(SaleDTO.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withBody(requestBody);
        
        // When
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(201);
        assertThat(response.getHeaders()).containsEntry("Content-Type", "application/json");
        
        SaleDTO responseSale = objectMapper.readValue(response.getBody(), SaleDTO.class);
        assertThat(responseSale.getVentaId()).isNotNull();
        assertThat(responseSale.getProductoId()).isEqualTo("101");
        assertThat(responseSale.getCantidad()).isEqualTo(2);
        assertThat(responseSale.getTotal()).isEqualByComparingTo(new BigDecimal("57.00"));
        assertThat(responseSale.getTerminalId()).isEqualTo("TERM-001");
        assertThat(responseSale.getCashierId()).isEqualTo("CASH-123");
        assertThat(responseSale.getFecha()).isNotNull();
        
        // Verify repository was called
        ArgumentCaptor<SaleDTO> saleCaptor = ArgumentCaptor.forClass(SaleDTO.class);
        verify(saleRepository).saveSale(saleCaptor.capture());
        
        SaleDTO savedSale = saleCaptor.getValue();
        assertThat(savedSale.getVentaId()).isNotNull();
        assertThat(savedSale.getProductoId()).isEqualTo("101");
    }
    
    @Test
    void handleRequest_withInvalidJSON_shouldReturn400() throws Exception {
        // Given
        String invalidJson = "{ invalid json }";
        
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withBody(invalidJson);
        
        // When
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getHeaders()).containsEntry("Content-Type", "application/json");
        
        ErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ErrorResponse.class);
        assertThat(errorResponse.getStatusCode()).isEqualTo(400);
        assertThat(errorResponse.getMessage()).isEqualTo("Invalid request body");
        
        verify(saleRepository, never()).saveSale(any());
    }
    
    @Test
    void handleRequest_withMissingTerminalId_shouldReturn400() throws Exception {
        // Given
        String requestBody = """
                {
                    "cashierId": "CASH-123",
                    "productoId": "101",
                    "cantidad": 2,
                    "total": "57.00"
                }
                """;
        
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withBody(requestBody);
        
        // When
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(400);
        
        ErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ErrorResponse.class);
        assertThat(errorResponse.getMessage()).contains("terminalId is required");
        
        verify(saleRepository, never()).saveSale(any());
    }
    
    @Test
    void handleRequest_withMissingCashierId_shouldReturn400() throws Exception {
        // Given
        String requestBody = """
                {
                    "terminalId": "TERM-001",
                    "productoId": "101",
                    "cantidad": 2,
                    "total": "57.00"
                }
                """;
        
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withBody(requestBody);
        
        // When
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(400);
        
        ErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ErrorResponse.class);
        assertThat(errorResponse.getMessage()).contains("cashierId is required");
        
        verify(saleRepository, never()).saveSale(any());
    }
    
    @Test
    void handleRequest_withMissingRequestBody_shouldReturn400() throws Exception {
        // Given
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withBody(null);
        
        // When
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(400);
        
        ErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ErrorResponse.class);
        assertThat(errorResponse.getMessage()).isEqualTo("Request body is required");
        
        verify(saleRepository, never()).saveSale(any());
    }
    
    @Test
    void handleRequest_withEmptyRequestBody_shouldReturn400() throws Exception {
        // Given
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withBody("");
        
        // When
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(400);
        
        ErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ErrorResponse.class);
        assertThat(errorResponse.getMessage()).isEqualTo("Request body is required");
        
        verify(saleRepository, never()).saveSale(any());
    }
    
    @Test
    void handleRequest_withZeroCantidad_shouldReturn400() throws Exception {
        // Given
        String requestBody = """
                {
                    "terminalId": "TERM-001",
                    "cashierId": "CASH-123",
                    "productoId": "101",
                    "cantidad": 0,
                    "total": "57.00"
                }
                """;
        
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withBody(requestBody);
        
        // When
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(400);
        
        ErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ErrorResponse.class);
        assertThat(errorResponse.getMessage()).contains("cantidad must be greater than 0");
        
        verify(saleRepository, never()).saveSale(any());
    }
    
    @Test
    void handleRequest_withDynamoDbException_shouldReturn500() throws Exception {
        // Given
        String requestBody = """
                {
                    "terminalId": "TERM-001",
                    "cashierId": "CASH-123",
                    "productoId": "101",
                    "cantidad": 2,
                    "total": "57.00"
                }
                """;
        
        when(saleRepository.saveSale(any(SaleDTO.class)))
                .thenThrow(DynamoDbException.builder().message("DynamoDB error").build());
        
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withBody(requestBody);
        
        // When
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(500);
        
        ErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ErrorResponse.class);
        assertThat(errorResponse.getStatusCode()).isEqualTo(500);
        assertThat(errorResponse.getMessage()).isEqualTo("Failed to save sale");
    }
    
    @Test
    void handleRequest_withUnexpectedException_shouldReturn500() throws Exception {
        // Given
        String requestBody = """
                {
                    "terminalId": "TERM-001",
                    "cashierId": "CASH-123",
                    "productoId": "101",
                    "cantidad": 2,
                    "total": "57.00"
                }
                """;
        
        when(saleRepository.saveSale(any(SaleDTO.class)))
                .thenThrow(new RuntimeException("Unexpected error"));
        
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withBody(requestBody);
        
        // When
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(500);
        
        ErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ErrorResponse.class);
        assertThat(errorResponse.getMessage()).isEqualTo("Internal server error");
    }
    
    @Test
    void handleRequest_shouldIncludeCorsHeaders() throws Exception {
        // Given
        String requestBody = """
                {
                    "terminalId": "TERM-001",
                    "cashierId": "CASH-123",
                    "productoId": "101",
                    "cantidad": 2,
                    "total": "57.00"
                }
                """;
        
        when(saleRepository.saveSale(any(SaleDTO.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withBody(requestBody);
        
        // When
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        
        // Then
        assertThat(response.getHeaders()).containsEntry("Access-Control-Allow-Origin", "*");
        assertThat(response.getHeaders()).containsEntry("Access-Control-Allow-Methods", "POST,OPTIONS");
    }
    
    @Test
    void handleRequest_withAllFields_shouldSaveCorrectly() throws Exception {
        // Given
        String requestBody = """
                {
                    "terminalId": "TERM-001",
                    "cashierId": "CASH-123",
                    "customerId": "CUST-456",
                    "productoId": "101",
                    "cantidad": 5,
                    "total": "142.50"
                }
                """;
        
        when(saleRepository.saveSale(any(SaleDTO.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
                .withBody(requestBody);
        
        // When
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(201);
        
        SaleDTO responseSale = objectMapper.readValue(response.getBody(), SaleDTO.class);
        assertThat(responseSale.getCantidad()).isEqualTo(5);
        assertThat(responseSale.getTotal()).isEqualByComparingTo(new BigDecimal("142.50"));
    }
}
