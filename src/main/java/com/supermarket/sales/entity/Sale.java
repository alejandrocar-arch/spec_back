package com.supermarket.sales.entity;

import com.supermarket.sales.enums.DiscountType;
import com.supermarket.sales.enums.PaymentType;
import com.supermarket.sales.enums.SaleStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales", indexes = {
    @Index(name = "idx_sale_terminal_status", columnList = "terminalId,status"),
    @Index(name = "idx_sale_terminal_created", columnList = "terminalId,createdAt"),
    @Index(name = "idx_sale_transaction_id", columnList = "transactionId", unique = true)
})
public class Sale {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String terminalId;
    
    @Column(nullable = false)
    private String cashierId;
    
    private Long customerId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleStatus status;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal discountValue;
    
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal amountReceived;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal change;
    
    private String creditReferenceNumber;
    
    private String transactionId;
    
    @Column(length = 255)
    private String cancellationReason;
    
    private String returnReason;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime frozenAt;
    
    private LocalDateTime completedAt;
    
    private LocalDateTime cancelledAt;
    
    private LocalDateTime returnedAt;
    
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = SaleStatus.ACTIVE;
        }
    }
    
    // Business methods
    public void addItem(SaleItem item) {
        items.add(item);
        item.setSale(this);
    }
    
    public void removeItem(SaleItem item) {
        items.remove(item);
        item.setSale(null);
    }
    
    public void freeze() {
        if (!canFreeze()) {
            throw new IllegalStateException("Only active sales can be frozen");
        }
        this.status = SaleStatus.FROZEN;
        this.frozenAt = LocalDateTime.now();
    }
    
    public void resume() {
        if (!canResume()) {
            throw new IllegalStateException("Only frozen sales can be resumed");
        }
        this.status = SaleStatus.ACTIVE;
    }
    
    public void cancel(String reason) {
        if (!canCancel()) {
            throw new IllegalStateException("Cannot cancel sale in current status");
        }
        this.status = SaleStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledAt = LocalDateTime.now();
    }
    
    public void complete(PaymentType paymentType) {
        if (!canCheckout()) {
            throw new IllegalStateException("Cannot complete sale in current status");
        }
        this.status = SaleStatus.COMPLETED;
        this.paymentType = paymentType;
        this.completedAt = LocalDateTime.now();
    }
    
    public void markAsReturned() {
        if (!canReturn()) {
            throw new IllegalStateException("Only completed sales can be returned");
        }
        this.status = SaleStatus.RETURNED;
        this.returnedAt = LocalDateTime.now();
    }
    
    public void markAsPartiallyReturned() {
        if (!canReturn()) {
            throw new IllegalStateException("Only completed sales can be returned");
        }
        this.status = SaleStatus.PARTIALLY_RETURNED;
        if (this.returnedAt == null) {
            this.returnedAt = LocalDateTime.now();
        }
    }
    
    // Validation methods
    public boolean canAddItems() {
        return status == SaleStatus.ACTIVE;
    }
    
    public boolean canCheckout() {
        return status == SaleStatus.ACTIVE && !items.isEmpty();
    }
    
    public boolean canFreeze() {
        return status == SaleStatus.ACTIVE;
    }
    
    public boolean canResume() {
        return status == SaleStatus.FROZEN;
    }
    
    public boolean canCancel() {
        return status == SaleStatus.ACTIVE || status == SaleStatus.FROZEN;
    }
    
    public boolean canReturn() {
        return status == SaleStatus.COMPLETED || status == SaleStatus.PARTIALLY_RETURNED;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    
    public SaleStatus getStatus() {
        return status;
    }
    
    public void setStatus(SaleStatus status) {
        this.status = status;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public BigDecimal getTax() {
        return tax;
    }
    
    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }
    
    public BigDecimal getDiscount() {
        return discount;
    }
    
    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public DiscountType getDiscountType() {
        return discountType;
    }
    
    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }
    
    public BigDecimal getDiscountValue() {
        return discountValue;
    }
    
    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }
    
    public PaymentType getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }
    
    public BigDecimal getAmountReceived() {
        return amountReceived;
    }
    
    public void setAmountReceived(BigDecimal amountReceived) {
        this.amountReceived = amountReceived;
    }
    
    public BigDecimal getChange() {
        return change;
    }
    
    public void setChange(BigDecimal change) {
        this.change = change;
    }
    
    public String getCreditReferenceNumber() {
        return creditReferenceNumber;
    }
    
    public void setCreditReferenceNumber(String creditReferenceNumber) {
        this.creditReferenceNumber = creditReferenceNumber;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
    
    public String getReturnReason() {
        return returnReason;
    }
    
    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getFrozenAt() {
        return frozenAt;
    }
    
    public void setFrozenAt(LocalDateTime frozenAt) {
        this.frozenAt = frozenAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
    
    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
    
    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }
    
    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }
    
    public List<SaleItem> getItems() {
        return items;
    }
    
    public void setItems(List<SaleItem> items) {
        this.items = items;
    }
}
