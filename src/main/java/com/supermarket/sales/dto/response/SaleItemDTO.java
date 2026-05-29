package com.supermarket.sales.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Sale item details with product snapshot and pricing")
public class SaleItemDTO {
    
    @Schema(description = "Sale item identifier", example = "501")
    private Long id;
    
    @Schema(description = "Product identifier", example = "101")
    private Long productId;
    
    @Schema(description = "Product name snapshot", example = "Coca Cola 2L")
    private String productName;
    
    @Schema(description = "Product barcode", example = "7501234567890")
    private String barcode;
    
    @Schema(description = "Unit price at time of sale", example = "28.50")
    private BigDecimal unitPrice;
    
    @Schema(description = "Quantity purchased", example = "3")
    private Integer quantity;
    
    @Schema(description = "Line total (unitPrice × quantity)", example = "85.50")
    private BigDecimal lineTotal;
    
    @Schema(description = "Quantity returned (for partial returns)", example = "0")
    private Integer returnedQuantity;

    public SaleItemDTO() {
    }

    public SaleItemDTO(Long id, Long productId, String productName, String barcode, 
                       BigDecimal unitPrice, Integer quantity, BigDecimal lineTotal, Integer returnedQuantity) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.barcode = barcode;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
        this.returnedQuantity = returnedQuantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public Integer getReturnedQuantity() {
        return returnedQuantity;
    }

    public void setReturnedQuantity(Integer returnedQuantity) {
        this.returnedQuantity = returnedQuantity;
    }
}
