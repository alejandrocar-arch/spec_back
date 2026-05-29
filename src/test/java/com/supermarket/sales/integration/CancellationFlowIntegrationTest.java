package com.supermarket.sales.integration;

import com.supermarket.sales.dto.request.AddItemByProductIdRequest;
import com.supermarket.sales.dto.request.CancelSaleRequest;
import com.supermarket.sales.dto.request.CreateSaleRequest;
import com.supermarket.sales.dto.response.SaleDTO;
import com.supermarket.sales.enums.SaleStatus;
import com.supermarket.sales.integration.testdata.ProductDTOTestBuilder;
import com.supermarket.sales.integration.testdata.SaleTestBuilder;
import com.supermarket.sales.integration.testdata.WireMockStubs;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for sale cancellation flow.
 * Tests: Create sale -> Add items -> Cancel -> Verify cannot modify -> Verify no stock changes
 */
class CancellationFlowIntegrationTest extends BaseIntegrationTest {
    
    @Test
    void cancellationFlow_cancelActiveSale_success() throws Exception {
        // Setup test data
        var product1 = ProductDTOTestBuilder.createProduct1();
        var product2 = ProductDTOTestBuilder.createProduct2();
        
        // Stub Product API responses
        WireMockStubs.stubGetProductById(product1);
        WireMockStubs.stubGetProductById(product2);
        
        // Step 1: Create sale
        CreateSaleRequest createRequest = SaleTestBuilder.createSaleRequest();
        MvcResult createResult = mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();
        
        SaleDTO sale = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                SaleDTO.class
        );
        Long saleId = sale.getId();
        
        // Step 2: Add items
        AddItemByProductIdRequest addItem1 = new AddItemByProductIdRequest();
        addItem1.setProductId(product1.getId());
        addItem1.setQuantity(3);
        
        mockMvc.perform(post("/api/v1/sales/" + saleId + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1));
        
        AddItemByProductIdRequest addItem2 = new AddItemByProductIdRequest();
        addItem2.setProductId(product2.getId());
        addItem2.setQuantity(2);
        
        mockMvc.perform(post("/api/v1/sales/" + saleId + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2));
        
        // Step 3: Cancel with reason
        CancelSaleRequest cancelRequest = new CancelSaleRequest();
        cancelRequest.setCancellationReason("Customer decided not to purchase");
        
        MvcResult cancelResult = mockMvc.perform(post("/api/v1/sales/" + saleId + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationReason").value("Customer decided not to purchase"))
                .andExpect(jsonPath("$.cancelledAt").exists())
                .andReturn();
        
        SaleDTO cancelledSale = objectMapper.readValue(
                cancelResult.getResponse().getContentAsString(),
                SaleDTO.class
        );
        
        assertThat(cancelledSale.getStatus()).isEqualTo(SaleStatus.CANCELLED);
        assertThat(cancelledSale.getCancellationReason()).isEqualTo("Customer decided not to purchase");
        assertThat(cancelledSale.getCancelledAt()).isNotNull();
        
        // Step 4: Verify cannot modify cancelled sale (try to add item)
        AddItemByProductIdRequest addItemAfterCancel = new AddItemByProductIdRequest();
        addItemAfterCancel.setProductId(product1.getId());
        addItemAfterCancel.setQuantity(1);
        
        mockMvc.perform(post("/api/v1/sales/" + saleId + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItemAfterCancel)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Sale is not active"));
        
        // Step 5: Verify no stock changes (no decrement or increment calls)
        verify(0, postRequestedFor(urlMatching("/api/v1/products/.*/decrement-stock")));
        verify(0, postRequestedFor(urlMatching("/api/v1/products/.*/increment-stock")));
    }
    
    @Test
    void cancellationFlow_cancelFrozenSale_success() throws Exception {
        // Setup test data
        var product1 = ProductDTOTestBuilder.createProduct1();
        
        // Stub Product API responses
        WireMockStubs.stubGetProductById(product1);
        
        // Create sale and add items
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
        Long saleId = sale.getId();
        
        AddItemByProductIdRequest addItem = new AddItemByProductIdRequest();
        addItem.setProductId(product1.getId());
        addItem.setQuantity(2);
        
        mockMvc.perform(post("/api/v1/sales/" + saleId + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem)))
                .andExpect(status().isOk());
        
        // Freeze the sale
        mockMvc.perform(post("/api/v1/sales/" + saleId + "/freeze")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FROZEN"));
        
        // Cancel frozen sale
        CancelSaleRequest cancelRequest = new CancelSaleRequest();
        cancelRequest.setCancellationReason("Customer left without completing purchase");
        
        mockMvc.perform(post("/api/v1/sales/" + saleId + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationReason").value("Customer left without completing purchase"));
        
        // Verify no stock changes
        verify(0, postRequestedFor(urlMatching("/api/v1/products/.*/decrement-stock")));
        verify(0, postRequestedFor(urlMatching("/api/v1/products/.*/increment-stock")));
    }
}
