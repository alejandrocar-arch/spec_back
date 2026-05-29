package com.supermarket.sales.service;

import com.supermarket.sales.client.CustomerApiClient;
import com.supermarket.sales.dto.external.CustomerDTO;
import com.supermarket.sales.enums.CreditStatus;
import com.supermarket.sales.exception.CreditNotApprovedException;
import com.supermarket.sales.exception.CustomerNotFoundException;
import com.supermarket.sales.exception.CustomerServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerServiceImpl.
 * Tests all methods with mocked CustomerApiClient, including success and error scenarios.
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {
    
    @Mock
    private CustomerApiClient customerApiClient;
    
    @InjectMocks
    private CustomerServiceImpl customerService;
    
    @Test
    void searchByName_withValidName_returnsCustomerList() {
        // Given
        String searchName = "John";
        List<CustomerDTO> expectedCustomers = Arrays.asList(
            createCustomerDTO(1L, "John Doe", "DNI", "12345678", CreditStatus.APPROVED),
            createCustomerDTO(2L, "John Smith", "DNI", "87654321", CreditStatus.REJECTED)
        );
        when(customerApiClient.searchCustomersByName(searchName)).thenReturn(expectedCustomers);
        
        // When
        List<CustomerDTO> result = customerService.searchByName(searchName);
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFullName()).isEqualTo("John Doe");
        assertThat(result.get(1).getFullName()).isEqualTo("John Smith");
        verify(customerApiClient).searchCustomersByName(searchName);
    }
    
    @Test
    void searchByName_withNoMatches_returnsEmptyList() {
        // Given
        String searchName = "NonExistent";
        when(customerApiClient.searchCustomersByName(searchName)).thenReturn(Collections.emptyList());
        
        // When
        List<CustomerDTO> result = customerService.searchByName(searchName);
        
        // Then
        assertThat(result).isEmpty();
        verify(customerApiClient).searchCustomersByName(searchName);
    }
    
    @Test
    void searchByName_whenServiceUnavailable_throwsException() {
        // Given
        String searchName = "John";
        when(customerApiClient.searchCustomersByName(searchName))
            .thenThrow(new CustomerServiceUnavailableException("Customer service unavailable"));
        
        // When/Then
        assertThatThrownBy(() -> customerService.searchByName(searchName))
            .isInstanceOf(CustomerServiceUnavailableException.class)
            .hasMessage("Customer service unavailable");
        verify(customerApiClient).searchCustomersByName(searchName);
    }
    
    @Test
    void searchByDocument_withValidDocument_returnsCustomer() {
        // Given
        String documentNumber = "12345678";
        CustomerDTO expectedCustomer = createCustomerDTO(1L, "John Doe", "DNI", documentNumber, CreditStatus.APPROVED);
        when(customerApiClient.searchCustomerByDocument(documentNumber)).thenReturn(expectedCustomer);
        
        // When
        CustomerDTO result = customerService.searchByDocument(documentNumber);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDocumentNumber()).isEqualTo(documentNumber);
        assertThat(result.getFullName()).isEqualTo("John Doe");
        verify(customerApiClient).searchCustomerByDocument(documentNumber);
    }
    
    @Test
    void searchByDocument_withNonExistentDocument_throwsNotFoundException() {
        // Given
        String documentNumber = "99999999";
        when(customerApiClient.searchCustomerByDocument(documentNumber))
            .thenThrow(new CustomerNotFoundException("Customer not found"));
        
        // When/Then
        assertThatThrownBy(() -> customerService.searchByDocument(documentNumber))
            .isInstanceOf(CustomerNotFoundException.class)
            .hasMessage("Customer not found");
        verify(customerApiClient).searchCustomerByDocument(documentNumber);
    }
    
    @Test
    void searchByDocument_whenServiceUnavailable_throwsException() {
        // Given
        String documentNumber = "12345678";
        when(customerApiClient.searchCustomerByDocument(documentNumber))
            .thenThrow(new CustomerServiceUnavailableException("Customer service unavailable"));
        
        // When/Then
        assertThatThrownBy(() -> customerService.searchByDocument(documentNumber))
            .isInstanceOf(CustomerServiceUnavailableException.class)
            .hasMessage("Customer service unavailable");
        verify(customerApiClient).searchCustomerByDocument(documentNumber);
    }
    
    @Test
    void getById_withValidId_returnsCustomer() {
        // Given
        Long customerId = 1L;
        CustomerDTO expectedCustomer = createCustomerDTO(customerId, "John Doe", "DNI", "12345678", CreditStatus.APPROVED);
        when(customerApiClient.getCustomerById(customerId)).thenReturn(expectedCustomer);
        
        // When
        CustomerDTO result = customerService.getById(customerId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(customerId);
        assertThat(result.getFullName()).isEqualTo("John Doe");
        verify(customerApiClient).getCustomerById(customerId);
    }
    
    @Test
    void getById_withNonExistentId_throwsNotFoundException() {
        // Given
        Long customerId = 999L;
        when(customerApiClient.getCustomerById(customerId))
            .thenThrow(new CustomerNotFoundException("Customer not found"));
        
        // When/Then
        assertThatThrownBy(() -> customerService.getById(customerId))
            .isInstanceOf(CustomerNotFoundException.class)
            .hasMessage("Customer not found");
        verify(customerApiClient).getCustomerById(customerId);
    }
    
    @Test
    void getById_whenServiceUnavailable_throwsException() {
        // Given
        Long customerId = 1L;
        when(customerApiClient.getCustomerById(customerId))
            .thenThrow(new CustomerServiceUnavailableException("Customer service unavailable"));
        
        // When/Then
        assertThatThrownBy(() -> customerService.getById(customerId))
            .isInstanceOf(CustomerServiceUnavailableException.class)
            .hasMessage("Customer service unavailable");
        verify(customerApiClient).getCustomerById(customerId);
    }
    
    @Test
    void validateCreditApproval_withApprovedStatus_doesNotThrowException() {
        // Given
        Long customerId = 1L;
        when(customerApiClient.validateCreditStatus(customerId)).thenReturn(CreditStatus.APPROVED);
        
        // When/Then
        customerService.validateCreditApproval(customerId);
        
        // Then
        verify(customerApiClient).validateCreditStatus(customerId);
    }
    
    @Test
    void validateCreditApproval_withRejectedStatus_throwsException() {
        // Given
        Long customerId = 1L;
        when(customerApiClient.validateCreditStatus(customerId)).thenReturn(CreditStatus.REJECTED);
        
        // When/Then
        assertThatThrownBy(() -> customerService.validateCreditApproval(customerId))
            .isInstanceOf(CreditNotApprovedException.class)
            .hasMessage("Customer credit not approved");
        verify(customerApiClient).validateCreditStatus(customerId);
    }
    
    @Test
    void validateCreditApproval_withPendingStatus_throwsException() {
        // Given
        Long customerId = 1L;
        when(customerApiClient.validateCreditStatus(customerId)).thenReturn(CreditStatus.PENDING);
        
        // When/Then
        assertThatThrownBy(() -> customerService.validateCreditApproval(customerId))
            .isInstanceOf(CreditNotApprovedException.class)
            .hasMessage("Customer credit not approved");
        verify(customerApiClient).validateCreditStatus(customerId);
    }
    
    @Test
    void validateCreditApproval_whenServiceUnavailable_throwsException() {
        // Given
        Long customerId = 1L;
        when(customerApiClient.validateCreditStatus(customerId))
            .thenThrow(new CustomerServiceUnavailableException("Customer service unavailable"));
        
        // When/Then
        assertThatThrownBy(() -> customerService.validateCreditApproval(customerId))
            .isInstanceOf(CustomerServiceUnavailableException.class)
            .hasMessage("Customer service unavailable");
        verify(customerApiClient).validateCreditStatus(customerId);
    }
    
    // Helper method to create CustomerDTO test data
    private CustomerDTO createCustomerDTO(Long id, String fullName, String documentType, 
                                          String documentNumber, CreditStatus creditStatus) {
        CustomerDTO customer = new CustomerDTO();
        customer.setId(id);
        customer.setFullName(fullName);
        customer.setDocumentType(documentType);
        customer.setDocumentNumber(documentNumber);
        customer.setCreditStatus(creditStatus);
        return customer;
    }
}
