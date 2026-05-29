package com.supermarket.sales.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Represents a line item in a sale with product snapshot.
 */
@Entity
@Table(name = "sale_items", indexes = {
    @Index(name = "idx_sale_item_sale_id", columnList = "sale_id"),
    @Index(name = "idx_sale_item_product_id", columnList = "productId")
})
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String barcode;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;

    @Column(nullable = false)
    private Integer returnedQuantity = 0;

    // Business Methods

    /**
     * Update quantity and recalculate line total
     */
    public void updateQuantity(Integer newQuantity) {
        this.quantity = newQuantity;
        calculateLineTotal();
    }

    /**
     * Recalculate lineTotal as unitPrice × quantity
     */
    public void calculateLineTotal() {
        this.lineTotal = this.unitPrice.multiply(new BigDecimal(this.quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Return quantity - returnedQuantity
     */
    public Integer getAvailableForReturn() {
        return this.quantity - this.returnedQuantity;
    }

    /**
     * Track partial returns
     */
    public void incrementReturnedQuantity(Integer amount) {
        this.returnedQuantity += amount;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
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
