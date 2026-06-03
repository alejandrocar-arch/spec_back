package com.supermarket.sales.serverless.productos.repository;

import com.supermarket.sales.serverless.productos.dto.ProductDTO;
import com.supermarket.sales.serverless.productos.exception.ProductNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository for accessing product data from DynamoDB.
 * Uses AWS SDK v2 for DynamoDB operations.
 */
public class ProductRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductRepository.class);
    
    private final DynamoDbClient dynamoDbClient;
    private final String tableName;
    
    public ProductRepository(DynamoDbClient dynamoDbClient, String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
        logger.info("ProductRepository initialized with table: {}", tableName);
    }
    
    /**
     * Query product by barcode using CodigoBarrasIndex GSI.
     *
     * @param codigoBarras the barcode to search for
     * @return ProductDTO if found
     * @throws ProductNotFoundException if product not found
     */
    public ProductDTO queryByCodigoBarras(String codigoBarras) {
        logger.info("Querying product by codigo_barras: {}", codigoBarras);
        
        try {
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":codigoBarras", AttributeValue.builder().s(codigoBarras).build());
            
            QueryRequest queryRequest = QueryRequest.builder()
                    .tableName(tableName)
                    .indexName("CodigoBarrasIndex")
                    .keyConditionExpression("codigo_barras = :codigoBarras")
                    .expressionAttributeValues(expressionAttributeValues)
                    .limit(1)
                    .build();
            
            QueryResponse response = dynamoDbClient.query(queryRequest);
            
            if (response.items().isEmpty()) {
                logger.warn("Product not found for codigo_barras: {}", codigoBarras);
                throw new ProductNotFoundException("Product not found for barcode: " + codigoBarras);
            }
            
            Map<String, AttributeValue> item = response.items().get(0);
            ProductDTO product = mapItemToProductDTO(item);
            
            logger.info("Product found: {}", product);
            return product;
            
        } catch (DynamoDbException e) {
            logger.error("DynamoDB error querying by codigo_barras: {}", codigoBarras, e);
            throw new RuntimeException("Error querying product by barcode", e);
        }
    }
    
    /**
     * Query product by name using partial, case-insensitive matching.
     * Performs a paginated scan and returns the first matching product.
     *
     * @param nombre the product name to search for
     * @return ProductDTO if found
     * @throws ProductNotFoundException if product not found
     */
    public ProductDTO queryByNombre(String nombre) {
        logger.info("Querying product by nombre: {}", nombre);
        
        try {
            String normalizedQuery = nombre == null ? "" : nombre.trim().toLowerCase();
            Map<String, AttributeValue> exclusiveStartKey = null;
            
            do {
                ScanRequest.Builder scanRequestBuilder = ScanRequest.builder()
                        .tableName(tableName)
                        .projectionExpression("id, nombre, codigo_barras, precio, categoria, descripcion, stock, unidad")
                        .limit(50);
                
                if (exclusiveStartKey != null && !exclusiveStartKey.isEmpty()) {
                    scanRequestBuilder.exclusiveStartKey(exclusiveStartKey);
                }
                
                ScanResponse response = dynamoDbClient.scan(scanRequestBuilder.build());
                
                for (Map<String, AttributeValue> item : response.items()) {
                    String itemNombre = getAttributeAsString(item, "nombre");
                    if (itemNombre != null && itemNombre.toLowerCase().contains(normalizedQuery)) {
                        ProductDTO product = mapItemToProductDTO(item);
                        logger.info("Product found by nombre query: {}", product);
                        return product;
                    }
                }
                
                exclusiveStartKey = response.lastEvaluatedKey();
            } while (exclusiveStartKey != null && !exclusiveStartKey.isEmpty());
            
            logger.warn("Product not found for nombre query: {}", nombre);
            throw new ProductNotFoundException("Product not found for name: " + nombre);
            
        } catch (DynamoDbException e) {
            logger.error("DynamoDB error querying by nombre: {}", nombre, e);
            throw new RuntimeException("Error querying product by name", e);
        }
    }
    
    /**
     * Map DynamoDB item to ProductDTO.
     * Safely reads required and optional attributes to avoid NullPointerException.
     *
     * @param item DynamoDB item
     * @return ProductDTO
     */
    private ProductDTO mapItemToProductDTO(Map<String, AttributeValue> item) {
        // Required fields
        String id = getRequiredAttributeAsString(item, "id");
        String nombre = getRequiredAttributeAsString(item, "nombre");
        String codigoBarras = getRequiredAttributeAsString(item, "codigo_barras");
        String precioStr = getRequiredAttributeAsString(item, "precio");
        BigDecimal precio = new BigDecimal(precioStr);
        
        // Optional fields - safe read pattern
        String categoria = getAttributeAsString(item, "categoria");
        String descripcion = getAttributeAsString(item, "descripcion");
        String stockStr = getAttributeAsString(item, "stock");
        Integer stock = stockStr != null && !stockStr.isEmpty() ? Integer.parseInt(stockStr) : null;
        String unidad = getAttributeAsString(item, "unidad");
        
        return new ProductDTO(id, nombre, codigoBarras, precio, categoria, descripcion, stock, unidad);
    }
    
    private String getRequiredAttributeAsString(Map<String, AttributeValue> item, String attributeName) {
        String value = getAttributeAsString(item, attributeName);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException("Required attribute is missing or empty: " + attributeName);
        }
        return value;
    }
    
    private String getAttributeAsString(Map<String, AttributeValue> item, String attributeName) {
        AttributeValue value = item.get(attributeName);
        if (value == null) {
            return null;
        }
        
        if (value.s() != null) {
            return value.s();
        }
        
        if (value.n() != null) {
            return value.n();
        }
        
        return null;
    }
}
