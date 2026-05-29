package com.supermarket.sales.dto.response;

import java.math.BigDecimal;

public class ReturnedItemDTO {
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal refundAmount;

    public ReturnedItemDTO() {
    }

    public ReturnedItemDTO(String productName, Integer quantity, BigDecimal unitPrice, BigDecimal refundAmount) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.refundAmount = refundAmount;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }
}
