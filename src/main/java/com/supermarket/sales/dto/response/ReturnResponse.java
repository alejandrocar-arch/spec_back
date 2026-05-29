package com.supermarket.sales.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing sale, return receipt, and credit note (if applicable) after return processing")
public class ReturnResponse {
    @Schema(description = "Updated sale information")
    private SaleDTO sale;
    
    @Schema(description = "Generated return receipt")
    private ReturnReceiptDTO returnReceipt;
    
    @Schema(description = "Credit note for credit sales (null for cash sales)")
    private CreditNoteDTO creditNote;

    public ReturnResponse() {
    }

    public ReturnResponse(SaleDTO sale, ReturnReceiptDTO returnReceipt, CreditNoteDTO creditNote) {
        this.sale = sale;
        this.returnReceipt = returnReceipt;
        this.creditNote = creditNote;
    }

    public SaleDTO getSale() {
        return sale;
    }

    public void setSale(SaleDTO sale) {
        this.sale = sale;
    }

    public ReturnReceiptDTO getReturnReceipt() {
        return returnReceipt;
    }

    public void setReturnReceipt(ReturnReceiptDTO returnReceipt) {
        this.returnReceipt = returnReceipt;
    }

    public CreditNoteDTO getCreditNote() {
        return creditNote;
    }

    public void setCreditNote(CreditNoteDTO creditNote) {
        this.creditNote = creditNote;
    }
}
