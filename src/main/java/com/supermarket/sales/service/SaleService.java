package com.supermarket.sales.service;

import com.supermarket.sales.dto.request.*;
import com.supermarket.sales.dto.response.*;
import com.supermarket.sales.enums.SaleStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for managing sales lifecycle operations.
 * Handles sale creation, item management, discounts, checkout, freezing, cancellation, and returns.
 */
public interface SaleService {
    
    // Sale Creation and Retrieval
    
    /**
     * Creates a new sale with ACTIVE status.
     *
     * @param request the sale creation request containing terminal ID, cashier ID, and optional customer ID
     * @return the created sale DTO
     */
    SaleDTO createSale(CreateSaleRequest request);
    
    /**
     * Retrieves a sale by its ID.
     *
     * @param saleId the sale ID
     * @return the sale DTO
     * @throws com.supermarket.sales.exception.SaleNotFoundException if sale not found
     */
    SaleDTO getSaleById(Long saleId);
    
    /**
     * Retrieves all sales for a specific terminal with optional filters.
     *
     * @param terminalId the terminal ID
     * @param status optional status filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @return list of sale DTOs
     */
    List<SaleDTO> getSalesByTerminal(String terminalId, SaleStatus status, LocalDate startDate, LocalDate endDate);
    
    // Item Management
    
    /**
     * Adds an item to a sale by product ID.
     * If the product already exists in the sale, increments the quantity.
     *
     * @param saleId the sale ID
     * @param productId the product ID
     * @param quantity the quantity to add
     * @return the updated sale DTO
     * @throws com.supermarket.sales.exception.SaleNotFoundException if sale not found
     * @throws com.supermarket.sales.exception.ProductNotFoundException if product not found
     * @throws com.supermarket.sales.exception.SaleNotActiveException if sale is not active
     * @throws com.supermarket.sales.exception.InsufficientStockException if insufficient stock
     */
    SaleDTO addItemByProductId(Long saleId, Long productId, Integer quantity);
    
    /**
     * Adds an item to a sale by barcode.
     * If the product already exists in the sale, increments the quantity.
     *
     * @param saleId the sale ID
     * @param barcode the product barcode
     * @param quantity the quantity to add
     * @return the updated sale DTO
     * @throws com.supermarket.sales.exception.SaleNotFoundException if sale not found
     * @throws com.supermarket.sales.exception.ProductNotFoundException if product not found
     * @throws com.supermarket.sales.exception.SaleNotActiveException if sale is not active
     * @throws com.supermarket.sales.exception.InsufficientStockException if insufficient stock
     */
    SaleDTO addItemByBarcode(Long saleId, String barcode, Integer quantity);
    
    /**
     * Updates the quantity of an item in a sale.
     *
     * @param saleId the sale ID
     * @param itemId the sale item ID
     * @param quantity the new quantity
     * @return the updated sale DTO
     * @throws com.supermarket.sales.exception.SaleNotFoundException if sale not found
     * @throws com.supermarket.sales.exception.SaleItemNotFoundException if item not found
     * @throws com.supermarket.sales.exception.SaleNotActiveException if sale is not active
     * @throws com.supermarket.sales.exception.InsufficientStockException if insufficient stock
     */
    SaleDTO updateItemQuantity(Long saleId, Long itemId, Integer quantity);
    
    /**
     * Removes an item from a sale.
     *
     * @param saleId the sale ID
     * @param itemId the sale item ID
     * @return the updated sale DTO
     * @throws com.supermarket.sales.exception.SaleNotFoundException if sale not found
     * @throws com.supermarket.sales.exception.SaleItemNotFoundException if item not found
     * @throws com.supermarket.sales.exception.SaleNotActiveException if sale is not active
     */
    SaleDTO removeItem(Long saleId, Long itemId);
    
    // Discount and Checkout
    
    /**
     * Applies a discount to a sale.
     *
     * @param saleId the sale ID
     * @param request the discount request containing type and value
     * @return the updated sale DTO
     * @throws com.supermarket.sales.exception.SaleNotFoundException if sale not found
     * @throws com.supermarket.sales.exception.SaleNotActiveException if sale is not active
     * @throws com.supermarket.sales.exception.InvalidDiscountException if discount is invalid
     */
    SaleDTO applyDiscount(Long saleId, ApplyDiscountRequest request);
    
    /**
     * Processes checkout for a sale with cash or credit payment.
     *
     * @param saleId the sale ID
     * @param request the checkout request containing payment type and details
     * @return the checkout response with sale and receipt
     * @throws com.supermarket.sales.exception.SaleNotFoundException if sale not found
     * @throws com.supermarket.sales.exception.SaleNotActiveException if sale is not active
     * @throws com.supermarket.sales.exception.InvalidPaymentException if payment details are invalid
     * @throws com.supermarket.sales.exception.CustomerRequiredException if customer required for credit
     * @throws com.supermarket.sales.exception.CreditNotApprovedException if credit not approved
     * @throws com.supermarket.sales.exception.InsufficientStockException if insufficient stock
     */
    CheckoutResponse checkout(Long saleId, CheckoutRequest request);
    
    // Freeze, Resume, Cancel
    
    /**
     * Freezes an active sale.
     *
     * @param saleId the sale ID
     * @return the updated sale DTO
     * @throws com.supermarket.sales.exception.SaleNotFoundException if sale not found
     * @throws com.supermarket.sales.exception.SaleNotActiveException if sale is not active
     */
    SaleDTO freezeSale(Long saleId);
    
    /**
     * Resumes a frozen sale.
     *
     * @param saleId the sale ID
     * @return the updated sale DTO
     * @throws com.supermarket.sales.exception.SaleNotFoundException if sale not found
     * @throws com.supermarket.sales.exception.SaleNotFrozenException if sale is not frozen
     */
    SaleDTO resumeSale(Long saleId);
    
    /**
     * Retrieves all frozen sales for a specific terminal.
     *
     * @param terminalId the terminal ID
     * @return list of frozen sale DTOs
     */
    List<FrozenSaleDTO> getFrozenSalesByTerminal(String terminalId);
    
    /**
     * Cancels a sale with a reason.
     *
     * @param saleId the sale ID
     * @param reason the cancellation reason
     * @return the updated sale DTO
     * @throws com.supermarket.sales.exception.SaleNotFoundException if sale not found
     * @throws com.supermarket.sales.exception.SaleAlreadyCancelledException if already cancelled
     */
    SaleDTO cancelSale(Long saleId, String reason);
    
    // Returns
    
    /**
     * Processes a full return of all items in a completed sale.
     *
     * @param saleId the sale ID
     * @param request the full return request containing return reason
     * @return the return response with sale, return receipt, and credit note if applicable
     * @throws com.supermarket.sales.exception.SaleNotFoundException if sale not found
     * @throws com.supermarket.sales.exception.SaleAlreadyReturnedException if already returned
     */
    ReturnResponse processFullReturn(Long saleId, FullReturnRequest request);
    
    /**
     * Processes a partial return of specific items in a completed sale.
     *
     * @param saleId the sale ID
     * @param request the partial return request containing return reason and items
     * @return the return response with sale, return receipt, and credit note if applicable
     * @throws com.supermarket.sales.exception.SaleNotFoundException if sale not found
     * @throws com.supermarket.sales.exception.InvalidReturnQuantityException if return quantity invalid
     */
    ReturnResponse processPartialReturn(Long saleId, PartialReturnRequest request);
}
