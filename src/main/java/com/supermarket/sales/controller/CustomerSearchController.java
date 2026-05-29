package com.supermarket.sales.controller;

import com.supermarket.sales.dto.external.CustomerDTO;
import com.supermarket.sales.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for customer search operations.
 * Provides endpoints to search customers by name or document number.
 */
@RestController
@RequestMapping("/api/v1/customers")
@Validated
@Tag(name = "Customer Search", description = "Endpoints for searching customers by name or document number")
public class CustomerSearchController {
    
    private final CustomerService customerService;
    
    public CustomerSearchController(CustomerService customerService) {
        this.customerService = customerService;
    }
    
    /**
     * Search customers by name (partial match, case-insensitive).
     *
     * @param name the customer name to search for
     * @return list of matching customers
     */
    @GetMapping("/search/by-name")
    @Operation(
        summary = "Search customers by name",
        description = "Searches for customers by partial name match (case-insensitive). Returns a list of matching customers with their details including credit status."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Customers found (may be empty list if no matches)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ),
        @ApiResponse(
            responseCode = "503",
            description = "Customer service unavailable"
        )
    })
    public ResponseEntity<List<CustomerDTO>> searchByName(
            @Parameter(description = "Customer name to search for", required = true)
            @RequestParam @NotBlank(message = "Customer name is required") String name) {
        
        List<CustomerDTO> customers = customerService.searchByName(name);
        return ResponseEntity.ok(customers);
    }
    
    /**
     * Search customer by document number (exact match).
     *
     * @param documentNumber the customer document number
     * @return the matching customer
     */
    @GetMapping("/search/by-document")
    @Operation(
        summary = "Search customer by document number",
        description = "Searches for a customer by exact document number match. Returns the customer details if found."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Customer found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Customer not found"
        ),
        @ApiResponse(
            responseCode = "503",
            description = "Customer service unavailable"
        )
    })
    public ResponseEntity<CustomerDTO> searchByDocument(
            @Parameter(description = "Customer document number", required = true)
            @RequestParam @NotBlank(message = "Document number is required") String documentNumber) {
        
        CustomerDTO customer = customerService.searchByDocument(documentNumber);
        return ResponseEntity.ok(customer);
    }
}
