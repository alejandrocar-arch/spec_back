package com.supermarket.sales.integration;

import com.supermarket.sales.dto.request.AddItemByProductIdRequest;
import com.supermarket.sales.dto.request.CheckoutRequest;
import com.supermarket.sales.dto.request.CreateSaleRequest;
import com.supermarket.sales.dto.request.FullReturnRequest;
import com.supermarket.sales.dto.response.ReturnResponse;
import com.supermarket.sales.dto.response.SaleDTO;
import com.supermarket.sales.enums.PaymentType;
import com.supermarket.sales.enums.SaleStatus;
import com.supermarket.sales.integration.testdata.ProductDTOTestBuilder;
import com.supermarket.sales.integration.testdata.SaleTestBuilder;
import com.supermarket.sales.integration.testdata.WireMockStubs;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for full return flow.
 * Tests: Complete a cash sale -> Process full return -> Verify stock increment
 */
class FullReturnFlowIntegrationTest extends BaseIntegrationTest {
    
    @Test
    void fullReturnFlow_afterCompletedSale_success() throws Exception {
        // Setup test data
        var product1 = ProductDTOTestBuilder.createProduct1();
        var product2 = ProductDTOTestBuilder.createProduct2();
        
        // Stub Product API responses
        WireMockStubs.stubGetProductById(product1);
        WireMockStubs.stubGetProductById(product2);
        WireMockStubs.stubDecrementStockAny(product1.getId());
        WireMockStubs.stubDecrementStockAny(product2.getId());
        WireMockStubs.stubIncrementStockAny(product1.getId());
        WireMockStubs.stubIncrementStockAny(product2.getId());
        
        // Step 1: Create and complete a cash sale
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
        
        // Add items
        AddItemByProductIdRequest addItem1 = new AddItemByProductIdRequest();
        addItem1.setProductId(product1.getId());
        addItem1.setQuantity(2);
        
        mockMvc.perform(post("/api/v1/sales/" + saleId + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem1)))
                .andExpect(status().isOk());
        
        AddItemByProductIdRequest addItem2 = new AddItemByProductIdRequest();
        addItem2.setProductId(product2.getId());
        addItem2.setQuantity(3);
        
        mockMvc.perform(post("/api/v1/sales/" + saleId + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem2)))
                .andExpect(status().isOk());
        
        // Checkout
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentType(PaymentType.CASH);
        checkoutRequest.setAmountReceived(new BigDecimal("150.00"));
        
        MvcResult checkoutResult = mockMvc.perform(post("/api/v1/sales/" + saleId + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sale.status").value("COMPLETED"))
                .andExpect(jsonPath("$.sale.transactionId").exists())
                .andReturn();
        
        String transactionId = objectMapper.readTree(checkoutResult.getResponse().getContentAsString())
                .path("sale").path("transactionId").asText();
        
        // Step 2: Process full return
        FullReturnRequest returnRequest = new FullReturnRequest();
        returnRequest.setReturnReason("Customer changed mind");
        
        MvcResult returnResult = mockMvc.perform(post("/api/v1/sales/" + saleId + "/return/full")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(returnRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sale.status").value("RETURNED"))
                .andExpect(jsonPath("$.sale.returnReason").value("Customer changed mind"))
                .andExpect(jsonPath("$.returnReceipt").exists())
                .andExpect(jsonPath("$.returnReceipt.originalTransactionId").value(transactionId))
                .andExpect(jsonPath("$.returnReceipt.returnReason").value("Customer changed mind"))
                .andExpect(jsonPath("$.returnReceipt.returnedItems.length()").value(2))
                .andReturn();
        
        ReturnResponse returnResponse = objectMapper.readValue(
                returnResult.getResponse().getContentAsString(),
                ReturnResponse.class
        );
        
        // Verify return receipt generation
        assertThat(returnResponse.getReturnReceipt()).isNotNull();
        assertThat(returnResponse.getReturnReceipt().getOriginalTransactionId()).isEqualTo(transactionId);
        assertThat(returnResponse.getReturnReceipt().getReturnReason()).isEqualTo("Customer changed mind");
        assertThat(returnResponse.getReturnReceipt().getReturnedItems()).hasSize(2);
        assertThat(returnResponse.getReturnReceipt().getReturnTimestamp()).isNotNull();
        
        // Verify stock increment WireMock calls
        verify(postRequestedFor(urlEqualTo("/api/v1/products/" + product1.getId() + "/increment-stock")));
        verify(postRequestedFor(urlEqualTo("/api/v1/products/" + product2.getId() + "/increment-stock")));
        
        // Verify sale status is RETURNED
        assertThat(returnResponse.getSale().getStatus()).isEqualTo(SaleStatus.RETURNED);
        assertThat(returnResponse.getSale().getReturnedAt()).isNotNull();
    }
}
