package com.supermarket.sales.dto.external;

import com.supermarket.sales.enums.CreditStatus;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * External data from Customer API.
 */
@Schema(description = "Customer information from Customer API")
public class CustomerDTO {
    @Schema(description = "Customer identifier", example = "1001")
    private Long id;
    
    @Schema(description = "Customer full name", example = "John Doe")
    private String fullName;
    
    @Schema(description = "Document type", example = "DNI")
    private String documentType;
    
    @Schema(description = "Document number", example = "12345678")
    private String documentNumber;
    
    @Schema(description = "Credit approval status", example = "APPROVED")
    private CreditStatus creditStatus;

    public CustomerDTO() {
    }

    public CustomerDTO(Long id, String fullName, String documentType, String documentNumber, CreditStatus creditStatus) {
        this.id = id;
        this.fullName = fullName;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.creditStatus = creditStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public CreditStatus getCreditStatus() {
        return creditStatus;
    }

    public void setCreditStatus(CreditStatus creditStatus) {
        this.creditStatus = creditStatus;
    }
}
