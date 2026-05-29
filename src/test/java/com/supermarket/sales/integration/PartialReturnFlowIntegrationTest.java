package com.supermarket.sales.integration;

import com.supermarket.sales.dto.request.*;
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
import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for partial return flow.
 * Tests: Complete sale with 5 items -> Return 2 items -> Return remaining items
 */
class PartialReturnFlowIntegrationTest extends BaseIntegrationTest {
    
    @Test
    void partialReturnFlow_returnItemsInMultipleSteps_success() throws Exception {
        // Setup test data - 5 different products
        var product1 = ProductDTOTestBuilder.createProduct1();
        var product2 = ProductDTOTestBuilder.createProduct2();
        var product3 = ProductDTOTestBuilder.createProduct3();
        var product4 = ProductDTOTestBuilder.createProduct4();
        var product5 = ProductDTOTestBuilder.createProduct5();
        
        // Stub Product API responses
        WireMockStubs.stubGetProductById(product1);
        WireMockStubs.stubGetProductById(product2);
        WireMockStubs.stubGetProductById(product3);
        WireMockStubs.stubGetProductById(product4);
        WireMockStubs.stubGetProductById(product5);
        WireMockStubs.stubDecrementStockAny(product1.getId());
        WireMockStubs.stubDecrementStockAny(product2.getId());
        WireMockStubs.stubDecrementStockAny(product3.getId());
        WireMockStubs.stubDecrementStockAny(product4.getId());
        WireMockStubs.stubDecrementStockAny(product5.getId());
        WireMockStubs.stubIncrementStockAny(product1.getId());
        WireMockStubs.stubIncrementStockAny(product2.getId());
        WireMockStubs.stubIncrementStockAny(product3.getId());
        WireMockStubs.stubIncrementStockAny(product4.getId());
        WireMockStubs.stubIncrementStockAny(product5.getId());
        
        // Step 1: Create and complete a sale with 5 items
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
        
        // Add 5 items
        AddItemByProductIdRequest addItem1 = new AddItemByProductIdRequest();
        addItem1.setProductId(product1.getId());
        addItem1.setQuantity(2);
        mockMvc.perform(post("/api/v1/sales/" + saleId + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem1)))
                .andExpect(status().isOk());
        
