package com.supermarket.sales.dto.response;

import com.supermarket.sales.enums.DiscountType;
import com.supermarket.sales.enums.PaymentType;
import com.supermarket.sales.enums.SaleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Sale details with items and totals")
public class SaleDTO {
    
    @Schema(description = "Sale identifier", example = "1")
    private Long id;
    
    @Schema(description = "POS terminal identifier", example = "TERM-001")
    private String terminalId;
    
    @Schema(description = "Cashier identifier", example = "CASH-123")
    private String cashierId;
    
    @Schema(description = "Customer identifier", example = "1001")
    private Long customerId;
    
    @Schema(description = "Current sale status", example = "ACTIVE")
    private SaleStatus status;
    
    @Schema(description = "List of items in the sale")
    private List<SaleItemDTO> items;
    
    @Schema(description = "Subtotal before tax and discount", example = "85.50")
    private BigDecimal subtotal;
    
    @Schema(description = "Tax amount", example = "16.25")
    private BigDecimal tax;
    
    @Schema(description = "Discount amount", example = "5.00")
    private BigDecimal discount;
    
    @Schema(description = "Total amount to pay", example = "96.75")
    private BigDecimal total;
    
    @Schema(description = "Type of discount applied", example = "PERCENTAGE")
    private DiscountType discountType;
    
    @Schema(description = "Discount value (percentage or fixed amount)", example = "10.00")
    private BigDecimal discountValue;
    
    @Schema(description = "Payment method", example = "CASH")
    private PaymentType paymentType;
    
    @Schema(description = "Amount received from customer (cash sales)", example = "100.00")
    private BigDecimal amountReceived;
    
    @Schema(description = "Change returned to customer (cash sales)", example = "3.25")
    private BigDecimal change;
    
    @Schema(description = "Credit reference number (credit sales)", example = "CRED-2024-001")
    private String creditReferenceNumber;
    
    @Schema(description = "Unique transaction identifier", example = "TXN-20240115-001")
    private String transactionId;
    
    @Schema(description = "Sale creation timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Sale completion timestamp", example = "2024-01-15T10:35:00")
    private LocalDateTime completedAt;

    public SaleDTO() {
    }

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

    public List<SaleItemDTO> getItems() {
        return items;
    }

    public void setItems(List<SaleItemDTO> items) {
        this.items = items;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
