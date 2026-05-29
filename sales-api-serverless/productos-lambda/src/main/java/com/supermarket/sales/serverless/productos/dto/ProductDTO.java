package com.supermarket.sales.serverless.productos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Product data transfer object.
 * Reused and adapted from the Spring Boot project.
 * 
 * Supports optional attributes: categoria, descripcion, stock, unidad
 */
public class ProductDTO {
    
    // Required fields
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("nombre")
    private String nombre;
    
    @JsonProperty("codigoBarras")
    private String codigoBarras;
    
    @JsonProperty("precio")
    private BigDecimal precio;
    
    // Optional fields
    @JsonProperty("categoria")
    private String categoria;
    
    @JsonProperty("descripcion")
    private String descripcion;
    
    @JsonProperty("stock")
    private Integer stock;
    
    @JsonProperty("unidad")
    private String unidad;

    public ProductDTO() {
    }

    public ProductDTO(String id, String nombre, String codigoBarras, BigDecimal precio) {
        this.id = id;
        this.nombre = nombre;
        this.codigoBarras = codigoBarras;
        this.precio = precio;
    }
    
    public ProductDTO(String id, String nombre, String codigoBarras, BigDecimal precio,
                      String categoria, String descripcion, Integer stock, String unidad) {
        this.id = id;
        this.nombre = nombre;
        this.codigoBarras = codigoBarras;
        this.precio = precio;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.stock = stock;
        this.unidad = unidad;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    @Override
    public String toString() {
        return "ProductDTO{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", codigoBarras='" + codigoBarras + '\'' +
                ", precio=" + precio +
                ", categoria='" + categoria + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", stock=" + stock +
                ", unidad='" + unidad + '\'' +
                '}';
    }
}
