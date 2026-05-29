package com.supermarket.sales.dto.request;

import com.supermarket.sales.enums.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Request to apply a discount to a sale")
public class ApplyDiscountRequest {
    
    @Schema(description = "Type of discount", example = "PERCENTAGE", required = true, allowableValues = {"PERCENTAGE", "FIXED_AMOUNT"})
    @NotNull(message = "Discount type is required")
    private DiscountType discountType;
    
    @Schema(description = "Discount value (percentage 0-100 or fixed amount)", example = "10.00", required = true, minimum = "0")
    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.00", message = "Discount value must be at least 0")
    private BigDecimal discountValue;

    public ApplyDiscountRequest() {
    }

    public ApplyDiscountRequest(DiscountType discountType, BigDecimal discountValue) {
        this.discountType = discountType;
        this.discountValue = discountValue;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }
}
