package com.supermarket.sales.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to add an item to a sale by product ID")
public class AddItemByProductIdRequest {
    
    @Schema(description = "Product identifier", example = "101", required = true)
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @Schema(description = "Quantity to add", example = "2", required = true, minimum = "1")
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    public AddItemByProductIdRequest() {
    }

    public AddItemByProductIdRequest(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
