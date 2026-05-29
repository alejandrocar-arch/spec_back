package com.supermarket.sales.serverless.productos.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.supermarket.sales.serverless.productos.dto.ErrorResponse;
import com.supermarket.sales.serverless.productos.dto.ProductDTO;
import com.supermarket.sales.serverless.productos.exception.ProductNotFoundException;
import com.supermarket.sales.serverless.productos.repository.ProductRepository;
import com.supermarket.sales.serverless.productos.service.ProductSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Lambda handler for product search requests.
 * Handles GET /productos?q=xxx requests.
 */
public class ProductosHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductosHandler.class);
    
    private final ProductSearchService searchService;
    private final ObjectMapper objectMapper;
    
    // Constructor for Lambda runtime (no-arg constructor)
    public ProductosHandler() {
        String tableName = System.getenv("PRODUCTOS_TABLE_NAME");
        
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalStateException("PRODUCTOS_TABLE_NAME environment variable is not set");
        }
        
        logger.info("Initializing ProductosHandler with table: {}", tableName);
        
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder().build();
        ProductRepository productRepository = new ProductRepository(dynamoDbClient, tableName);
        this.searchService = new ProductSearchService(productRepository);
        
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    // Constructor for testing
    public ProductosHandler(ProductSearchService searchService, ObjectMapper objectMapper) {
        this.searchService = searchService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        String requestId = context.getAwsRequestId();
        logger.info("[RequestId: {}] Received GET /productos request", requestId);
        
        try {
            // Extract query parameter 'q'
            Map<String, String> queryParams = input.getQueryStringParameters();
            
            if (queryParams == null || !queryParams.containsKey("q")) {
                logger.warn("[RequestId: {}] Missing query parameter 'q'", requestId);
                return buildErrorResponse(400, "Query parameter 'q' is required");
            }
            
            String query = queryParams.get("q");
            
            if (query == null || query.trim().isEmpty()) {
                logger.warn("[RequestId: {}] Empty query parameter 'q'", requestId);
                return buildErrorResponse(400, "Query parameter 'q' cannot be empty");
            }
            
            logger.info("[RequestId: {}] Searching product with query: {}", requestId, query);
            
            // Search product
            ProductDTO product = searchService.searchProduct(query);
            
            logger.info("[RequestId: {}] Product found: {}", requestId, product.getId());
            
            // Build success response
            return buildSuccessResponse(200, product);
            
        } catch (IllegalArgumentException e) {
            logger.warn("[RequestId: {}] Validation error: {}", requestId, e.getMessage());
            return buildErrorResponse(400, e.getMessage());
            
        } catch (ProductNotFoundException e) {
            logger.warn("[RequestId: {}] Product not found: {}", requestId, e.getMessage());
            return buildErrorResponse(404, "Product not found");
            
        } catch (Exception e) {
            logger.error("[RequestId: {}] Unexpected error", requestId, e);
            return buildErrorResponse(500, "Internal server error");
        }
    }
    
    /**
     * Build success response with product data.
     */
    private APIGatewayProxyResponseEvent buildSuccessResponse(int statusCode, ProductDTO product) {
        try {
            String body = objectMapper.writeValueAsString(product);
            
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(getResponseHeaders())
                    .withBody(body);
                    
        } catch (Exception e) {
            logger.error("Error serializing product to JSON", e);
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
        headers.put("Access-Control-Allow-Methods", "GET,OPTIONS");
        return headers;
    }
}
