package com.supermarket.sales.integration.testdata;

import com.supermarket.sales.dto.request.CreateSaleRequest;

/**
 * Test data builder for Sale-related test objects.
 */
public class SaleTestBuilder {
    
    public static CreateSaleRequest createSaleRequest() {
        return createSaleRequest("TERM-001", "CASH-001", null);
    }
    
    public static CreateSaleRequest createSaleRequest(String terminalId, String cashierId, Long customerId) {
        CreateSaleRequest request = new CreateSaleRequest();
        request.setTerminalId(terminalId);
        request.setCashierId(cashierId);
        request.setCustomerId(customerId);
        return request;
    }
    
    public static CreateSaleRequest createSaleRequestWithCustomer(Long customerId) {
        return createSaleRequest("TERM-001", "CASH-001", customerId);
    }
}
