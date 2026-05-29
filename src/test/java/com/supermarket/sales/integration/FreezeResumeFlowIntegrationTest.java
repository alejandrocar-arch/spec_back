package com.supermarket.sales.integration;

import com.supermarket.sales.dto.request.AddItemByProductIdRequest;
import com.supermarket.sales.dto.request.CheckoutRequest;
import com.supermarket.sales.dto.request.CreateSaleRequest;
import com.supermarket.sales.dto.response.FrozenSaleDTO;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for freeze and resume sale flow.
 * Tests: Create sale -> Add items -> Freeze -> Create another sale -> List frozen -> Resume -> Checkout
 */
class FreezeResumeFlowIntegrationTest extends BaseIntegrationTest {
    
    @Test
    void freezeAndResumeSaleFlow_withMultipleSales_success() throws Exception {
        // Setup test data
        var product1 = ProductDTOTestBuilder.createProduct1();
        var product2 = ProductDTOTestBuilder.createProduct2();
        
        // Stub Product API responses
        WireMockStubs.stubGetProductById(product1);
        WireMockStubs.stubGetProductById(product2);
        WireMockStubs.stubDecrementStockAny(product1.getId());
        WireMockStubs.stubDecrementStockAny(product2.getId());
        
        // Step 1: Create first sale
        CreateSaleRequest createRequest1 = SaleTestBuilder.createSaleRequest();
        MvcResult createResult1 = mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();
        
        SaleDTO sale1 = objectMapper.readValue(
                createResult1.getResponse().getContentAsString(),
                SaleDTO.class
        );
        Long saleId1 = sale1.getId();
        
        // Step 2: Add items to first sale
        AddItemByProductIdRequest addItem1 = new AddItemByProductIdRequest();
        addItem1.setProductId(product1.getId());
        addItem1.setQuantity(3);
        
        mockMvc.perform(post("/api/v1/sales/" + saleId1 + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1));
        
        // Step 3: Freeze first sale
        mockMvc.perform(post("/api/v1/sales/" + saleId1 + "/freeze")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FROZEN"))
                .andExpect(jsonPath("$.frozenAt").exists())
                .andExpect(jsonPath("$.items.length()").value(1));
        
        // Step 4: Create another sale (for different customer)
        CreateSaleRequest createRequest2 = SaleTestBuilder.createSaleRequest();
        MvcResult createResult2 = mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();
        
        SaleDTO sale2 = objectMapper.readValue(
                createResult2.getResponse().getContentAsString(),
                SaleDTO.class
        );
        Long saleId2 = sale2.getId();
        
        // Add item to second sale and complete it quickly
        AddItemByProductIdRequest addItem2 = new AddItemByProductIdRequest();
        addItem2.setProductId(product2.getId());
        addItem2.setQuantity(1);
        
        mockMvc.perform(post("/api/v1/sales/" + saleId2 + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem2)))
                .andExpect(status().isOk());
        
        // Step 5: List frozen sales by terminal
        MvcResult frozenListResult = mockMvc.perform(get("/api/v1/sales/terminal/TERM-001/frozen")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(saleId1))
                .andExpect(jsonPath("$[0].status").value("FROZEN"))
                .andExpect(jsonPath("$[0].itemCount").value(1))
                .andReturn();
        
        FrozenSaleDTO[] frozenSales = objectMapper.readValue(
                frozenListResult.getResponse().getContentAsString(),
                FrozenSaleDTO[].class
        );
        
        assertThat(frozenSales).hasSize(1);
        assertThat(frozenSales[0].getId()).isEqualTo(saleId1);
        assertThat(frozenSales[0].getItemCount()).isEqualTo(1);
        
        // Step 6: Resume first sale
        mockMvc.perform(post("/api/v1/sales/" + saleId1 + "/resume")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(3));
        
        // Step 7: Complete checkout on resumed sale
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentType(PaymentType.CASH);
        checkoutRequest.setAmountReceived(new BigDecimal("50.00"));
        
        mockMvc.perform(post("/api/v1/sales/" + saleId1 + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sale.status").value("COMPLETED"))
                .andExpect(jsonPath("$.sale.transactionId").exists())
                .andExpect(jsonPath("$.receipt").exists());
        
        // Verify state transitions: ACTIVE -> FROZEN -> ACTIVE -> COMPLETED
        MvcResult finalSaleResult = mockMvc.perform(get("/api/v1/sales/" + saleId1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andReturn();
        
        SaleDTO finalSale = objectMapper.readValue(
                finalSaleResult.getResponse().getContentAsString(),
                SaleDTO.class
        );
        
        assertThat(finalSale.getStatus()).isEqualTo(SaleStatus.COMPLETED);
        assertThat(finalSale.getFrozenAt()).isNotNull();
        assertThat(finalSale.getCompletedAt()).isNotNull();
    }
}
