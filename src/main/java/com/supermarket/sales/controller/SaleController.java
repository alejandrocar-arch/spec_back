package com.supermarket.sales.controller;

import com.supermarket.sales.dto.request.*;
import com.supermarket.sales.dto.response.*;
import com.supermarket.sales.enums.SaleStatus;
import com.supermarket.sales.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for sale management operations.
 * Handles sale creation, item management, checkout, freezing, cancellation, and returns.
 */
@RestController
@RequestMapping("/api/v1/sales")
@Tag(name = "Sales", description = "Endpoints for managing sales lifecycle")
public class SaleController {
    
    private final SaleService saleService;
    
    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }
    
    // ========== Sale Management Endpoints ==========
    
    /**
     * Create a new sale.
     *
     * @param request the sale creation request
     * @return the created sale
     */
    @PostMapping
    @Operation(
        summary = "Create a new sale",
        description = "Creates a new sale with ACTIVE status for the specified terminal and cashier. Optionally associates a customer with the sale."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Sale created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SaleDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
        )
    })
    public ResponseEntity<SaleDTO> createSale(
            @Parameter(description = "Sale creation request", required = true)
            @Valid @RequestBody CreateSaleRequest request) {
        
        SaleDTO sale = saleService.createSale(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(sale);
    }
    
    /**
     * Get sale by ID.
     *
     * @param saleId the sale ID
     * @return the sale details
     */
    @GetMapping("/{saleId}")
    @Operation(
        summary = "Get sale by ID",
        description = "Retrieves complete details of a sale including all items, totals, and status."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sale found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SaleDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sale not found"
        )
    })
    public ResponseEntity<SaleDTO> getSaleById(
            @Parameter(description = "Sale ID", required = true)
            @PathVariable Long saleId) {
        
        SaleDTO sale = saleService.getSaleById(saleId);
        return ResponseEntity.ok(sale);
    }
    
    /**
     * Get all sales for a terminal with optional filters.
     *
     * @param terminalId the terminal ID
     * @param status optional status filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @return list of sales
     */
    @GetMapping("/terminal/{terminalId}")
    @Operation(
        summary = "Get sales by terminal",
        description = "Retrieves all sales for a specific terminal. Supports optional filtering by status and date range."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sales retrieved successfully (may be empty list)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SaleDTO.class))
        )
    })
    public ResponseEntity<List<SaleDTO>> getSalesByTerminal(
            @Parameter(description = "Terminal ID", required = true)
            @PathVariable String terminalId,
            @Parameter(description = "Optional status filter")
            @RequestParam(required = false) SaleStatus status,
            @Parameter(description = "Optional start date filter (format: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Optional end date filter (format: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<SaleDTO> sales = saleService.getSalesByTerminal(terminalId, status, startDate, endDate);
        return ResponseEntity.ok(sales);
    }
    
    /**
     * Get frozen sales for a terminal.
     *
     * @param terminalId the terminal ID
     * @return list of frozen sales
     */
    @GetMapping("/terminal/{terminalId}/frozen")
    @Operation(
        summary = "Get frozen sales by terminal",
        description = "Retrieves all frozen sales for a specific terminal, ordered by freeze timestamp descending."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Frozen sales retrieved successfully (may be empty list)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = FrozenSaleDTO.class))
        )
    })
    public ResponseEntity<List<FrozenSaleDTO>> getFrozenSalesByTerminal(
            @Parameter(description = "Terminal ID", required = true)
            @PathVariable String terminalId) {
        
        List<FrozenSaleDTO> frozenSales = saleService.getFrozenSalesByTerminal(terminalId);
        return ResponseEntity.ok(frozenSales);
    }
    
    // ========== Item Management Endpoints ==========
    
    /**
     * Add item to sale by product ID.
     *
     * @param saleId the sale ID
     * @param request the add item request
     * @return the updated sale
     */
    @PostMapping("/{saleId}/items/by-product-id")
    @Operation(
        summary = "Add item to sale by product ID",
        description = "Adds an item to an active sale by product ID. If the product already exists in the sale, increments the quantity."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Item added successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SaleDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sale or product not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Sale not active or insufficient stock"
        )
    })
    public ResponseEntity<SaleDTO> addItemByProductId(
            @Parameter(description = "Sale ID", required = true)
            @PathVariable Long saleId,
            @Parameter(description = "Add item request", required = true)
            @Valid @RequestBody AddItemByProductIdRequest request) {
        
        SaleDTO sale = saleService.addItemByProductId(saleId, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(sale);
    }
    
    /**
     * Add item to sale by barcode.
     *
     * @param saleId the sale ID
     * @param request the add item request
     * @return the updated sale
     */
    @PostMapping("/{saleId}/items/by-barcode")
    @Operation(
        summary = "Add item to sale by barcode",
        description = "Adds an item to an active sale by scanning barcode. If the product already exists in the sale, increments the quantity."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Item added successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SaleDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sale or product not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Sale not active or insufficient stock"
        )
    })
    public ResponseEntity<SaleDTO> addItemByBarcode(
            @Parameter(description = "Sale ID", required = true)
            @PathVariable Long saleId,
            @Parameter(description = "Add item request", required = true)
            @Valid @RequestBody AddItemByBarcodeRequest request) {
        
        SaleDTO sale = saleService.addItemByBarcode(saleId, request.getBarcode(), request.getQuantity());
        return ResponseEntity.ok(sale);
    }
    
    /**
     * Update item quantity in sale.
     *
     * @param saleId the sale ID
     * @param itemId the sale item ID
     * @param request the update quantity request
     * @return the updated sale
     */
    @PutMapping("/{saleId}/items/{itemId}/quantity")
    @Operation(
        summary = "Update item quantity",
        description = "Updates the quantity of an item in an active sale. Validates stock availability for the new quantity."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Quantity updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SaleDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sale or item not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Sale not active or insufficient stock"
        )
    })
    public ResponseEntity<SaleDTO> updateItemQuantity(
            @Parameter(description = "Sale ID", required = true)
            @PathVariable Long saleId,
            @Parameter(description = "Sale item ID", required = true)
            @PathVariable Long itemId,
            @Parameter(description = "Update quantity request", required = true)
            @Valid @RequestBody UpdateQuantityRequest request) {
        
        SaleDTO sale = saleService.updateItemQuantity(saleId, itemId, request.getQuantity());
        return ResponseEntity.ok(sale);
    }
    
    /**
     * Remove item from sale.
     *
     * @param saleId the sale ID
     * @param itemId the sale item ID
     * @return the updated sale
     */
    @DeleteMapping("/{saleId}/items/{itemId}")
    @Operation(
        summary = "Remove item from sale",
        description = "Removes an item from an active sale and recalculates totals."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Item removed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SaleDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sale or item not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Sale not active"
        )
    })
    public ResponseEntity<SaleDTO> removeItem(
            @Parameter(description = "Sale ID", required = true)
            @PathVariable Long saleId,
            @Parameter(description = "Sale item ID", required = true)
            @PathVariable Long itemId) {
        
        SaleDTO sale = saleService.removeItem(saleId, itemId);
        return ResponseEntity.ok(sale);
    }
    
    // ========== Sale Operations Endpoints ==========
    
    /**
     * Apply discount to sale.
     *
     * @param saleId the sale ID
     * @param request the discount request
     * @return the updated sale
     */
    @PostMapping("/{saleId}/discount")
    @Operation(
        summary = "Apply discount to sale",
        description = "Applies a percentage or fixed amount discount to an active sale and recalculates totals."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Discount applied successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SaleDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid discount value"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sale not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Sale not active"
        )
    })
    public ResponseEntity<SaleDTO> applyDiscount(
            @Parameter(description = "Sale ID", required = true)
            @PathVariable Long saleId,
            @Parameter(description = "Discount request", required = true)
            @Valid @RequestBody ApplyDiscountRequest request) {
        
        SaleDTO sale = saleService.applyDiscount(saleId, request);
        return ResponseEntity.ok(sale);
    }
    
    /**
     * Checkout sale with cash or credit payment.
     *
     * @param saleId the sale ID
     * @param request the checkout request
     * @return the checkout response with sale and receipt
     */
    @PostMapping("/{saleId}/checkout")
    @Operation(
        summary = "Checkout sale",
        description = "Processes checkout for a sale with cash or credit payment. Validates payment details, decrements stock, generates transaction ID and receipt."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Checkout completed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CheckoutResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid payment details or empty sale"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sale not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Sale not active or insufficient stock"
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Customer required for credit or credit not approved"
        )
    })
    public ResponseEntity<CheckoutResponse> checkout(
            @Parameter(description = "Sale ID", required = true)
            @PathVariable Long saleId,
            @Parameter(description = "Checkout request", required = true)
            @Valid @RequestBody CheckoutRequest request) {
        
        CheckoutResponse response = saleService.checkout(saleId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Freeze an active sale.
     *
     * @param saleId the sale ID
     * @return the updated sale
     */
    @PostMapping("/{saleId}/freeze")
    @Operation(
        summary = "Freeze sale",
        description = "Freezes an active sale to temporarily pause it. The sale can be resumed later."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sale frozen successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SaleDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sale not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Only active sales can be frozen"
        )
    })
    public ResponseEntity<SaleDTO> freezeSale(
            @Parameter(description = "Sale ID", required = true)
            @PathVariable Long saleId) {
        
        SaleDTO sale = saleService.freezeSale(saleId);
        return ResponseEntity.ok(sale);
    }
    
    /**
     * Resume a frozen sale.
     *
     * @param saleId the sale ID
     * @return the updated sale
     */
    @PostMapping("/{saleId}/resume")
    @Operation(
        summary = "Resume frozen sale",
        description = "Resumes a frozen sale, changing its status back to ACTIVE."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sale resumed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SaleDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sale not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Only frozen sales can be resumed"
        )
    })
    public ResponseEntity<SaleDTO> resumeSale(
            @Parameter(description = "Sale ID", required = true)
            @PathVariable Long saleId) {
        
        SaleDTO sale = saleService.resumeSale(saleId);
        return ResponseEntity.ok(sale);
    }
    
    /**
     * Cancel a sale.
     *
     * @param saleId the sale ID
     * @param request the cancellation request
     * @return the updated sale
     */
    @PostMapping("/{saleId}/cancel")
    @Operation(
        summary = "Cancel sale",
        description = "Cancels an active or frozen sale with a reason. Completed or returned sales cannot be cancelled."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sale cancelled successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SaleDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid cancellation reason"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sale not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Cannot cancel completed or returned sale"
        )
    })
    public ResponseEntity<SaleDTO> cancelSale(
            @Parameter(description = "Sale ID", required = true)
            @PathVariable Long saleId,
            @Parameter(description = "Cancellation request", required = true)
            @Valid @RequestBody CancelSaleRequest request) {
        
        SaleDTO sale = saleService.cancelSale(saleId, request.getCancellationReason());
        return ResponseEntity.ok(sale);
    }
    
    // ========== Return Endpoints ==========
    
    /**
     * Process full return of all items.
     *
     * @param saleId the sale ID
     * @param request the full return request
     * @return the return response with sale, return receipt, and credit note if applicable
     */
    @PostMapping("/{saleId}/return/full")
    @Operation(
        summary = "Process full return",
        description = "Processes a full return of all items in a completed sale. Increments stock for all items and generates return receipt. For credit sales, also generates a credit note."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Full return processed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReturnResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid return reason"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sale not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Only completed sales can be returned or sale already returned"
        )
    })
    public ResponseEntity<ReturnResponse> processFullReturn(
            @Parameter(description = "Sale ID", required = true)
            @PathVariable Long saleId,
            @Parameter(description = "Full return request", required = true)
            @Valid @RequestBody FullReturnRequest request) {
        
        ReturnResponse response = saleService.processFullReturn(saleId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Process partial return of specific items.
     *
     * @param saleId the sale ID
     * @param request the partial return request
     * @return the return response with sale, return receipt, and credit note if applicable
     */
    @PostMapping("/{saleId}/return/partial")
    @Operation(
        summary = "Process partial return",
        description = "Processes a partial return of specific items in a completed sale. Increments stock only for returned items and generates return receipt. For credit sales, also generates a credit note."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Partial return processed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReturnResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid return data or return quantity exceeds purchased quantity"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sale not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Only completed sales can be returned or sale already fully returned"
        )
    })
    public ResponseEntity<ReturnResponse> processPartialReturn(
            @Parameter(description = "Sale ID", required = true)
            @PathVariable Long saleId,
            @Parameter(description = "Partial return request", required = true)
            @Valid @RequestBody PartialReturnRequest request) {
        
        ReturnResponse response = saleService.processPartialReturn(saleId, request);
        return ResponseEntity.ok(response);
    }
}
