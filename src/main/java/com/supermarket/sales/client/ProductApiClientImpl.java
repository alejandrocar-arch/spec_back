package com.supermarket.sales.client;

import com.supermarket.sales.dto.external.ProductDTO;
import com.supermarket.sales.exception.ProductNotFoundException;
import com.supermarket.sales.exception.ProductServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of ProductApiClient with retry and circuit breaker patterns.
 */
@Component
public class ProductApiClientImpl implements ProductApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductApiClientImpl.class);
    
    private final RestTemplate restTemplate;
    private final String productApiBaseUrl;
    
    public ProductApiClientImpl(
            RestTemplate restTemplate,
            @Value("${external.product-api.base-url}") String productApiBaseUrl) {
        this.restTemplate = restTemplate;
        this.productApiBaseUrl = productApiBaseUrl;
    }
    
    @Override
    @Retryable(
        retryFor = {ResourceAccessException.class},
        maxAttemptsExpression = "${external.product-api.retry-attempts}",
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @CircuitBreaker(name = "productApi", fallbackMethod = "searchProductsByNameFallback")
    public List<ProductDTO> searchProductsByName(String name) {
        try {
            String url = productApiBaseUrl + "/products/search?name={name}";
            ResponseEntity<List<ProductDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ProductDTO>>() {},
                    name
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error searching products by name: {}", e.getMessage());
            throw new ProductServiceUnavailableException("Product service unavailable");
        } catch (Exception e) {
            logger.error("Unexpected error searching products by name: {}", e.getMessage());
            throw new ProductServiceUnavailableException("Product service unavailable");
        }
    }
    
    @Override
    @Retryable(
        retryFor = {ResourceAccessException.class},
        maxAttemptsExpression = "${external.product-api.retry-attempts}",
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @CircuitBreaker(name = "productApi", fallbackMethod = "searchProductByBarcodeFallback")
    public ProductDTO searchProductByBarcode(String barcode) {
        try {
            String url = productApiBaseUrl + "/products/barcode/{barcode}";
            ResponseEntity<ProductDTO> response = restTemplate.getForEntity(url, ProductDTO.class, barcode);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Product not found with barcode: {}", barcode);
            throw new ProductNotFoundException("Product not found");
        } catch (HttpClientErrorException e) {
            logger.error("Error searching product by barcode: {}", e.getMessage());
            throw new ProductServiceUnavailableException("Product service unavailable");
        } catch (Exception e) {
            logger.error("Unexpected error searching product by barcode: {}", e.getMessage());
            throw new ProductServiceUnavailableException("Product service unavailable");
        }
    }
    
    @Override
    @Retryable(
        retryFor = {ResourceAccessException.class},
        maxAttemptsExpression = "${external.product-api.retry-attempts}",
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @CircuitBreaker(name = "productApi", fallbackMethod = "getProductByIdFallback")
    public ProductDTO getProductById(Long productId) {
        try {
            String url = productApiBaseUrl + "/products/{id}";
            ResponseEntity<ProductDTO> response = restTemplate.getForEntity(url, ProductDTO.class, productId);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Product not found with ID: {}", productId);
            throw new ProductNotFoundException("Product not found");
        } catch (HttpClientErrorException e) {
            logger.error("Error getting product by ID: {}", e.getMessage());
            throw new ProductServiceUnavailableException("Product service unavailable");
        } catch (Exception e) {
            logger.error("Unexpected error getting product by ID: {}", e.getMessage());
            throw new ProductServiceUnavailableException("Product service unavailable");
        }
    }
    
    @Override
    @Retryable(
        retryFor = {ResourceAccessException.class},
        maxAttemptsExpression = "${external.product-api.retry-attempts}",
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @CircuitBreaker(name = "productApi", fallbackMethod = "decrementStockFallback")
    public void decrementStock(Long productId, Integer quantity) {
        try {
            String url = productApiBaseUrl + "/products/{id}/stock/decrement";
            Map<String, Integer> request = new HashMap<>();
            request.put("quantity", quantity);
            restTemplate.postForEntity(url, request, Void.class, productId);
            logger.info("Decremented stock for product {} by {}", productId, quantity);
        } catch (Exception e) {
            logger.error("Error decrementing stock for product {}: {}", productId, e.getMessage());
            throw new ProductServiceUnavailableException("Product service unavailable");
        }
    }
    
    @Override
    @Retryable(
        retryFor = {ResourceAccessException.class},
        maxAttemptsExpression = "${external.product-api.retry-attempts}",
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @CircuitBreaker(name = "productApi", fallbackMethod = "incrementStockFallback")
    public void incrementStock(Long productId, Integer quantity) {
        try {
            String url = productApiBaseUrl + "/products/{id}/stock/increment";
            Map<String, Integer> request = new HashMap<>();
            request.put("quantity", quantity);
            restTemplate.postForEntity(url, request, Void.class, productId);
            logger.info("Incremented stock for product {} by {}", productId, quantity);
        } catch (Exception e) {
            logger.error("Error incrementing stock for product {}: {}", productId, e.getMessage());
            throw new ProductServiceUnavailableException("Product service unavailable");
        }
    }
    
    // Fallback methods for circuit breaker
    
    private List<ProductDTO> searchProductsByNameFallback(String name, Exception e) {
        logger.error("Circuit breaker fallback for searchProductsByName: {}", e.getMessage());
        throw new ProductServiceUnavailableException("Product service unavailable");
    }
    
    private ProductDTO searchProductByBarcodeFallback(String barcode, Exception e) {
        logger.error("Circuit breaker fallback for searchProductByBarcode: {}", e.getMessage());
        throw new ProductServiceUnavailableException("Product service unavailable");
    }
    
    private ProductDTO getProductByIdFallback(Long productId, Exception e) {
        logger.error("Circuit breaker fallback for getProductById: {}", e.getMessage());
        throw new ProductServiceUnavailableException("Product service unavailable");
    }
    
    private void decrementStockFallback(Long productId, Integer quantity, Exception e) {
        logger.error("Circuit breaker fallback for decrementStock: {}", e.getMessage());
        throw new ProductServiceUnavailableException("Product service unavailable");
    }
    
    private void incrementStockFallback(Long productId, Integer quantity, Exception e) {
        logger.error("Circuit breaker fallback for incrementStock: {}", e.getMessage());
        throw new ProductServiceUnavailableException("Product service unavailable");
    }
}
