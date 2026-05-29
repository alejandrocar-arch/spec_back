package com.supermarket.sales.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ReturnReceiptDTO {
    private String originalTransactionId;
    private LocalDateTime returnTimestamp;
    private String returnReason;
    private List<ReturnedItemDTO> returnedItems;
    private BigDecimal totalRefundAmount;
    private String creditNoteNumber;

    public ReturnReceiptDTO() {
    }

    public String getOriginalTransactionId() {
        return originalTransactionId;
    }

    public void setOriginalTransactionId(String originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }

    public LocalDateTime getReturnTimestamp() {
        return returnTimestamp;
    }

    public void setReturnTimestamp(LocalDateTime returnTimestamp) {
        this.returnTimestamp = returnTimestamp;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public List<ReturnedItemDTO> getReturnedItems() {
        return returnedItems;
    }

    public void setReturnedItems(List<ReturnedItemDTO> returnedItems) {
        this.returnedItems = returnedItems;
    }

    public BigDecimal getTotalRefundAmount() {
        return totalRefundAmount;
    }

    public void setTotalRefundAmount(BigDecimal totalRefundAmount) {
        this.totalRefundAmount = totalRefundAmount;
    }

    public String getCreditNoteNumber() {
        return creditNoteNumber;
    }

    public void setCreditNoteNumber(String creditNoteNumber) {
        this.creditNoteNumber = creditNoteNumber;
    }
}
