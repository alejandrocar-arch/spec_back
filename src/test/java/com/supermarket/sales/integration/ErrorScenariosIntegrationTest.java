package com.supermarket.sales.integration;

import com.supermarket.sales.dto.request.*;
import com.supermarket.sales.dto.response.SaleDTO;
import com.supermarket.sales.enums.CreditStatus;
import com.supermarket.sales.enums.PaymentType;
import com.supermarket.sales.integration.testdata.CustomerDTOTestBuilder;
import com.supermarket.sales.integration.testdata.ProductDTOTestBuilder;
import com.supermarket.sales.integration.testdata.SaleTestBuilder;
import com.supermarket.sales.integration.testdata.WireMockStubs;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for error scenarios.
 * Tests various error conditions and edge cases.
 */
class ErrorScenariosIntegrationTest extends BaseIntegrationTest {
    
    @Test
    void addItem_withInsufficientStock_returnsConflict() throws Exception {
        // Setup: Product with low stock
        var product = ProductDTOTestBuilder.createProduct(1L, "Low Stock Product", "BAR001", 
                new BigDecimal("10.00"), 2); // Only 2 in stock
        
        WireMockStubs.stubGetProductById(product);
        
        // Create sale
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
        
        // Try to add more than available stock
        AddItemByProductIdRequest addItem = new AddItemByProductIdRequest();
        addItem.setProductId(product.getId());
        addItem.setQuantity(5); // Requesting 5 but only 2 available
        
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Insufficient stock available"));
    }
    
    @Test
    void checkout_withEmptySale_returnsBadRequest() throws Exception {
        // Create sale without items
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
        
        // Try to checkout empty sale
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentType(PaymentType.CASH);
        checkoutRequest.setAmountReceived(new BigDecimal("50.00"));
        
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot checkout empty sale"));
    }
    
