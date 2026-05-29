package com.supermarket.sales.serverless.ventas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Request DTO for creating a sale.
 * Extended from Spring Boot project with additional fields for V1.
 */
public class CreateSaleRequest {
    
    @JsonProperty("terminalId")
    private String terminalId;
    
    @JsonProperty("cashierId")
    private String cashierId;
    
    @JsonProperty("customerId")
    private String customerId;
    
    // V1 specific fields
    @JsonProperty("productoId")
    private String productoId;
    
    @JsonProperty("cantidad")
    private Integer cantidad;
    
    @JsonProperty("total")
    private BigDecimal total;

    public CreateSaleRequest() {
    }

    public CreateSaleRequest(String terminalId, String cashierId, String customerId, 
                            String productoId, Integer cantidad, BigDecimal total) {
        this.terminalId = terminalId;
        this.cashierId = cashierId;
        this.customerId = customerId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.total = total;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getCashierId() {
        return cashierId;
    }

    public void setCashierId(String cashierId) {
        this.cashierId = cashierId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "CreateSaleRequest{" +
                "terminalId='" + terminalId + '\'' +
                ", cashierId='" + cashierId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", productoId='" + productoId + '\'' +
                ", cantidad=" + cantidad +
                ", total=" + total +
                '}';
    }
}
