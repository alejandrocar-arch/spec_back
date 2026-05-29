package com.supermarket.sales.serverless.ventas.repository;

import com.supermarket.sales.serverless.ventas.dto.SaleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository for saving sale data to DynamoDB.
 * Uses AWS SDK v2 for DynamoDB operations.
 */
public class SaleRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(SaleRepository.class);
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    
    private final DynamoDbClient dynamoDbClient;
    private final String tableName;
    
    public SaleRepository(DynamoDbClient dynamoDbClient, String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
        logger.info("SaleRepository initialized with table: {}", tableName);
    }
    
    /**
     * Save a sale to DynamoDB.
     *
     * @param sale the sale to save
     * @return the saved SaleDTO
     * @throws RuntimeException if DynamoDB operation fails
     */
    public SaleDTO saveSale(SaleDTO sale) {
        logger.info("Saving sale with ventaId: {}", sale.getVentaId());
        
        try {
            Map<String, AttributeValue> item = mapSaleDTOToItem(sale);
            
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();
            
            dynamoDbClient.putItem(putItemRequest);
            
            logger.info("Sale saved successfully: {}", sale.getVentaId());
            return sale;
            
        } catch (DynamoDbException e) {
            logger.error("DynamoDB error saving sale: {}", sale.getVentaId(), e);
            throw new RuntimeException("Error saving sale to DynamoDB", e);
        }
    }
    
    /**
     * Map SaleDTO to DynamoDB item.
     *
     * @param sale the SaleDTO
     * @return DynamoDB item map
     */
    private Map<String, AttributeValue> mapSaleDTOToItem(SaleDTO sale) {
        Map<String, AttributeValue> item = new HashMap<>();
        
        item.put("ventaId", AttributeValue.builder().s(sale.getVentaId()).build());
        item.put("productoId", AttributeValue.builder().s(sale.getProductoId()).build());
        item.put("cantidad", AttributeValue.builder().n(sale.getCantidad().toString()).build());
        item.put("total", AttributeValue.builder().s(sale.getTotal().toString()).build());
        item.put("fecha", AttributeValue.builder().s(sale.getFecha().format(ISO_FORMATTER)).build());
        item.put("terminalId", AttributeValue.builder().s(sale.getTerminalId()).build());
        item.put("cashierId", AttributeValue.builder().s(sale.getCashierId()).build());
        
        return item;
    }
}
