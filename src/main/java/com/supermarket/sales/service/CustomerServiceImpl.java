package com.supermarket.sales.service;

import com.supermarket.sales.client.CustomerApiClient;
import com.supermarket.sales.dto.external.CustomerDTO;
import com.supermarket.sales.enums.CreditStatus;
import com.supermarket.sales.exception.CreditNotApprovedException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of CustomerService.
 */
@Service
public class CustomerServiceImpl implements CustomerService {
    
    private final CustomerApiClient customerApiClient;
    
    public CustomerServiceImpl(CustomerApiClient customerApiClient) {
        this.customerApiClient = customerApiClient;
    }
    
    @Override
    public List<CustomerDTO> searchByName(String name) {
        return customerApiClient.searchCustomersByName(name);
    }
    
    @Override
    public CustomerDTO searchByDocument(String documentNumber) {
        return customerApiClient.searchCustomerByDocument(documentNumber);
    }
    
    @Override
    public CustomerDTO getById(Long customerId) {
        return customerApiClient.getCustomerById(customerId);
    }
    
    @Override
    public void validateCreditApproval(Long customerId) {
        CreditStatus status = customerApiClient.validateCreditStatus(customerId);
        if (status != CreditStatus.APPROVED) {
            throw new CreditNotApprovedException("Customer credit not approved");
        }
    }
}
