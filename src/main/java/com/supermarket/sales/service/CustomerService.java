package com.supermarket.sales.service;

import com.supermarket.sales.dto.external.CustomerDTO;

import java.util.List;

/**
 * Service for Customer API integration.
 */
public interface CustomerService {
    
    /**
     * Search customers by name
     */
    List<CustomerDTO> searchByName(String name);
    
    /**
     * Search customer by document
     */
    CustomerDTO searchByDocument(String documentNumber);
    
    /**
     * Get customer by ID
     */
    CustomerDTO getById(Long customerId);
    
    /**
     * Validate credit approval
     */
    void validateCreditApproval(Long customerId);
}
