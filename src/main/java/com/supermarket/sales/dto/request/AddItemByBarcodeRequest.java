package com.supermarket.sales.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to add an item to a sale by barcode")
public class AddItemByBarcodeRequest {
    
    @Schema(description = "Product barcode", example = "7501234567890", required = true)
    @NotBlank(message = "Barcode is required")
    private String barcode;
    
    @Schema(description = "Quantity to add", example = "3", required = true, minimum = "1")
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    public AddItemByBarcodeRequest() {
    }

    public AddItemByBarcodeRequest(String barcode, Integer quantity) {
        this.barcode = barcode;
        this.quantity = quantity;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
