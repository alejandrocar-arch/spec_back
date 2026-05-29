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
     * Query product by name using NombreIndex GSI.
     *
     * @param nombre the product name to search for
     * @return ProductDTO if found
     * @throws ProductNotFoundException if product not found
     */
    public ProductDTO queryByNombre(String nombre) {
        logger.info("Querying product by nombre: {}", nombre);
        
        try {
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":nombre", AttributeValue.builder().s(nombre).build());
            
            QueryRequest queryRequest = QueryRequest.builder()
                    .tableName(tableName)
                    .indexName("NombreIndex")
                    .keyConditionExpression("nombre = :nombre")
                    .expressionAttributeValues(expressionAttributeValues)
                    .limit(1)
                    .build();
            
            QueryResponse response = dynamoDbClient.query(queryRequest);
            
            if (response.items().isEmpty()) {
                logger.warn("Product not found for nombre: {}", nombre);
                throw new ProductNotFoundException("Product not found for name: " + nombre);
            }
            
            Map<String, AttributeValue> item = response.items().get(0);
            ProductDTO product = mapItemToProductDTO(item);
            
            logger.info("Product found: {}", product);
            return product;
            
        } catch (DynamoDbException e) {
            logger.error("DynamoDB error querying by nombre: {}", nombre, e);
            throw new RuntimeException("Error querying product by name", e);
        }
    }
    
    /**
     * Map DynamoDB item to ProductDTO.
     * Safely reads optional attributes without NullPointerException.
     *
     * @param item DynamoDB item
     * @return ProductDTO
     */
    private ProductDTO mapItemToProductDTO(Map<String, AttributeValue> item) {
        // Required fields
        String id = item.get("id").s();
        String nombre = item.get("nombre").s();
        String codigoBarras = item.get("codigo_barras").s();
        String precioStr = item.get("precio").n();
        BigDecimal precio = new BigDecimal(precioStr);
        
        // Optional fields - safe read pattern
        String categoria = item.containsKey("categoria") ? item.get("categoria").s() : null;
        String descripcion = item.containsKey("descripcion") ? item.get("descripcion").s() : null;
        Integer stock = item.containsKey("stock") ? Integer.parseInt(item.get("stock").n()) : null;
        String unidad = item.containsKey("unidad") ? item.get("unidad").s() : null;
        
        return new ProductDTO(id, nombre, codigoBarras, precio, categoria, descripcion, stock, unidad);
    }
}
