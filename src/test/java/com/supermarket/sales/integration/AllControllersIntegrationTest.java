package com.supermarket.sales.integration;

import com.supermarket.sales.dto.external.CustomerDTO;
import com.supermarket.sales.dto.external.ProductDTO;
import com.supermarket.sales.dto.request.AddItemByProductIdRequest;
import com.supermarket.sales.dto.request.CreateSaleRequest;
import com.supermarket.sales.dto.response.SaleDTO;
import com.supermarket.sales.integration.testdata.CustomerDTOTestBuilder;
import com.supermarket.sales.integration.testdata.ProductDTOTestBuilder;
import com.supermarket.sales.integration.testdata.SaleTestBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for all controllers.
 * Tests ProductSearchController, CustomerSearchController, and SaleController endpoints.
 */
class AllControllersIntegrationTest extends BaseIntegrationTest {
    
    // ========== ProductSearchController Tests ==========
    
    @Test
    void productSearchController_searchByName_returnsProducts() throws Exception {
        // Stub Product API search by name
        ProductDTO product1 = ProductDTOTestBuilder.createProduct1();
        ProductDTO product2 = ProductDTOTestBuilder.createProduct2();
        
        stubFor(get(urlPathEqualTo("/api/v1/products/search"))
                .withQueryParam("name", equalTo("Product"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(Arrays.asList(product1, product2)))));
        
        mockMvc.perform(get("/api/v1/products/search/by-name")
                        .param("name", "Product")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(product1.getId()))
                .andExpect(jsonPath("$[1].id").value(product2.getId()));
    }
    
