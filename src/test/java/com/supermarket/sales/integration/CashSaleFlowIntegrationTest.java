package com.supermarket.sales.integration;

import com.supermarket.sales.dto.request.*;
import com.supermarket.sales.dto.response.CheckoutResponse;
import com.supermarket.sales.dto.response.SaleDTO;
import com.supermarket.sales.enums.DiscountType;
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
 * Integration test for complete cash sale flow.
 * Tests: Create sale -> Add items -> Apply discount -> Checkout with cash
 */
class CashSaleFlowIntegrationTest extends BaseIntegrationTest {
    
    @Test
    void completeCashSaleFlow_withMultipleItems_andDiscount_success() throws Exception {
        // Setup test data
        var product1 = ProductDTOTestBuilder.createProduct1(); // $10.00
        var product2 = ProductDTOTestBuilder.createProduct2(); // $20.00
        var product3 = ProductDTOTestBuilder.createProduct3(); // $15.50
        
        // Stub Product API responses
        WireMockStubs.stubGetProductById(product1);
        WireMockStubs.stubGetProductById(product2);
        WireMockStubs.stubGetProductByBarcode(product3);
        WireMockStubs.stubDecrementStockAny(product1.getId());
        WireMockStubs.stubDecrementStockAny(product2.getId());
        WireMockStubs.stubDecrementStockAny(product3.getId());
        
        // Step 1: Create sale
        CreateSaleRequest createRequest = SaleTestBuilder.createSaleRequest();
        MvcResult createResult = mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.terminalId").value("TERM-001"))
                .andExpect(jsonPath("$.cashierId").value("CASH-001"))
                .andReturn();
        
        SaleDTO sale = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), 
                SaleDTO.class
        );
        Long saleId = sale.getId();
        
        // Step 2: Add first item by product ID (2 units of Product 1)
        AddItemByProductIdRequest addItem1 = new AddItemByProductIdRequest();
        addItem1.setProductId(product1.getId());
        addItem1.setQuantity(2);
        
        mockMvc.perform(post("/api/v1/sales/" + saleId + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productId").value(product1.getId()))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].lineTotal").value(20.00));
        
        // Step 3: Add second item by product ID (3 units of Product 2)
        AddItemByProductIdRequest addItem2 = new AddItemByProductIdRequest();
        addItem2.setProductId(product2.getId());
        addItem2.setQuantity(3);
        
        mockMvc.perform(post("/api/v1/sales/" + saleId + "/items/by-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2));
        
        // Step 4: Add third item by barcode (1 unit of Product 3)
        AddItemByBarcodeRequest addItem3 = new AddItemByBarcodeRequest();
        addItem3.setBarcode(product3.getBarcode());
        addItem3.setQuantity(1);
        
        mockMvc.perform(post("/api/v1/sales/" + saleId + "/items/by-barcode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addItem3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(3));
        
        // Step 5: Apply 10% discount
        ApplyDiscountRequest discountRequest = new ApplyDiscountRequest();
        discountRequest.setDiscountType(DiscountType.PERCENTAGE);
        discountRequest.setDiscountValue(new BigDecimal("10.00"));
        
        mockMvc.perform(post("/api/v1/sales/" + saleId + "/discount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discountType").value("PERCENTAGE"))
                .andExpect(jsonPath("$.discountValue").value(10.00));
        
        // Step 6: Checkout with cash
        // Subtotal: (2*10) + (3*20) + (1*15.50) = 20 + 60 + 15.50 = 95.50
        // Tax (19%): 95.50 * 0.19 = 18.145 = 18.15
        // Subtotal + Tax: 95.50 + 18.15 = 113.65
        // Discount (10%): 113.65 * 0.10 = 11.365 = 11.37
        // Total: 113.65 - 11.37 = 102.28
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setPaymentType(PaymentType.CASH);
        checkoutRequest.setAmountReceived(new BigDecimal("150.00"));
        
        MvcResult checkoutResult = mockMvc.perform(post("/api/v1/sales/" + saleId + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sale.status").value("COMPLETED"))
                .andExpect(jsonPath("$.sale.paymentType").value("CASH"))
                .andExpect(jsonPath("$.sale.transactionId").exists())
                .andExpect(jsonPath("$.receipt").exists())
                .andExpect(jsonPath("$.receipt.transactionId").exists())
                .andExpect(jsonPath("$.receipt.paymentType").value("CASH"))
                .andExpect(jsonPath("$.receipt.amountReceived").value(150.00))
                .andExpect(jsonPath("$.receipt.items.length()").value(3))
                .andReturn();
        
        CheckoutResponse checkoutResponse = objectMapper.readValue(
                checkoutResult.getResponse().getContentAsString(),
                CheckoutResponse.class
        );
        
        // Verify receipt details
        assertThat(checkoutResponse.getReceipt()).isNotNull();
        assertThat(checkoutResponse.getReceipt().getStoreName()).isEqualTo("Test Store");
        assertThat(checkoutResponse.getReceipt().getTerminalId()).isEqualTo("TERM-001");
        assertThat(checkoutResponse.getReceipt().getCashierId()).isEqualTo("CASH-001");
        assertThat(checkoutResponse.getReceipt().getTransactionId()).isNotNull();
        assertThat(checkoutResponse.getReceipt().getItems()).hasSize(3);
        assertThat(checkoutResponse.getReceipt().getSubtotal()).isEqualByComparingTo("95.50");
        assertThat(checkoutResponse.getReceipt().getTax()).isEqualByComparingTo("18.15");
        assertThat(checkoutResponse.getReceipt().getTotal()).isEqualByComparingTo("102.28");
        assertThat(checkoutResponse.getReceipt().getAmountReceived()).isEqualByComparingTo("150.00");
        assertThat(checkoutResponse.getReceipt().getChange()).isEqualByComparingTo("47.72");
        
        // Verify WireMock calls for stock decrement
        verify(postRequestedFor(urlEqualTo("/api/v1/products/" + product1.getId() + "/decrement-stock")));
        verify(postRequestedFor(urlEqualTo("/api/v1/products/" + product2.getId() + "/decrement-stock")));
        verify(postRequestedFor(urlEqualTo("/api/v1/products/" + product3.getId() + "/decrement-stock")));
        
        // Verify sale status is COMPLETED
        assertThat(checkoutResponse.getSale().getStatus()).isEqualTo(SaleStatus.COMPLETED);
    }
}
