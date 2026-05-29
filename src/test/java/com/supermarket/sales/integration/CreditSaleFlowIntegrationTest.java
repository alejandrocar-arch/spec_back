package com.supermarket.sales.integration;

import com.supermarket.sales.dto.request.AddItemByProductIdRequest;
import com.supermarket.sales.dto.request.CheckoutRequest;
import com.supermarket.sales.dto.request.CreateSaleRequest;
import com.supermarket.sales.dto.response.CheckoutResponse;
import com.supermarket.sales.dto.response.SaleDTO;
import com.supermarket.sales.enums.PaymentType;
import com.supermarket.sales.enums.SaleStatus;
import com.supermarket.sales.integration.testdata.CustomerDTOTestBuilder;
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
 * Integration test for complete credit sale flow.
 * Tests: Create sale with customer -> Add items -> Checkout with credit
 */
class CreditSaleFlowIntegrationTest extends BaseIntegrationTest {
    
    @Test
    void completeCreditSaleFlow_withApprovedCustomer_success() throws Exception {
        // Setup test data
        var customer = CustomerDTOTestBuilder.createApprovedCustomer();
        var product1 = ProductDTOTestBuilder.createProduct1(); // $10.00
        var product2 = ProductDTOTestBuilder.createProduct2(); // $20.00
        
        // Stub external API responses
        WireMockStubs.stubGetCustomerById(customer);
        WireMockStubs.stubGetProductById(product1);
        WireMockStubs.stubGetProductById(product2);
        WireMockStubs.stubDecrementStockAny(product1.getId());
        WireMockStubs.stubDecrementStockAny(product2.getId());
        
        // Step 1: Create sale with customer
        CreateSaleRequest createRequest = SaleTestBuilder.createSaleRequestWithCustomer(customer.getId());
        MvcResult createResult = mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.customerId").value(customer.getId()))
                .andReturn();
        
        SaleDTO sale = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                SaleDTO.class
        );
        Long saleId = sale.getId();
        
        // Step 2: Add items
        AddItemByProductIdRequest addItem1 = new AddItemByProductIdRequest();
        addItem1.setProductId(product1.getId());
        addItem1.setQuantity(5);
        
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
        
        // Step 3: Checkout with credit
        // Subtotal: (5*10) + (2*20) = 50 + 40 = 90.00
        // Tax (19%): 90.00 * 0.19 = 17.10
        // Total: 90.00 + 17.10 = 107.10
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentType(PaymentType.CREDIT);
        checkoutRequest.setCustomerId(customer.getId());
        
        MvcResult checkoutResult = mockMvc.perform(post("/api/v1/sales/" + saleId + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sale.status").value("COMPLETED"))
                .andExpect(jsonPath("$.sale.paymentType").value("CREDIT"))
                .andExpect(jsonPath("$.sale.transactionId").exists())
                .andExpect(jsonPath("$.sale.creditReferenceNumber").exists())
                .andExpect(jsonPath("$.receipt").exists())
                .andExpect(jsonPath("$.receipt.paymentType").value("CREDIT"))
                .andExpect(jsonPath("$.receipt.creditReferenceNumber").exists())
                .andExpect(jsonPath("$.receipt.customerInfo").exists())
                .andReturn();
        
        CheckoutResponse checkoutResponse = objectMapper.readValue(
                checkoutResult.getResponse().getContentAsString(),
                CheckoutResponse.class
        );
        
        // Verify credit validation WireMock call
        verify(getRequestedFor(urlEqualTo("/api/v1/customers/" + customer.getId())));
        
        // Verify receipt with credit reference
        assertThat(checkoutResponse.getReceipt()).isNotNull();
        assertThat(checkoutResponse.getReceipt().getPaymentType()).isEqualTo(PaymentType.CREDIT);
        assertThat(checkoutResponse.getReceipt().getCreditReferenceNumber()).isNotNull();
        assertThat(checkoutResponse.getReceipt().getCustomerInfo()).isNotNull();
        assertThat(checkoutResponse.getReceipt().getCustomerInfo().getId()).isEqualTo(customer.getId());
        assertThat(checkoutResponse.getReceipt().getAmountReceived()).isNull();
        assertThat(checkoutResponse.getReceipt().getChange()).isNull();
        
        // Verify stock decrement WireMock calls
        verify(postRequestedFor(urlEqualTo("/api/v1/products/" + product1.getId() + "/decrement-stock")));
        verify(postRequestedFor(urlEqualTo("/api/v1/products/" + product2.getId() + "/decrement-stock")));
        
        // Verify sale status is COMPLETED
        assertThat(checkoutResponse.getSale().getStatus()).isEqualTo(SaleStatus.COMPLETED);
    }
}
