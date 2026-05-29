package com.supermarket.sales.controller;

import com.supermarket.sales.dto.external.ProductDTO;
import com.supermarket.sales.service.ProductService;
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
 * REST controller for product search operations.
 * Provides endpoints to search products by name or barcode.
 */
@RestController
@RequestMapping("/api/v1/products")
@Validated
@Tag(name = "Product Search", description = "Endpoints for searching products by name or barcode")
public class ProductSearchController {
    
    private final ProductService productService;
    
    public ProductSearchController(ProductService productService) {
        this.productService = productService;
    }
    
    /**
     * Search products by name (partial match, case-insensitive).
     *
     * @param name the product name to search for
     * @return list of matching products
     */
    @GetMapping("/search/by-name")
    @Operation(
        summary = "Search products by name",
        description = "Searches for products by partial name match (case-insensitive). Returns a list of matching products with their details including stock availability."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Products found (may be empty list if no matches)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ),
        @ApiResponse(
            responseCode = "503",
            description = "Product service unavailable"
        )
    })
    public ResponseEntity<List<ProductDTO>> searchByName(
            @Parameter(description = "Product name to search for", required = true)
            @RequestParam @NotBlank(message = "Product name is required") String name) {
        
        List<ProductDTO> products = productService.searchByName(name);
        return ResponseEntity.ok(products);
    }
    
    /**
     * Search product by barcode (exact match).
     *
     * @param barcode the product barcode
     * @return the matching product
     */
    @GetMapping("/search/by-barcode")
    @Operation(
        summary = "Search product by barcode",
        description = "Searches for a product by exact barcode match. Returns the product details if found."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found"
        ),
        @ApiResponse(
            responseCode = "503",
            description = "Product service unavailable"
        )
    })
    public ResponseEntity<ProductDTO> searchByBarcode(
            @Parameter(description = "Product barcode", required = true)
            @RequestParam @NotBlank(message = "Barcode is required") String barcode) {
        
        ProductDTO product = productService.searchByBarcode(barcode);
        return ResponseEntity.ok(product);
    }
}