    @Test
    void productSearchController_searchByBarcode_returnsProduct() throws Exception {
        // Stub Product API search by barcode
        ProductDTO product = ProductDTOTestBuilder.createProduct1();
        
        stubFor(get(urlPathEqualTo("/api/v1/products/barcode/" + product.getBarcode()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(product))));
        
        mockMvc.perform(get("/api/v1/products/search/by-barcode")
                        .param("barcode", product.getBarcode())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.barcode").value(product.getBarcode()));
    }
    
    @Test
    void productSearchController_searchByBarcode_notFound_returns404() throws Exception {
        // Stub Product API to return 404
        stubFor(get(urlPathEqualTo("/api/v1/products/barcode/INVALID"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Product not found\"}")));
        
        mockMvc.perform(get("/api/v1/products/search/by-barcode")
                        .param("barcode", "INVALID")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found"));
    }
    
    @Test
    void productSearchController_serviceUnavailable_returns503() throws Exception {
        // Stub Product API to return 503
        stubFor(get(urlPathEqualTo("/api/v1/products/search"))
                .withQueryParam("name", equalTo("Test"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Service unavailable\"}")));
        
        mockMvc.perform(get("/api/v1/products/search/by-name")
                        .param("name", "Test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Product service unavailable"));
    }
    
    // ========== CustomerSearchController Tests ==========
    
    @Test
    void customerSearchController_searchByName_returnsCustomers() throws Exception {
        // Stub Customer API search by name
        CustomerDTO customer1 = CustomerDTOTestBuilder.createApprovedCustomer();
        CustomerDTO customer2 = CustomerDTOTestBuilder.createRejectedCustomer();
        
        stubFor(get(urlPathEqualTo("/api/v1/customers/search"))
                .withQueryParam("name", equalTo("John"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(Arrays.asList(customer1, customer2)))));
        
        mockMvc.perform(get("/api/v1/customers/search/by-name")
                        .param("name", "John")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(customer1.getId()))
                .andExpect(jsonPath("$[1].id").value(customer2.getId()));
    }
    
    @Test
    void customerSearchController_searchByDocument_returnsCustomer() throws Exception {
        // Stub Customer API search by document
        CustomerDTO customer = CustomerDTOTestBuilder.createApprovedCustomer();
        
        stubFor(get(urlPathEqualTo("/api/v1/customers/document/" + customer.getDocumentNumber()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(customer))));
        
        mockMvc.perform(get("/api/v1/customers/search/by-document")
                        .param("documentNumber", customer.getDocumentNumber())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customer.getId()))
                .andExpect(jsonPath("$.documentNumber").value(customer.getDocumentNumber()));
    }
    
    @Test
    void customerSearchController_searchByDocument_notFound_returns404() throws Exception {
        // Stub Customer API to return 404
        stubFor(get(urlPathEqualTo("/api/v1/customers/document/99999999"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Customer not found\"}")));
        
        mockMvc.perform(get("/api/v1/customers/search/by-document")
                        .param("documentNumber", "99999999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }
    
    @Test
    void customerSearchController_serviceUnavailable_returns503() throws Exception {
        // Stub Customer API to return 503
        stubFor(get(urlPathEqualTo("/api/v1/customers/search"))
                .withQueryParam("name", equalTo("Test"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Service unavailable\"}")));
        
        mockMvc.perform(get("/api/v1/customers/search/by-name")
                        .param("name", "Test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Customer service unavailable"));
    }
    
    // ========== SaleController Tests ==========
    
    @Test
    void saleController_createSale_returns201() throws Exception {
        CreateSaleRequest request = SaleTestBuilder.createSaleRequest();
        
        mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.terminalId").value("TERM-001"))
                .andExpect(jsonPath("$.cashierId").value("CASH-001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
    
    @Test
    void saleController_getSaleById_returns200() throws Exception {
        // Create a sale first
        CreateSaleRequest request = SaleTestBuilder.createSaleRequest();
        MvcResult createResult = mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        
        SaleDTO sale = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                SaleDTO.class
        );
        
        // Get the sale by ID
        mockMvc.perform(get("/api/v1/sales/" + sale.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sale.getId()))
                .andExpect(jsonPath("$.terminalId").value("TERM-001"));
    }
    
    @Test
    void saleController_getSaleById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/sales/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Sale not found"));
    }
    
    @Test
    void saleController_getSalesByTerminal_returns200() throws Exception {
        // Create multiple sales for the same terminal
        CreateSaleRequest request1 = SaleTestBuilder.createSaleRequest();
        mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());
        
        CreateSaleRequest request2 = SaleTestBuilder.createSaleRequest();
        mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());
        
        // Get sales by terminal
        mockMvc.perform(get("/api/v1/sales/terminal/TERM-001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
    
    @Test
    void saleController_validationError_returns400() throws Exception {
        // Create request with missing required fields
        CreateSaleRequest invalidRequest = new CreateSaleRequest();
        invalidRequest.setTerminalId(""); // Empty terminal ID
        invalidRequest.setCashierId(null); // Null cashier ID
        
        mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists());
    }
    
    @Test
    void saleController_addItemValidation_returns400() throws Exception {
        // Create a sale
        CreateSaleRequest createRequest = SaleTestBuilder.createSaleRequest();
        MvcResult createResult = mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        SaleDTO sale = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                SaleDTO.class
        );
        
        // Try to add item with invalid quantity
        AddItemByProductIdRequest invalidRequest = new AddItemByProductIdRequest();
        invalidRequest.setProductId(1L);
        invalidRequest.setQuantity(0); // Invalid: must be at least 1
        
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists());
    }
    
    @Test
    void saleController_errorResponseFormat_isConsistent() throws Exception {
        // Test that error responses have consistent format
        mockMvc.perform(get("/api/v1/sales/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists());
    }
    
    @Test
    void saleController_httpStatusCodes_areCorrect() throws Exception {
        // Test various HTTP status codes
        
        // 201 Created
        CreateSaleRequest createRequest = SaleTestBuilder.createSaleRequest();
        MvcResult createResult = mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        SaleDTO sale = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                SaleDTO.class
        );
        
        // 200 OK
        mockMvc.perform(get("/api/v1/sales/" + sale.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        // 404 Not Found
        mockMvc.perform(get("/api/v1/sales/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        
        // 400 Bad Request (validation error)
        CreateSaleRequest invalidRequest = new CreateSaleRequest();
        mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        // 409 Conflict (business rule violation)
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/resume")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }
}
