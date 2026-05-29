package com.supermarket.sales.client;

import com.supermarket.sales.dto.external.CustomerDTO;
import com.supermarket.sales.enums.CreditStatus;

import java.util.List;

/**
 * Client interface for Customer API integration.
 */
public interface CustomerApiClient {
    
    /**
     * Search customers by partial name match
     */
    List<CustomerDTO> searchCustomersByName(String name);
    
    /**
     * Find customer by exact document number
     */
    CustomerDTO searchCustomerByDocument(String documentNumber);
    
    /**
     * Retrieve customer details by ID
     */
    CustomerDTO getCustomerById(Long customerId);
    
    /**
     * Check customer credit approval status
     */
    CreditStatus validateCreditStatus(Long customerId);
}
