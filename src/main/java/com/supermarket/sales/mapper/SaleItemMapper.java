package com.supermarket.sales.mapper;

import com.supermarket.sales.dto.response.SaleItemDTO;
import com.supermarket.sales.entity.SaleItem;
import org.springframework.stereotype.Component;

/**
 * Mapper for SaleItem entity to DTO conversions.
 */
@Component
public class SaleItemMapper {
    
    public SaleItemDTO toDTO(SaleItem item) {
        if (item == null) {
            return null;
        }
        
        SaleItemDTO dto = new SaleItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setBarcode(item.getBarcode());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setQuantity(item.getQuantity());
        dto.setLineTotal(item.getLineTotal());
        dto.setReturnedQuantity(item.getReturnedQuantity());
        
        return dto;
    }
}
