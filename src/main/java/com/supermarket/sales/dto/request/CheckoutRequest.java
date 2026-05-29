package com.supermarket.sales.dto.request;

import com.supermarket.sales.enums.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Request to checkout a sale with cash or credit payment")
public class CheckoutRequest {
    
    @Schema(description = "Payment method", example = "CASH", required = true, allowableValues = {"CASH", "CREDIT"})
    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;
    
    @Schema(description = "Amount received from customer (required for CASH payments)", example = "100.00")
    private BigDecimal amountReceived;
    
    @Schema(description = "Customer identifier (required for CREDIT payments)", example = "1001")
    private Long customerId;

    public CheckoutRequest() {
    }

    public CheckoutRequest(PaymentType paymentType, BigDecimal amountReceived, Long customerId) {
        this.paymentType = paymentType;
        this.amountReceived = amountReceived;
        this.customerId = customerId;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public BigDecimal getAmountReceived() {
        return amountReceived;
    }

    public void setAmountReceived(BigDecimal amountReceived) {
        this.amountReceived = amountReceived;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}
