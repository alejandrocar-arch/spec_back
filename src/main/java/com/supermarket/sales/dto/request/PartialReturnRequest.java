package com.supermarket.sales.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "Request to process a partial return of specific items in a sale")
public class PartialReturnRequest {
    
    @Schema(description = "Reason for returning the items", example = "Wrong size", required = true)
    @NotBlank(message = "Return reason is required")
    private String returnReason;
    
    @Schema(description = "List of items to return with quantities", required = true)
    @NotEmpty(message = "Return items list cannot be empty")
    @Valid
    private List<ReturnItemRequest> returnItems;

    public PartialReturnRequest() {
    }

    public PartialReturnRequest(String returnReason, List<ReturnItemRequest> returnItems) {
        this.returnReason = returnReason;
        this.returnItems = returnItems;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public List<ReturnItemRequest> getReturnItems() {
        return returnItems;
    }

    public void setReturnItems(List<ReturnItemRequest> returnItems) {
        this.returnItems = returnItems;
    }
}
