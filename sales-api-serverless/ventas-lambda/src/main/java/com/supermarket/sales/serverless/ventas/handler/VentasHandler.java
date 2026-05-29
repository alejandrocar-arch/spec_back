package com.supermarket.sales.serverless.ventas.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.supermarket.sales.serverless.ventas.dto.CreateSaleRequest;
import com.supermarket.sales.serverless.ventas.dto.ErrorResponse;
import com.supermarket.sales.serverless.ventas.dto.SaleDTO;
import com.supermarket.sales.serverless.ventas.exception.ValidationException;
import com.supermarket.sales.serverless.ventas.repository.SaleRepository;
import com.supermarket.sales.serverless.ventas.service.SaleRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.util.HashMap;
import java.util.Map;

/**
 * Lambda handler for sale registration requests.
 * Handles POST /ventas requests.
 */
public class VentasHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(VentasHandler.class);
    
    private final SaleRegistrationService registrationService;
    private final ObjectMapper objectMapper;
    
    // Constructor for Lambda runtime (no-arg constructor)
    public VentasHandler() {
        String tableName = System.getenv("VENTAS_TABLE_NAME");
        
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalStateException("VENTAS_TABLE_NAME environment variable is not set");
        }
        
        logger.info("Initializing VentasHandler with table: {}", tableName);
        
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder().build();
        SaleRepository saleRepository = new SaleRepository(dynamoDbClient, tableName);
        this.registrationService = new SaleRegistrationService(saleRepository);
        
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    // Constructor for testing
    public VentasHandler(SaleRegistrationService registrationService, ObjectMapper objectMapper) {
        this.registrationService = registrationService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        String requestId = context.getAwsRequestId();
        logger.info("[RequestId: {}] Received POST /ventas request", requestId);
        
        try {
            // Validate request body presence
            String body = input.getBody();
            
            if (body == null || body.trim().isEmpty()) {
                logger.warn("[RequestId: {}] Missing request body", requestId);
                return buildErrorResponse(400, "Request body is required");
            }
            
            // Parse request body
            CreateSaleRequest request;
            try {
                request = objectMapper.readValue(body, CreateSaleRequest.class);
            } catch (JsonProcessingException e) {
                logger.warn("[RequestId: {}] Invalid JSON in request body: {}", requestId, e.getMessage());
                return buildErrorResponse(400, "Invalid request body");
            }
            
            logger.info("[RequestId: {}] Registering sale for terminal: {}, cashier: {}", 
                       requestId, request.getTerminalId(), request.getCashierId());
            
            // Register sale
            SaleDTO sale = registrationService.registerSale(request);
            
            logger.info("[RequestId: {}] Sale registered successfully with ventaId: {}", 
                       requestId, sale.getVentaId());
            
            // Build success response
            return buildSuccessResponse(201, sale);
            
        } catch (ValidationException e) {
            logger.warn("[RequestId: {}] Validation error: {}", requestId, e.getMessage());
            return buildErrorResponse(400, e.getMessage());
            
        } catch (DynamoDbException e) {
            logger.error("[RequestId: {}] DynamoDB error", requestId, e);
            return buildErrorResponse(500, "Failed to save sale");
            
        } catch (Exception e) {
            logger.error("[RequestId: {}] Unexpected error", requestId, e);
            return buildErrorResponse(500, "Internal server error");
        }
    }
    
    /**
     * Build success response with sale data.
     */
    private APIGatewayProxyResponseEvent buildSuccessResponse(int statusCode, SaleDTO sale) {
        try {
            String body = objectMapper.writeValueAsString(sale);
            
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(getResponseHeaders())
                    .withBody(body);
                    
        } catch (Exception e) {
            logger.error("Error serializing sale to JSON", e);
            return buildErrorResponse(500, "Internal server error");
        }
    }
    
    /**
     * Build error response with error details.
     */
    private APIGatewayProxyResponseEvent buildErrorResponse(int statusCode, String message) {
        try {
            ErrorResponse errorResponse = new ErrorResponse(statusCode, message);
            String body = objectMapper.writeValueAsString(errorResponse);
            
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(getResponseHeaders())
                    .withBody(body);
                    
        } catch (Exception e) {
            logger.error("Error serializing error response to JSON", e);
            
            // Fallback error response
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withHeaders(getResponseHeaders())
                    .withBody("{\"statusCode\":500,\"message\":\"Internal server error\"}");
        }
    }
    
    /**
     * Get standard response headers.
     */
    private Map<String, String> getResponseHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "POST,OPTIONS");
        return headers;
    }
}
