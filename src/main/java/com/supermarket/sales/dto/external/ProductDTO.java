package com.supermarket.sales.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * External data from Product API.
 */
@Schema(description = "Product information from Product API")
public class ProductDTO {
    
    @Schema(description = "Product identifier", example = "101")
    private Long id;
    
    @Schema(description = "Product name", example = "Coca Cola 2L")
    private String name;
    
    @Schema(description = "Product barcode", example = "7501234567890")
    private String barcode;
    
    @Schema(description = "Current unit price", example = "28.50")
    private BigDecimal unitPrice;
    
    @Schema(description = "Available stock quantity", example = "150")
    private Integer availableStock;
    
    @Schema(description = "Product category", example = "Beverages")
    private String category;

    public ProductDTO() {
    }

    public ProductDTO(Long id, String name, String barcode, BigDecimal unitPrice, Integer availableStock, String category) {
        this.id = id;
        this.name = name;
        this.barcode = barcode;
        this.unitPrice = unitPrice;
        this.availableStock = availableStock;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
