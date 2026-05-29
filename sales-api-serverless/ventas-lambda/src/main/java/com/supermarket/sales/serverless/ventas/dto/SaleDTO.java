package com.supermarket.sales.serverless.ventas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Simplified Sale DTO for V1.
 * Contains only essential fields for sale registration.
 */
public class SaleDTO {
    
    @JsonProperty("ventaId")
    private String ventaId;
    
    @JsonProperty("productoId")
    private String productoId;
    
    @JsonProperty("cantidad")
    private Integer cantidad;
    
    @JsonProperty("total")
    private BigDecimal total;
    
    @JsonProperty("fecha")
    private LocalDateTime fecha;
    
    @JsonProperty("terminalId")
    private String terminalId;
    
    @JsonProperty("cashierId")
    private String cashierId;

    public SaleDTO() {
    }

    public SaleDTO(String ventaId, String productoId, Integer cantidad, BigDecimal total, 
                   LocalDateTime fecha, String terminalId, String cashierId) {
        this.ventaId = ventaId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.total = total;
        this.fecha = fecha;
        this.terminalId = terminalId;
        this.cashierId = cashierId;
    }

    public String getVentaId() {
        return ventaId;
    }

    public void setVentaId(String ventaId) {
        this.ventaId = ventaId;
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

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
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

    @Override
    public String toString() {
        return "SaleDTO{" +
                "ventaId='" + ventaId + '\'' +
                ", productoId='" + productoId + '\'' +
                ", cantidad=" + cantidad +
                ", total=" + total +
                ", fecha=" + fecha +
                ", terminalId='" + terminalId + '\'' +
                ", cashierId='" + cashierId + '\'' +
                '}';
    }
}