        AddItemByProductIdRequest addItem2 = new AddItemByProductIdRequest();
        addItem2.setProductId(product2.getId());
        addItem2.setQuantity(1);
        mockMvc.perform(post("/api/v1/sales/" + saleId + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem2)))
                .andExpect(status().isOk());
        
        AddItemByProductIdRequest addItem3 = new AddItemByProductIdRequest();
        addItem3.setProductId(product3.getId());
        addItem3.setQuantity(3);
        mockMvc.perform(post("/api/v1/sales/" + saleId + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem3)))
                .andExpect(status().isOk());
        
        AddItemByProductIdRequest addItem4 = new AddItemByProductIdRequest();
        addItem4.setProductId(product4.getId());
        addItem4.setQuantity(5);
        mockMvc.perform(post("/api/v1/sales/" + saleId + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem4)))
                .andExpect(status().isOk());
        
        AddItemByProductIdRequest addItem5 = new AddItemByProductIdRequest();
        addItem5.setProductId(product5.getId());
        addItem5.setQuantity(1);
        MvcResult addItemResult = mockMvc.perform(post("/api/v1/sales/" + saleId + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(5))
                .andReturn();
        
        // Get item IDs for return
        SaleDTO saleWithItems = objectMapper.readValue(
                addItemResult.getResponse().getContentAsString(),
                SaleDTO.class
        );
        Long itemId1 = saleWithItems.getItems().get(0).getId(); // Product 1
        Long itemId2 = saleWithItems.getItems().get(1).getId(); // Product 2
        Long itemId3 = saleWithItems.getItems().get(2).getId(); // Product 3
        Long itemId4 = saleWithItems.getItems().get(3).getId(); // Product 4
        Long itemId5 = saleWithItems.getItems().get(4).getId(); // Product 5
        
        // Checkout
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentType(PaymentType.CASH);
        checkoutRequest.setAmountReceived(new BigDecimal("200.00"));
        
        mockMvc.perform(post("/api/v1/sales/" + saleId + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sale.status").value("COMPLETED"));
        
        // Step 2: Return 2 items (partial return)
        ReturnItemRequest returnItem1 = new ReturnItemRequest();
        returnItem1.setItemId(itemId1);
        returnItem1.setQuantity(1); // Return 1 out of 2
        
        ReturnItemRequest returnItem2 = new ReturnItemRequest();
        returnItem2.setItemId(itemId3);
        returnItem2.setQuantity(2); // Return 2 out of 3
        
        PartialReturnRequest partialReturnRequest = new PartialReturnRequest();
        partialReturnRequest.setReturnReason("Partial return - some items defective");
        partialReturnRequest.setReturnItems(Arrays.asList(returnItem1, returnItem2));
        
        MvcResult partialReturnResult = mockMvc.perform(post("/api/v1/sales/" + saleId + "/return/partial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialReturnRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sale.status").value("PARTIALLY_RETURNED"))
                .andExpect(jsonPath("$.returnReceipt").exists())
                .andExpect(jsonPath("$.returnReceipt.returnedItems.length()").value(2))
                .andReturn();
        
        ReturnResponse partialReturnResponse = objectMapper.readValue(
                partialReturnResult.getResponse().getContentAsString(),
                ReturnResponse.class
        );
        
        // Verify partial return receipt
        assertThat(partialReturnResponse.getReturnReceipt()).isNotNull();
        assertThat(partialReturnResponse.getReturnReceipt().getReturnedItems()).hasSize(2);
        
        // Verify partial stock increment WireMock calls
        verify(postRequestedFor(urlEqualTo("/api/v1/products/" + product1.getId() + "/increment-stock")));
        verify(postRequestedFor(urlEqualTo("/api/v1/products/" + product3.getId() + "/increment-stock")));
        
        // Verify sale status is PARTIALLY_RETURNED
        assertThat(partialReturnResponse.getSale().getStatus()).isEqualTo(SaleStatus.PARTIALLY_RETURNED);
        
        // Step 3: Return remaining items
        ReturnItemRequest returnItem1Remaining = new ReturnItemRequest();
        returnItem1Remaining.setItemId(itemId1);
        returnItem1Remaining.setQuantity(1); // Return remaining 1
        
        ReturnItemRequest returnItem2Full = new ReturnItemRequest();
        returnItem2Full.setItemId(itemId2);
        returnItem2Full.setQuantity(1); // Return all
        
        ReturnItemRequest returnItem3Remaining = new ReturnItemRequest();
        returnItem3Remaining.setItemId(itemId3);
        returnItem3Remaining.setQuantity(1); // Return remaining 1
        
        ReturnItemRequest returnItem4Full = new ReturnItemRequest();
        returnItem4Full.setItemId(itemId4);
        returnItem4Full.setQuantity(5); // Return all
        
        ReturnItemRequest returnItem5Full = new ReturnItemRequest();
        returnItem5Full.setItemId(itemId5);
        returnItem5Full.setQuantity(1); // Return all
        
        PartialReturnRequest finalReturnRequest = new PartialReturnRequest();
        finalReturnRequest.setReturnReason("Returning all remaining items");
        finalReturnRequest.setReturnItems(Arrays.asList(
                returnItem1Remaining, returnItem2Full, returnItem3Remaining, 
                returnItem4Full, returnItem5Full
        ));
        
        MvcResult finalReturnResult = mockMvc.perform(post("/api/v1/sales/" + saleId + "/return/partial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(finalReturnRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sale.status").value("RETURNED"))
                .andExpect(jsonPath("$.returnReceipt").exists())
                .andReturn();
        
        ReturnResponse finalReturnResponse = objectMapper.readValue(
                finalReturnResult.getResponse().getContentAsString(),
                ReturnResponse.class
        );
        
        // Verify final status is RETURNED (all items returned)
        assertThat(finalReturnResponse.getSale().getStatus()).isEqualTo(SaleStatus.RETURNED);
        
        // Verify all products had stock incremented
        verify(atLeast(1), postRequestedFor(urlEqualTo("/api/v1/products/" + product1.getId() + "/increment-stock")));
        verify(atLeast(1), postRequestedFor(urlEqualTo("/api/v1/products/" + product2.getId() + "/increment-stock")));
        verify(atLeast(1), postRequestedFor(urlEqualTo("/api/v1/products/" + product3.getId() + "/increment-stock")));
        verify(atLeast(1), postRequestedFor(urlEqualTo("/api/v1/products/" + product4.getId() + "/increment-stock")));
        verify(atLeast(1), postRequestedFor(urlEqualTo("/api/v1/products/" + product5.getId() + "/increment-stock")));
    }
}
