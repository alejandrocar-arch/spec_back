package com.supermarket.sales.integration.testdata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supermarket.sales.dto.external.CustomerDTO;
import com.supermarket.sales.dto.external.ProductDTO;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Helper class for creating WireMock stubs for external APIs.
 */
public class WireMockStubs {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // ========== Product API Stubs ==========
    
    public static void stubGetProductById(ProductDTO product) throws JsonProcessingException {
        stubFor(get(urlEqualTo("/api/v1/products/" + product.getId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(product))));
    }
    
    public static void stubGetProductByBarcode(ProductDTO product) throws JsonProcessingException {
        stubFor(get(urlPathEqualTo("/api/v1/products/barcode/" + product.getBarcode()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(product))));
    }
    
    public static void stubProductNotFound(Long productId) {
        stubFor(get(urlEqualTo("/api/v1/products/" + productId))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Product not found\"}")));
    }
    
    public static void stubProductServiceUnavailable(Long productId) {
        stubFor(get(urlEqualTo("/api/v1/products/" + productId))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Service unavailable\"}")));
    }
    
    public static void stubDecrementStock(Long productId, Integer quantity) {
        stubFor(post(urlEqualTo("/api/v1/products/" + productId + "/decrement-stock"))
                .withRequestBody(equalToJson("{\"quantity\":" + quantity + "}"))
                .willReturn(aResponse()
                        .withStatus(200)));
    }
    
    public static void stubDecrementStockAny(Long productId) {
        stubFor(post(urlEqualTo("/api/v1/products/" + productId + "/decrement-stock"))
                .willReturn(aResponse()
                        .withStatus(200)));
    }
    
    public static void stubIncrementStock(Long productId, Integer quantity) {
        stubFor(post(urlEqualTo("/api/v1/products/" + productId + "/increment-stock"))
                .withRequestBody(equalToJson("{\"quantity\":" + quantity + "}"))
                .willReturn(aResponse()
                        .withStatus(200)));
    }
    
    public static void stubIncrementStockAny(Long productId) {
        stubFor(post(urlEqualTo("/api/v1/products/" + productId + "/increment-stock"))
                .willReturn(aResponse()
                        .withStatus(200)));
    }
    
    public static void stubInsufficientStock(Long productId) {
        stubFor(get(urlEqualTo("/api/v1/products/" + productId))
                .willReturn(aResponse()
                        .withStatus(409)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Insufficient stock available\"}")));
    }
    
    // ========== Customer API Stubs ==========
    
    public static void stubGetCustomerById(CustomerDTO customer) throws JsonProcessingException {
        stubFor(get(urlEqualTo("/api/v1/customers/" + customer.getId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(customer))));
    }
    
    public static void stubCustomerNotFound(Long customerId) {
        stubFor(get(urlEqualTo("/api/v1/customers/" + customerId))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Customer not found\"}")));
    }
    
    public static void stubCustomerServiceUnavailable(Long customerId) {
        stubFor(get(urlEqualTo("/api/v1/customers/" + customerId))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Service unavailable\"}")));
    }
}
