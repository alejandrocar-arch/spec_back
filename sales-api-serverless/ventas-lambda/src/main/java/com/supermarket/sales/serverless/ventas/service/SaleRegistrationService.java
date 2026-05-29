package com.supermarket.sales.serverless.ventas.service;

import com.supermarket.sales.serverless.ventas.dto.CreateSaleRequest;
import com.supermarket.sales.serverless.ventas.dto.SaleDTO;
import com.supermarket.sales.serverless.ventas.exception.ValidationException;
import com.supermarket.sales.serverless.ventas.repository.SaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Service for sale registration business logic.
 * Validates requests, generates IDs, and delegates to repository.
 */
public class SaleRegistrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(SaleRegistrationService.class);
    
    private final SaleRepository saleRepository;
    
    public SaleRegistrationService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }
    
    /**
     * Register a new sale.
     *
     * @param request the sale creation request
     * @return SaleDTO with generated ID and timestamp
     * @throws ValidationException if validation fails
     */
    public SaleDTO registerSale(CreateSaleRequest request) {
        logger.info("Registering sale for terminal: {}, cashier: {}", 
                    request.getTerminalId(), request.getCashierId());
        
        // Validate request
        validateRequest(request);
        
        // Generate unique ventaId
        String ventaId = generateVentaId();
        
        // Generate current timestamp in UTC
        LocalDateTime fecha = LocalDateTime.now(ZoneOffset.UTC);
        
        // Create SaleDTO
        SaleDTO sale = new SaleDTO(
                ventaId,
                request.getProductoId(),
                request.getCantidad(),
                request.getTotal(),
                fecha,
                request.getTerminalId(),
                request.getCashierId()
        );
        
        // Save to repository
        SaleDTO savedSale = saleRepository.saveSale(sale);
        
        logger.info("Sale registered successfully with ventaId: {}", ventaId);
        return savedSale;
    }
    
    /**
     * Validate sale creation request.
     *
     * @param request the request to validate
     * @throws ValidationException if validation fails
     */
    private void validateRequest(CreateSaleRequest request) {
        if (request.getTerminalId() == null || request.getTerminalId().trim().isEmpty()) {
            throw new ValidationException("terminalId is required");
        }
        
        if (request.getCashierId() == null || request.getCashierId().trim().isEmpty()) {
            throw new ValidationException("cashierId is required");
        }
        
        if (request.getProductoId() == null || request.getProductoId().trim().isEmpty()) {
            throw new ValidationException("productoId is required");
        }
        
        if (request.getCantidad() == null || request.getCantidad() <= 0) {
            throw new ValidationException("cantidad must be greater than 0");
        }
        
        if (request.getTotal() == null || request.getTotal().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ValidationException("total must be greater than 0");
        }
    }
    
    /**
     * Generate unique ventaId using UUID.
     *
     * @return unique ventaId
     */
    private String generateVentaId() {
        return UUID.randomUUID().toString();
    }
}
