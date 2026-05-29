package com.supermarket.sales.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CreditNoteDTO {
    private String creditNoteNumber;
    private String originalCreditReferenceNumber;
    private String originalTransactionId;
    private LocalDateTime issueDate;
    private String customerName;
    private List<ReturnedItemDTO> returnedItems;
    private BigDecimal totalCreditAmount;

    public CreditNoteDTO() {
    }

    public String getCreditNoteNumber() {
        return creditNoteNumber;
    }

    public void setCreditNoteNumber(String creditNoteNumber) {
        this.creditNoteNumber = creditNoteNumber;
    }

    public String getOriginalCreditReferenceNumber() {
        return originalCreditReferenceNumber;
    }

    public void setOriginalCreditReferenceNumber(String originalCreditReferenceNumber) {
        this.originalCreditReferenceNumber = originalCreditReferenceNumber;
    }

    public String getOriginalTransactionId() {
        return originalTransactionId;
    }

    public void setOriginalTransactionId(String originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<ReturnedItemDTO> getReturnedItems() {
        return returnedItems;
    }

    public void setReturnedItems(List<ReturnedItemDTO> returnedItems) {
        this.returnedItems = returnedItems;
    }

    public BigDecimal getTotalCreditAmount() {
        return totalCreditAmount;
    }

    public void setTotalCreditAmount(BigDecimal totalCreditAmount) {
        this.totalCreditAmount = totalCreditAmount;
    }
}
