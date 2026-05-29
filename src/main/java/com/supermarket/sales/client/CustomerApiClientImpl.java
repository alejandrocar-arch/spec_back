package com.supermarket.sales.client;

import com.supermarket.sales.dto.external.CustomerDTO;
import com.supermarket.sales.enums.CreditStatus;
import com.supermarket.sales.exception.CustomerNotFoundException;
import com.supermarket.sales.exception.CustomerServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Implementation of CustomerApiClient with retry and circuit breaker patterns.
 */
@Component
public class CustomerApiClientImpl implements CustomerApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerApiClientImpl.class);
    
    private final RestTemplate restTemplate;
    private final String customerApiBaseUrl;
    
    public CustomerApiClientImpl(
            RestTemplate restTemplate,
            @Value("${external.customer-api.base-url}") String customerApiBaseUrl) {
        this.restTemplate = restTemplate;
        this.customerApiBaseUrl = customerApiBaseUrl;
    }
    
    @Override
    @Retryable(
        retryFor = {ResourceAccessException.class},
        maxAttemptsExpression = "${external.customer-api.retry-attempts}",
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @CircuitBreaker(name = "customerApi", fallbackMethod = "searchCustomersByNameFallback")
    public List<CustomerDTO> searchCustomersByName(String name) {
        try {
            String url = customerApiBaseUrl + "/customers/search?name={name}";
            ResponseEntity<List<CustomerDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<CustomerDTO>>() {},
                    name
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error searching customers by name: {}", e.getMessage());
            throw new CustomerServiceUnavailableException("Customer service unavailable");
        } catch (Exception e) {
            logger.error("Unexpected error searching customers by name: {}", e.getMessage());
            throw new CustomerServiceUnavailableException("Customer service unavailable");
        }
    }
    
    @Override
    @Retryable(
        retryFor = {ResourceAccessException.class},
        maxAttemptsExpression = "${external.customer-api.retry-attempts}",
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @CircuitBreaker(name = "customerApi", fallbackMethod = "searchCustomerByDocumentFallback")
    public CustomerDTO searchCustomerByDocument(String documentNumber) {
        try {
            String url = customerApiBaseUrl + "/customers/document/{documentNumber}";
            ResponseEntity<CustomerDTO> response = restTemplate.getForEntity(url, CustomerDTO.class, documentNumber);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Customer not found with document: {}", documentNumber);
            throw new CustomerNotFoundException("Customer not found");
        } catch (HttpClientErrorException e) {
            logger.error("Error searching customer by document: {}", e.getMessage());
            throw new CustomerServiceUnavailableException("Customer service unavailable");
        } catch (Exception e) {
            logger.error("Unexpected error searching customer by document: {}", e.getMessage());
            throw new CustomerServiceUnavailableException("Customer service unavailable");
        }
    }
    
    @Override
    @Retryable(
        retryFor = {ResourceAccessException.class},
        maxAttemptsExpression = "${external.customer-api.retry-attempts}",
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @CircuitBreaker(name = "customerApi", fallbackMethod = "getCustomerByIdFallback")
    public CustomerDTO getCustomerById(Long customerId) {
        try {
            String url = customerApiBaseUrl + "/customers/{id}";
            ResponseEntity<CustomerDTO> response = restTemplate.getForEntity(url, CustomerDTO.class, customerId);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Customer not found with ID: {}", customerId);
            throw new CustomerNotFoundException("Customer not found");
        } catch (HttpClientErrorException e) {
            logger.error("Error getting customer by ID: {}", e.getMessage());
            throw new CustomerServiceUnavailableException("Customer service unavailable");
        } catch (Exception e) {
            logger.error("Unexpected error getting customer by ID: {}", e.getMessage());
            throw new CustomerServiceUnavailableException("Customer service unavailable");
        }
    }
    
    @Override
    @Retryable(
        retryFor = {ResourceAccessException.class},
        maxAttemptsExpression = "${external.customer-api.retry-attempts}",
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @CircuitBreaker(name = "customerApi", fallbackMethod = "validateCreditStatusFallback")
    public CreditStatus validateCreditStatus(Long customerId) {
        try {
            String url = customerApiBaseUrl + "/customers/{id}/credit-status";
            ResponseEntity<CreditStatus> response = restTemplate.getForEntity(url, CreditStatus.class, customerId);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error validating credit status for customer {}: {}", customerId, e.getMessage());
            throw new CustomerServiceUnavailableException("Customer service unavailable");
        }
    }
    
    // Fallback methods for circuit breaker
    
    private List<CustomerDTO> searchCustomersByNameFallback(String name, Exception e) {
        logger.error("Circuit breaker fallback for searchCustomersByName: {}", e.getMessage());
        throw new CustomerServiceUnavailableException("Customer service unavailable");
    }
    
    private CustomerDTO searchCustomerByDocumentFallback(String documentNumber, Exception e) {
        logger.error("Circuit breaker fallback for searchCustomerByDocument: {}", e.getMessage());
        throw new CustomerServiceUnavailableException("Customer service unavailable");
    }
    
    private CustomerDTO getCustomerByIdFallback(Long customerId, Exception e) {
        logger.error("Circuit breaker fallback for getCustomerById: {}", e.getMessage());
        throw new CustomerServiceUnavailableException("Customer service unavailable");
    }
    
    private CreditStatus validateCreditStatusFallback(Long customerId, Exception e) {
        logger.error("Circuit breaker fallback for validateCreditStatus: {}", e.getMessage());
        throw new CustomerServiceUnavailableException("Customer service unavailable");
    }
}
