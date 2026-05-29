package com.supermarket.sales.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing sale and receipt after successful checkout")
public class CheckoutResponse {
    @Schema(description = "Updated sale information")
    private SaleDTO sale;
    
    @Schema(description = "Generated receipt")
    private ReceiptDTO receipt;

    public CheckoutResponse() {
    }

    public CheckoutResponse(SaleDTO sale, ReceiptDTO receipt) {
        this.sale = sale;
        this.receipt = receipt;
    }

    public SaleDTO getSale() {
        return sale;
    }

    public void setSale(SaleDTO sale) {
        this.sale = sale;
    }

    public ReceiptDTO getReceipt() {
        return receipt;
    }

    public void setReceipt(ReceiptDTO receipt) {
        this.receipt = receipt;
    }
}
