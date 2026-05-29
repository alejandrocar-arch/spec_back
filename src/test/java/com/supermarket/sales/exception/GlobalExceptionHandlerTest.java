package com.supermarket.sales.exception;

import com.supermarket.sales.dto.error.ErrorResponse;
import com.supermarket.sales.dto.error.ValidationError;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private MethodArgumentNotValidException validationException;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/v1/sales");
    }

    @Test
    void handleValidationException_ShouldReturn400WithValidationErrors() {
        // Arrange
        FieldError fieldError = new FieldError("createSaleRequest", "terminalId", null, false, null, null, "Terminal ID is required");
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleValidationException(validationException, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals("/api/v1/sales", response.getBody().getPath());
        assertNotNull(response.getBody().getValidationErrors());
        assertEquals(1, response.getBody().getValidationErrors().size());
        
        ValidationError validationError = response.getBody().getValidationErrors().get(0);
        assertEquals("terminalId", validationError.getField());
        assertEquals("Terminal ID is required", validationError.getMessage());
    }

    @Test
    void handleBusinessException_InvalidDiscount_ShouldReturn400() {
        // Arrange
        InvalidDiscountException exception = new InvalidDiscountException("Discount percentage must be between 0 and 100");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(exception, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Discount percentage must be between 0 and 100", response.getBody().getMessage());
        assertEquals("/api/v1/sales", response.getBody().getPath());
    }

    @Test
    void handleBusinessException_SaleNotActive_ShouldReturn409() {
        // Arrange
        SaleNotActiveException exception = new SaleNotActiveException("Sale is not active");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(exception, request);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Sale is not active", response.getBody().getMessage());
        assertEquals("/api/v1/sales", response.getBody().getPath());
    }

    @Test
    void handleBusinessException_CreditNotApproved_ShouldReturn422() {
        // Arrange
        CreditNotApprovedException exception = new CreditNotApprovedException("Customer credit not approved");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(exception, request);

        // Assert
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(422, response.getBody().getStatus());
        assertEquals("Customer credit not approved", response.getBody().getMessage());
        assertEquals("/api/v1/sales", response.getBody().getPath());
    }

    @Test
    void handleResourceNotFoundException_ShouldReturn404() {
        // Arrange
        SaleNotFoundException exception = new SaleNotFoundException("Sale not found");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(exception, request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Sale not found", response.getBody().getMessage());
        assertEquals("/api/v1/sales", response.getBody().getPath());
    }

    @Test
    void handleExternalServiceException_ShouldReturn503() {
        // Arrange
        ProductServiceUnavailableException exception = new ProductServiceUnavailableException("Product service unavailable");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleExternalServiceException(exception, request);

        // Assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().getStatus());
        assertEquals("Product service unavailable", response.getBody().getMessage());
        assertEquals("/api/v1/sales", response.getBody().getPath());
    }

    @Test
    void handleHttpMessageNotReadableException_ShouldReturn400() {
        // Arrange
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Invalid JSON", (Throwable) null);

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadableException(exception, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Invalid request format", response.getBody().getMessage());
        assertEquals("/api/v1/sales", response.getBody().getPath());
    }

    @Test
    void handleHttpRequestMethodNotSupportedException_ShouldReturn405() {
        // Arrange
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("POST");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleHttpRequestMethodNotSupportedException(exception, request);

        // Assert
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(405, response.getBody().getStatus());
        assertEquals("Method not supported", response.getBody().getMessage());
        assertEquals("/api/v1/sales", response.getBody().getPath());
    }

    @Test
    void handleGenericException_ShouldReturn500() {
        // Arrange
        Exception exception = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(exception, request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal server error", response.getBody().getMessage());
        assertEquals("/api/v1/sales", response.getBody().getPath());
    }

    @Test
    void handleBusinessException_InsufficientStock_ShouldReturn409() {
        // Arrange
        InsufficientStockException exception = new InsufficientStockException("Insufficient stock available");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(exception, request);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Insufficient stock available", response.getBody().getMessage());
    }

    @Test
    void handleBusinessException_CustomerRequired_ShouldReturn422() {
        // Arrange
        CustomerRequiredException exception = new CustomerRequiredException("Customer required for credit sales");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(exception, request);

        // Assert
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(422, response.getBody().getStatus());
        assertEquals("Customer required for credit sales", response.getBody().getMessage());
    }
}
