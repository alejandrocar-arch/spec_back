package com.supermarket.sales.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Individual field validation error")
public class ValidationError {
    @Schema(description = "Field name that failed validation", example = "terminalId")
    private String field;
    
    @Schema(description = "Validation error message", example = "Terminal ID is required")
    private String message;
    
    @Schema(description = "Value that was rejected", example = "")
    private Object rejectedValue;

    public ValidationError() {
    }

    public ValidationError(String field, String message, Object rejectedValue) {
        this.field = field;
        this.message = message;
        this.rejectedValue = rejectedValue;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    public void setRejectedValue(Object rejectedValue) {
        this.rejectedValue = rejectedValue;
    }
}
