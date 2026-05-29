package com.supermarket.sales.integration.testdata;

import com.supermarket.sales.dto.external.CustomerDTO;
import com.supermarket.sales.enums.CreditStatus;

/**
 * Test data builder for CustomerDTO objects.
 */
public class CustomerDTOTestBuilder {
    
    public static CustomerDTO createCustomer(Long id, String name, String documentNumber, CreditStatus creditStatus) {
        CustomerDTO customer = new CustomerDTO();
        customer.setId(id);
        customer.setFullName(name);
        customer.setDocumentType("DNI");
        customer.setDocumentNumber(documentNumber);
        customer.setCreditStatus(creditStatus);
        return customer;
    }
    
    public static CustomerDTO createApprovedCustomer() {
        return createCustomer(1L, "John Doe", "12345678", CreditStatus.APPROVED);
    }
    
    public static CustomerDTO createRejectedCustomer() {
        return createCustomer(2L, "Jane Smith", "87654321", CreditStatus.REJECTED);
    }
    
    public static CustomerDTO createPendingCustomer() {
        return createCustomer(3L, "Bob Johnson", "11223344", CreditStatus.PENDING);
    }
}
