package com.supermarket.sales.mapper;

import com.supermarket.sales.dto.response.SaleDTO;
import com.supermarket.sales.entity.Sale;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper for Sale entity to DTO conversions.
 */
@Component
public class SaleMapper {
    
    private final SaleItemMapper saleItemMapper;
    
    public SaleMapper(SaleItemMapper saleItemMapper) {
        this.saleItemMapper = saleItemMapper;
    }
    
    public SaleDTO toDTO(Sale sale) {
        if (sale == null) {
            return null;
        }
        
        SaleDTO dto = new SaleDTO();
        dto.setId(sale.getId());
        dto.setTerminalId(sale.getTerminalId());
        dto.setCashierId(sale.getCashierId());
        dto.setCustomerId(sale.getCustomerId());
        dto.setStatus(sale.getStatus());
        dto.setSubtotal(sale.getSubtotal());
        dto.setTax(sale.getTax());
        dto.setDiscount(sale.getDiscount());
        dto.setTotal(sale.getTotal());
        dto.setDiscountType(sale.getDiscountType());
        dto.setDiscountValue(sale.getDiscountValue());
        dto.setPaymentType(sale.getPaymentType());
        dto.setAmountReceived(sale.getAmountReceived());
        dto.setChange(sale.getChange());
        dto.setCreditReferenceNumber(sale.getCreditReferenceNumber());
        dto.setTransactionId(sale.getTransactionId());
        dto.setCreatedAt(sale.getCreatedAt());
        dto.setCompletedAt(sale.getCompletedAt());
        
        // Map items
        dto.setItems(sale.getItems().stream()
                .map(saleItemMapper::toDTO)
                .collect(Collectors.toList()));
        
        return dto;
    }
}
