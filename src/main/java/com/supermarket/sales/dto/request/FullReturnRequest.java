package com.supermarket.sales.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to process a full return of all items in a sale")
public class FullReturnRequest {
    
    @Schema(description = "Reason for returning the items", example = "Defective product", required = true)
    @NotBlank(message = "Return reason is required")
    private String returnReason;

    public FullReturnRequest() {
    }

    public FullReturnRequest(String returnReason) {
        this.returnReason = returnReason;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }
}
