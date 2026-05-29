package com.supermarket.sales.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to cancel a sale")
public class CancelSaleRequest {
    
    @Schema(description = "Reason for cancelling the sale", example = "Customer changed mind", required = true, maxLength = 255)
    @NotBlank(message = "Cancellation reason is required")
    @Size(max = 255, message = "Cancellation reason must not exceed 255 characters")
    private String cancellationReason;

    public CancelSaleRequest() {
    }

    public CancelSaleRequest(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
