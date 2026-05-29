package com.supermarket.sales.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to create a new sale")
public class CreateSaleRequest {
    
    @Schema(description = "POS terminal identifier", example = "TERM-001", required = true)
    @NotBlank(message = "Terminal ID is required")
    private String terminalId;
    
    @Schema(description = "Cashier identifier", example = "CASH-123", required = true)
    @NotBlank(message = "Cashier ID is required")
    private String cashierId;
    
    @Schema(description = "Optional customer identifier for credit sales or loyalty tracking", example = "1001")
    private Long customerId;

    public CreateSaleRequest() {
    }

    public CreateSaleRequest(String terminalId, String cashierId, Long customerId) {
        this.terminalId = terminalId;
        this.cashierId = cashierId;
        this.customerId = customerId;
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
}