    @Test
    void checkout_withInsufficientCash_returnsBadRequest() throws Exception {
        // Setup
        var product = ProductDTOTestBuilder.createProduct1();
        WireMockStubs.stubGetProductById(product);
        
        // Create sale and add item
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
        
        AddItemByProductIdRequest addItem = new AddItemByProductIdRequest();
        addItem.setProductId(product.getId());
        addItem.setQuantity(2);
        
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem)))
                .andExpect(status().isOk());
        
        // Try to checkout with insufficient cash
        // Total should be around 23.80 (20 + 19% tax)
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentType(PaymentType.CASH);
        checkoutRequest.setAmountReceived(new BigDecimal("10.00")); // Not enough
        
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Amount received is insufficient"));
    }
    
    @Test
    void checkout_creditSaleWithoutCustomer_returnsUnprocessableEntity() throws Exception {
        // Setup
        var product = ProductDTOTestBuilder.createProduct1();
        WireMockStubs.stubGetProductById(product);
        
        // Create sale WITHOUT customer
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
        
        AddItemByProductIdRequest addItem = new AddItemByProductIdRequest();
        addItem.setProductId(product.getId());
        addItem.setQuantity(1);
        
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem)))
                .andExpect(status().isOk());
        
        // Try to checkout with credit but no customer
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentType(PaymentType.CREDIT);
        checkoutRequest.setCustomerId(null);
        
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Customer required for credit sales"));
    }
    
    @Test
    void checkout_creditSaleWithRejectedCreditStatus_returnsUnprocessableEntity() throws Exception {
        // Setup: Customer with rejected credit
        var customer = CustomerDTOTestBuilder.createRejectedCustomer();
        var product = ProductDTOTestBuilder.createProduct1();
        
        WireMockStubs.stubGetCustomerById(customer);
        WireMockStubs.stubGetProductById(product);
        
        // Create sale with customer
        CreateSaleRequest createRequest = SaleTestBuilder.createSaleRequestWithCustomer(customer.getId());
        MvcResult createResult = mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        SaleDTO sale = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                SaleDTO.class
        );
        
        AddItemByProductIdRequest addItem = new AddItemByProductIdRequest();
        addItem.setProductId(product.getId());
        addItem.setQuantity(1);
        
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem)))
                .andExpect(status().isOk());
        
        // Try to checkout with credit but customer has rejected status
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentType(PaymentType.CREDIT);
        checkoutRequest.setCustomerId(customer.getId());
        
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Customer credit not approved"));
    }
    
    @Test
    void partialReturn_withExcessiveQuantity_returnsBadRequest() throws Exception {
        // Setup: Complete a sale
        var product = ProductDTOTestBuilder.createProduct1();
        var customer = CustomerDTOTestBuilder.createApprovedCustomer();
        
        WireMockStubs.stubGetProductById(product);
        WireMockStubs.stubGetCustomerById(customer);
        WireMockStubs.stubDecrementStockAny(product.getId());
        
        CreateSaleRequest createRequest = SaleTestBuilder.createSaleRequestWithCustomer(customer.getId());
        MvcResult createResult = mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        SaleDTO sale = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                SaleDTO.class
        );
        
        AddItemByProductIdRequest addItem = new AddItemByProductIdRequest();
        addItem.setProductId(product.getId());
        addItem.setQuantity(2); // Only 2 items
        
        MvcResult addItemResult = mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem)))
                .andExpect(status().isOk())
                .andReturn();
        
        SaleDTO saleWithItems = objectMapper.readValue(
                addItemResult.getResponse().getContentAsString(),
                SaleDTO.class
        );
        Long itemId = saleWithItems.getItems().get(0).getId();
        
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentType(PaymentType.CREDIT);
        checkoutRequest.setCustomerId(customer.getId());
        
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isOk());
        
        // Try to return more than purchased
        ReturnItemRequest returnItem = new ReturnItemRequest();
        returnItem.setItemId(itemId);
        returnItem.setQuantity(5); // Trying to return 5 but only bought 2
        
        PartialReturnRequest returnRequest = new PartialReturnRequest();
        returnRequest.setReturnReason("Return too many");
        returnRequest.setReturnItems(Arrays.asList(returnItem));
        
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/return/partial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(returnRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Return quantity exceeds purchased quantity"));
    }
    
    @Test
    void cancelCompletedSale_returnsConflict() throws Exception {
        // Setup: Complete a sale
        var product = ProductDTOTestBuilder.createProduct1();
        WireMockStubs.stubGetProductById(product);
        WireMockStubs.stubDecrementStockAny(product.getId());
        
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
        
        AddItemByProductIdRequest addItem = new AddItemByProductIdRequest();
        addItem.setProductId(product.getId());
        addItem.setQuantity(1);
        
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem)))
                .andExpect(status().isOk());
        
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentType(PaymentType.CASH);
        checkoutRequest.setAmountReceived(new BigDecimal("50.00"));
        
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isOk());
        
        // Try to cancel completed sale
        CancelSaleRequest cancelRequest = new CancelSaleRequest();
        cancelRequest.setCancellationReason("Trying to cancel completed sale");
        
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot cancel completed or returned sale"));
    }
    
    @Test
    void resumeNonFrozenSale_returnsConflict() throws Exception {
        // Create active sale
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
        
        // Try to resume active (not frozen) sale
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/resume")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Only frozen sales can be resumed"));
    }
    
    @Test
    void freezeNonActiveSale_returnsConflict() throws Exception {
        // Create and freeze sale
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
        
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/freeze")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        // Try to freeze already frozen sale
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/freeze")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Only active sales can be frozen"));
    }
    
    @Test
    void productApiUnavailable_returnsServiceUnavailable() throws Exception {
        // Stub Product API to return 503
        WireMockStubs.stubProductServiceUnavailable(999L);
        
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
        
        // Try to add item when Product API is unavailable
        AddItemByProductIdRequest addItem = new AddItemByProductIdRequest();
        addItem.setProductId(999L);
        addItem.setQuantity(1);
        
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Product service unavailable"));
    }
    
    @Test
    void customerApiUnavailable_returnsServiceUnavailable() throws Exception {
        // Stub Customer API to return 503
        WireMockStubs.stubCustomerServiceUnavailable(999L);
        
        var product = ProductDTOTestBuilder.createProduct1();
        WireMockStubs.stubGetProductById(product);
        
        // Create sale with customer
        CreateSaleRequest createRequest = SaleTestBuilder.createSaleRequestWithCustomer(999L);
        MvcResult createResult = mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        SaleDTO sale = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                SaleDTO.class
        );
        
        AddItemByProductIdRequest addItem = new AddItemByProductIdRequest();
        addItem.setProductId(product.getId());
        addItem.setQuantity(1);
        
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem)))
                .andExpect(status().isOk());
        
        // Try to checkout with credit when Customer API is unavailable
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentType(PaymentType.CREDIT);
        checkoutRequest.setCustomerId(999L);
        
        mockMvc.perform(post("/api/v1/sales/" + sale.getId() + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Customer service unavailable"));
    }
}
