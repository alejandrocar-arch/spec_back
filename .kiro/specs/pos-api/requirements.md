# Requirements Document: Sales API for Supermarket POS System

## Introduction

The Sales API is a REST API that manages sales transactions for a supermarket Point of Sale (POS) system. This API handles the complete sales lifecycle including product and customer search, sale creation, item management, multiple payment types, checkout processing, sale freezing, cancellations, and returns (both full and partial). The Sales API integrates with two existing external services: the Product API for product catalog and stock management, and the Customer API for customer information and credit validation.

## Glossary

- **Sales_API**: The REST API system being specified that manages sales transactions
- **Product_API**: External REST API that manages product catalog, stock levels, and pricing
- **Customer_API**: External REST API that manages customer records and credit status
- **POS_Terminal**: Physical point of sale terminal identified by a unique terminal ID
- **Cashier**: User operating the POS terminal, identified by a cashier ID
- **Sale**: A transaction record containing items, payment information, and status
- **Sale_Item**: A line item in a sale containing product reference, quantity, and price snapshot
- **Payment_Type**: Enumeration of payment methods: CASH or CREDIT
- **Sale_Status**: Enumeration of sale states: ACTIVE, FROZEN, COMPLETED, CANCELLED, RETURNED, PARTIALLY_RETURNED
- **Credit_Status**: Enumeration of customer credit approval states: APPROVED, REJECTED, PENDING
- **Receipt**: Document generated after successful checkout containing transaction details
- **Transaction_ID**: Unique identifier assigned to a completed sale
- **Stock**: Available quantity of a product in inventory
- **Subtotal**: Sum of all line item totals before tax and discount
- **Tax**: Calculated tax amount based on configurable tax rate
- **Discount**: Optional reduction in price, either percentage or fixed amount
- **Total**: Final amount to be paid after tax and discount calculations
- **Change**: Amount returned to customer when cash received exceeds total
- **Credit_Reference_Number**: Unique identifier for credit sale transactions
- **Return_Reason**: Text explanation for why items are being returned
- **Cancellation_Reason**: Text explanation for why a sale is being cancelled
- **Barcode**: Product identifier encoded in scannable format
- **BigDecimal**: Java decimal number type with arbitrary precision
- **Frozen_Sale**: Sale temporarily paused to allow processing of another transaction
- **Return_Receipt**: Document generated after return processing referencing original transaction

## Requirements

### Requirement 1: Product Search by Name

**User Story:** As a cashier, I want to search for products by name, so that I can add items to a sale when the barcode is not available or readable.

#### Acceptance Criteria

1. WHEN a product name search request is received, THE Sales_API SHALL call the Product_API with the search term
2. THE Sales_API SHALL perform case-insensitive partial matching on product names
3. WHEN the Product_API returns matching products, THE Sales_API SHALL return a list containing product ID, name, barcode, unit price, available stock, and category for each product
4. WHEN the Product_API returns no matches, THE Sales_API SHALL return an empty list with HTTP status 200
5. IF the Product_API is unavailable or returns an error, THEN THE Sales_API SHALL return HTTP status 503 with message "Product service unavailable"

### Requirement 2: Product Search by Barcode

**User Story:** As a cashier, I want to search for products by barcode, so that I can quickly add scanned items to a sale.

#### Acceptance Criteria

1. WHEN a barcode search request is received, THE Sales_API SHALL call the Product_API with the barcode value
2. THE Sales_API SHALL perform exact matching on barcode values
3. WHEN the Product_API returns a matching product, THE Sales_API SHALL return product ID, name, barcode, unit price, available stock, and category
4. WHEN the Product_API returns no match, THE Sales_API SHALL return HTTP status 404 with message "Product not found"
5. IF the Product_API is unavailable or returns an error, THEN THE Sales_API SHALL return HTTP status 503 with message "Product service unavailable"

### Requirement 3: Customer Search by Name

**User Story:** As a cashier, I want to search for customers by name, so that I can associate a customer with a sale for credit purchases or loyalty tracking.

#### Acceptance Criteria

1. WHEN a customer name search request is received, THE Sales_API SHALL call the Customer_API with the search term
2. THE Sales_API SHALL perform case-insensitive partial matching on customer names
3. WHEN the Customer_API returns matching customers, THE Sales_API SHALL return a list containing customer ID, full name, document type, document number, and credit status for each customer
4. WHEN the Customer_API returns no matches, THE Sales_API SHALL return an empty list with HTTP status 200
5. IF the Customer_API is unavailable or returns an error, THEN THE Sales_API SHALL return HTTP status 503 with message "Customer service unavailable"

### Requirement 4: Customer Search by Document Number

**User Story:** As a cashier, I want to search for customers by document number, so that I can quickly locate a specific customer for credit sales.

#### Acceptance Criteria

1. WHEN a document number search request is received, THE Sales_API SHALL call the Customer_API with the document number
2. THE Sales_API SHALL perform exact matching on document numbers
3. WHEN the Customer_API returns a matching customer, THE Sales_API SHALL return customer ID, full name, document type, document number, and credit status
4. WHEN the Customer_API returns no match, THE Sales_API SHALL return HTTP status 404 with message "Customer not found"
5. IF the Customer_API is unavailable or returns an error, THEN THE Sales_API SHALL return HTTP status 503 with message "Customer service unavailable"

### Requirement 5: Sale Creation

**User Story:** As a cashier, I want to create a new sale, so that I can begin adding items for a customer transaction.

#### Acceptance Criteria

1. WHEN a sale creation request is received, THE Sales_API SHALL create a new sale with status ACTIVE
2. THE Sales_API SHALL associate the sale with the provided POS terminal ID
3. THE Sales_API SHALL record the cashier ID from the request
4. WHERE a customer ID is provided, THE Sales_API SHALL associate the customer with the sale
5. THE Sales_API SHALL initialize the sale with zero items and zero totals
6. THE Sales_API SHALL return the created sale with a unique sale ID and HTTP status 201
7. IF the terminal ID is missing or invalid, THEN THE Sales_API SHALL return HTTP status 400 with message "Invalid terminal ID"
8. IF the cashier ID is missing, THEN THE Sales_API SHALL return HTTP status 400 with message "Cashier ID required"

### Requirement 6: Add Item to Sale by Product ID

**User Story:** As a cashier, I want to add products to a sale by product ID, so that I can build the transaction item list.

#### Acceptance Criteria

1. WHEN an add item request with product ID is received for an ACTIVE sale, THE Sales_API SHALL call the Product_API to retrieve product details
2. THE Sales_API SHALL validate that the requested quantity does not exceed available stock
3. WHEN the product does not exist in the sale, THE Sales_API SHALL create a new sale item with product ID, name, unit price snapshot, quantity, and calculated line total
4. WHEN the product already exists in the sale, THE Sales_API SHALL increment the quantity by the requested amount
5. THE Sales_API SHALL recalculate subtotal, tax, and total after adding the item
6. THE Sales_API SHALL return the updated sale with HTTP status 200
7. IF the quantity is less than 1, THEN THE Sales_API SHALL return HTTP status 400 with message "Quantity must be at least 1"
8. IF the requested quantity exceeds available stock, THEN THE Sales_API SHALL return HTTP status 409 with message "Insufficient stock available"
9. IF the sale is not in ACTIVE status, THEN THE Sales_API SHALL return HTTP status 409 with message "Sale is not active"
10. IF the product is not found in Product_API, THEN THE Sales_API SHALL return HTTP status 404 with message "Product not found"

### Requirement 7: Add Item to Sale by Barcode

**User Story:** As a cashier, I want to add products to a sale by scanning barcodes, so that I can quickly process items.

#### Acceptance Criteria

1. WHEN an add item request with barcode is received for an ACTIVE sale, THE Sales_API SHALL call the Product_API to retrieve product details by barcode
2. THE Sales_API SHALL validate that the requested quantity does not exceed available stock
3. WHEN the product does not exist in the sale, THE Sales_API SHALL create a new sale item with product ID, name, unit price snapshot, quantity, and calculated line total
4. WHEN the product already exists in the sale, THE Sales_API SHALL increment the quantity by the requested amount
5. THE Sales_API SHALL recalculate subtotal, tax, and total after adding the item
6. THE Sales_API SHALL return the updated sale with HTTP status 200
7. IF the quantity is less than 1, THEN THE Sales_API SHALL return HTTP status 400 with message "Quantity must be at least 1"
8. IF the requested quantity exceeds available stock, THEN THE Sales_API SHALL return HTTP status 409 with message "Insufficient stock available"
9. IF the sale is not in ACTIVE status, THEN THE Sales_API SHALL return HTTP status 409 with message "Sale is not active"
10. IF the barcode is not found in Product_API, THEN THE Sales_API SHALL return HTTP status 404 with message "Product not found"

### Requirement 8: Update Item Quantity

**User Story:** As a cashier, I want to update the quantity of an item in a sale, so that I can correct mistakes or adjust quantities.

#### Acceptance Criteria

1. WHEN an update quantity request is received for an ACTIVE sale, THE Sales_API SHALL validate that the item exists in the sale
2. THE Sales_API SHALL call the Product_API to verify the new quantity does not exceed available stock
3. THE Sales_API SHALL update the item quantity and recalculate the line total
4. THE Sales_API SHALL recalculate subtotal, tax, and total after updating the item
5. THE Sales_API SHALL return the updated sale with HTTP status 200
6. IF the new quantity is less than 1, THEN THE Sales_API SHALL return HTTP status 400 with message "Quantity must be at least 1"
7. IF the new quantity exceeds available stock, THEN THE Sales_API SHALL return HTTP status 409 with message "Insufficient stock available"
8. IF the item does not exist in the sale, THEN THE Sales_API SHALL return HTTP status 404 with message "Item not found in sale"
9. IF the sale is not in ACTIVE status, THEN THE Sales_API SHALL return HTTP status 409 with message "Sale is not active"

### Requirement 9: Remove Item from Sale

**User Story:** As a cashier, I want to remove an item from a sale, so that I can correct mistakes when wrong items were added.

#### Acceptance Criteria

1. WHEN a remove item request is received for an ACTIVE sale, THE Sales_API SHALL validate that the item exists in the sale
2. THE Sales_API SHALL remove the item from the sale
3. THE Sales_API SHALL recalculate subtotal, tax, and total after removing the item
4. THE Sales_API SHALL return the updated sale with HTTP status 200
5. IF the item does not exist in the sale, THEN THE Sales_API SHALL return HTTP status 404 with message "Item not found in sale"
6. IF the sale is not in ACTIVE status, THEN THE Sales_API SHALL return HTTP status 409 with message "Sale is not active"

### Requirement 10: Sale Totals Calculation

**User Story:** As a cashier, I want sale totals to be calculated automatically, so that pricing is accurate and consistent.

#### Acceptance Criteria

1. WHEN items are added, updated, or removed from a sale, THE Sales_API SHALL calculate subtotal as the sum of all line item totals
2. THE Sales_API SHALL calculate tax as subtotal multiplied by the configured tax rate
3. WHERE a discount is applied, THE Sales_API SHALL subtract the discount amount from the sum of subtotal and tax
4. THE Sales_API SHALL calculate total as subtotal plus tax minus discount
5. THE Sales_API SHALL use BigDecimal with 2 decimal places for all monetary values
6. THE Sales_API SHALL perform all intermediate calculations using integer arithmetic in cents to avoid floating-point errors
7. THE Sales_API SHALL use a default tax rate of 19 percent when no rate is configured

### Requirement 11: Apply Discount to Sale

**User Story:** As a cashier, I want to apply discounts to a sale, so that I can honor promotions and special pricing.

#### Acceptance Criteria

1. WHEN a percentage discount is applied to an ACTIVE sale, THE Sales_API SHALL calculate the discount amount as subtotal multiplied by the percentage
2. WHEN a fixed amount discount is applied to an ACTIVE sale, THE Sales_API SHALL use the provided discount amount
3. THE Sales_API SHALL recalculate the total after applying the discount
4. THE Sales_API SHALL return the updated sale with HTTP status 200
5. IF the discount percentage is less than 0 or greater than 100, THEN THE Sales_API SHALL return HTTP status 400 with message "Discount percentage must be between 0 and 100"
6. IF the fixed discount amount is negative, THEN THE Sales_API SHALL return HTTP status 400 with message "Discount amount cannot be negative"
7. IF the discount amount exceeds the subtotal plus tax, THEN THE Sales_API SHALL return HTTP status 400 with message "Discount cannot exceed sale total"
8. IF the sale is not in ACTIVE status, THEN THE Sales_API SHALL return HTTP status 409 with message "Sale is not active"

### Requirement 12: Cash Payment Checkout

**User Story:** As a cashier, I want to complete a cash sale, so that I can finalize the transaction and provide change to the customer.

#### Acceptance Criteria

1. WHEN a checkout request with payment type CASH is received for an ACTIVE sale, THE Sales_API SHALL validate that the sale contains at least one item
2. THE Sales_API SHALL validate that the amount received is greater than or equal to the sale total
3. THE Sales_API SHALL calculate change as amount received minus sale total
4. THE Sales_API SHALL call the Product_API to decrement stock for all items in the sale
5. THE Sales_API SHALL change the sale status to COMPLETED
6. THE Sales_API SHALL generate a unique transaction ID
7. THE Sales_API SHALL generate a receipt containing store name, terminal ID, cashier ID, timestamp, customer info if present, all items with prices, subtotal, tax, discount, total, payment method, amount received, change, and transaction ID
8. THE Sales_API SHALL return the completed sale with receipt and HTTP status 200
9. IF the sale has no items, THEN THE Sales_API SHALL return HTTP status 400 with message "Cannot checkout empty sale"
10. IF the amount received is less than the sale total, THEN THE Sales_API SHALL return HTTP status 400 with message "Amount received is insufficient"
11. IF any item has insufficient stock at checkout time, THEN THE Sales_API SHALL return HTTP status 409 with a list of out-of-stock items
12. IF the sale is not in ACTIVE status, THEN THE Sales_API SHALL return HTTP status 409 with message "Sale is not active"

### Requirement 13: Credit Payment Checkout

**User Story:** As a cashier, I want to complete a credit sale, so that approved customers can purchase on credit.

#### Acceptance Criteria

1. WHEN a checkout request with payment type CREDIT is received for an ACTIVE sale, THE Sales_API SHALL validate that the sale contains at least one item
2. THE Sales_API SHALL validate that a customer is associated with the sale
3. THE Sales_API SHALL call the Customer_API to retrieve the customer credit status
4. THE Sales_API SHALL validate that the credit status is APPROVED
5. THE Sales_API SHALL call the Product_API to decrement stock for all items in the sale
6. THE Sales_API SHALL change the sale status to COMPLETED
7. THE Sales_API SHALL generate a unique transaction ID
8. THE Sales_API SHALL generate a unique credit reference number
9. THE Sales_API SHALL generate a receipt containing store name, terminal ID, cashier ID, timestamp, customer info, all items with prices, subtotal, tax, discount, total, payment method CREDIT, credit reference number, and transaction ID
10. THE Sales_API SHALL return the completed sale with receipt and HTTP status 200
11. IF the sale has no items, THEN THE Sales_API SHALL return HTTP status 400 with message "Cannot checkout empty sale"
12. IF no customer is associated with the sale, THEN THE Sales_API SHALL return HTTP status 422 with message "Customer required for credit sales"
13. IF the customer credit status is not APPROVED, THEN THE Sales_API SHALL return HTTP status 422 with message "Customer credit not approved"
14. IF any item has insufficient stock at checkout time, THEN THE Sales_API SHALL return HTTP status 409 with a list of out-of-stock items
15. IF the sale is not in ACTIVE status, THEN THE Sales_API SHALL return HTTP status 409 with message "Sale is not active"

### Requirement 14: Cancel Sale

**User Story:** As a cashier, I want to cancel a sale, so that I can abort a transaction that cannot be completed.

#### Acceptance Criteria

1. WHEN a cancellation request is received for an ACTIVE or FROZEN sale, THE Sales_API SHALL validate that a cancellation reason is provided
2. THE Sales_API SHALL change the sale status to CANCELLED
3. THE Sales_API SHALL record the cancellation reason
4. THE Sales_API SHALL return the cancelled sale with HTTP status 200
5. IF the cancellation reason is empty or exceeds 255 characters, THEN THE Sales_API SHALL return HTTP status 400 with message "Cancellation reason required and must not exceed 255 characters"
6. IF the sale status is COMPLETED, RETURNED, or PARTIALLY_RETURNED, THEN THE Sales_API SHALL return HTTP status 409 with message "Cannot cancel completed or returned sale"
7. IF the sale status is already CANCELLED, THEN THE Sales_API SHALL return HTTP status 409 with message "Sale is already cancelled"

### Requirement 15: Freeze Sale

**User Story:** As a cashier, I want to freeze a sale, so that I can temporarily pause it and start a new transaction for another customer.

#### Acceptance Criteria

1. WHEN a freeze request is received for an ACTIVE sale, THE Sales_API SHALL change the sale status to FROZEN
2. THE Sales_API SHALL preserve all items, quantities, prices, and totals
3. THE Sales_API SHALL record the freeze timestamp
4. THE Sales_API SHALL return the frozen sale with HTTP status 200
5. IF the sale is not in ACTIVE status, THEN THE Sales_API SHALL return HTTP status 409 with message "Only active sales can be frozen"

### Requirement 16: Resume Frozen Sale

**User Story:** As a cashier, I want to resume a frozen sale, so that I can continue processing it after attending to another customer.

#### Acceptance Criteria

1. WHEN a resume request is received for a FROZEN sale, THE Sales_API SHALL change the sale status to ACTIVE
2. THE Sales_API SHALL preserve all items, quantities, prices, and totals
3. THE Sales_API SHALL return the resumed sale with HTTP status 200
4. IF the sale is not in FROZEN status, THEN THE Sales_API SHALL return HTTP status 409 with message "Only frozen sales can be resumed"

### Requirement 17: List Frozen Sales by Terminal

**User Story:** As a cashier, I want to see all frozen sales for my terminal, so that I can select which one to resume.

#### Acceptance Criteria

1. WHEN a list frozen sales request is received with a terminal ID, THE Sales_API SHALL return all sales with status FROZEN for that terminal
2. THE Sales_API SHALL include sale ID, freeze timestamp, item count, and total for each frozen sale
3. THE Sales_API SHALL order the results by freeze timestamp descending
4. WHEN no frozen sales exist for the terminal, THE Sales_API SHALL return an empty list with HTTP status 200

### Requirement 18: Automatic Cancellation of Expired Frozen Sales

**User Story:** As a system administrator, I want frozen sales to expire automatically, so that terminals do not accumulate abandoned sales indefinitely.

#### Acceptance Criteria

1. WHILE a sale is in FROZEN status, THE Sales_API SHALL track the elapsed time since freeze timestamp
2. WHEN the elapsed time exceeds the configured expiration timeout, THE Sales_API SHALL change the sale status to CANCELLED
3. THE Sales_API SHALL set the cancellation reason to "Automatically cancelled due to freeze timeout"
4. THE Sales_API SHALL use a default expiration timeout of 2 hours when no timeout is configured
5. THE Sales_API SHALL process expiration checks periodically

### Requirement 19: Full Return of Completed Sale

**User Story:** As a cashier, I want to process a full return, so that customers can return all items from a purchase.

#### Acceptance Criteria

1. WHEN a full return request is received for a COMPLETED sale, THE Sales_API SHALL validate that a return reason is provided
2. THE Sales_API SHALL call the Product_API to increment stock for all items in the original sale
3. THE Sales_API SHALL change the sale status to RETURNED
4. THE Sales_API SHALL record the return reason and return timestamp
5. THE Sales_API SHALL generate a return receipt referencing the original transaction ID and listing all returned items
6. THE Sales_API SHALL return the updated sale with return receipt and HTTP status 200
7. IF the return reason is empty, THEN THE Sales_API SHALL return HTTP status 400 with message "Return reason required"
8. IF the sale status is not COMPLETED, THEN THE Sales_API SHALL return HTTP status 409 with message "Only completed sales can be returned"
9. IF the sale status is already RETURNED, THEN THE Sales_API SHALL return HTTP status 409 with message "Sale has already been fully returned"

### Requirement 20: Partial Return of Completed Sale

**User Story:** As a cashier, I want to process a partial return, so that customers can return specific items from a purchase.

#### Acceptance Criteria

1. WHEN a partial return request is received for a COMPLETED or PARTIALLY_RETURNED sale, THE Sales_API SHALL validate that return items and quantities are provided
2. THE Sales_API SHALL validate that each returned quantity does not exceed the originally purchased quantity minus any previously returned quantity
3. THE Sales_API SHALL call the Product_API to increment stock only for the returned items and quantities
4. THE Sales_API SHALL change the sale status to PARTIALLY_RETURNED
5. THE Sales_API SHALL record the return reason, returned items, quantities, and return timestamp
6. THE Sales_API SHALL generate a return receipt referencing the original transaction ID and listing only the returned items
7. THE Sales_API SHALL return the updated sale with return receipt and HTTP status 200
8. IF the return reason is empty, THEN THE Sales_API SHALL return HTTP status 400 with message "Return reason required"
9. IF any returned quantity exceeds the available quantity for return, THEN THE Sales_API SHALL return HTTP status 400 with message "Return quantity exceeds purchased quantity"
10. IF the sale status is not COMPLETED or PARTIALLY_RETURNED, THEN THE Sales_API SHALL return HTTP status 409 with message "Only completed sales can be returned"
11. IF the sale status is RETURNED, THEN THE Sales_API SHALL return HTTP status 409 with message "Sale has already been fully returned"

### Requirement 21: Credit Note Generation for Credit Sale Returns

**User Story:** As a cashier, I want credit notes generated for credit sale returns, so that credit customers receive proper documentation for returned purchases.

#### Acceptance Criteria

1. WHEN a return is processed for a sale with payment type CREDIT, THE Sales_API SHALL generate a credit note in addition to the return receipt
2. THE Sales_API SHALL include the original credit reference number in the credit note
3. THE Sales_API SHALL include a new credit note number in the credit note
4. THE Sales_API SHALL include all returned items and amounts in the credit note
5. THE Sales_API SHALL return the credit note along with the return receipt

### Requirement 22: Retrieve Sale by ID

**User Story:** As a cashier, I want to retrieve a sale by its ID, so that I can view transaction details.

#### Acceptance Criteria

1. WHEN a get sale request is received with a sale ID, THE Sales_API SHALL return the sale with all items, totals, status, and associated information
2. THE Sales_API SHALL return HTTP status 200 with the sale details
3. IF the sale ID does not exist, THEN THE Sales_API SHALL return HTTP status 404 with message "Sale not found"

### Requirement 23: List Sales by Terminal

**User Story:** As a cashier, I want to see all sales for my terminal, so that I can review recent transactions.

#### Acceptance Criteria

1. WHEN a list sales request is received with a terminal ID, THE Sales_API SHALL return all sales for that terminal
2. THE Sales_API SHALL include sale ID, status, timestamp, item count, and total for each sale
3. THE Sales_API SHALL order the results by creation timestamp descending
4. WHERE a status filter is provided, THE Sales_API SHALL return only sales matching that status
5. WHERE a date range filter is provided, THE Sales_API SHALL return only sales within that date range
6. WHEN no sales exist for the terminal, THE Sales_API SHALL return an empty list with HTTP status 200

### Requirement 24: API Documentation

**User Story:** As a developer, I want interactive API documentation, so that I can understand and test the API endpoints.

#### Acceptance Criteria

1. THE Sales_API SHALL expose OpenAPI specification at the /v3/api-docs endpoint
2. THE Sales_API SHALL expose Swagger UI at the /swagger-ui.html endpoint
3. THE Sales_API SHALL document all endpoints with request schemas, response schemas, and status codes
4. THE Sales_API SHALL include example requests and responses for each endpoint

### Requirement 25: Input Validation

**User Story:** As a system, I want to validate all input data, so that invalid requests are rejected with clear error messages.

#### Acceptance Criteria

1. WHEN a request is received with invalid data, THE Sales_API SHALL validate the request using Jakarta Bean Validation
2. THE Sales_API SHALL return HTTP status 400 with a list of validation errors
3. THE Sales_API SHALL include the field name and error message for each validation failure
4. THE Sales_API SHALL validate required fields, data types, string lengths, numeric ranges, and format constraints

### Requirement 26: Error Response Format

**User Story:** As a client application, I want consistent error response format, so that I can handle errors uniformly.

#### Acceptance Criteria

1. WHEN an error occurs, THE Sales_API SHALL return a JSON response containing timestamp, status code, error type, message, and path
2. THE Sales_API SHALL use appropriate HTTP status codes for different error types
3. THE Sales_API SHALL return HTTP status 400 for validation errors
4. THE Sales_API SHALL return HTTP status 404 for resource not found errors
5. THE Sales_API SHALL return HTTP status 409 for business rule conflicts
6. THE Sales_API SHALL return HTTP status 422 for unprocessable entity errors
7. THE Sales_API SHALL return HTTP status 500 for internal server errors
8. THE Sales_API SHALL return HTTP status 503 for external service unavailability

### Requirement 27: Database Schema Compatibility

**User Story:** As a developer, I want the database schema to be compatible with PostgreSQL, so that the application can be deployed to production with PostgreSQL.

#### Acceptance Criteria

1. THE Sales_API SHALL use H2 database in development and test environments
2. THE Sales_API SHALL use PostgreSQL-compatible data types and SQL syntax
3. THE Sales_API SHALL use Spring Data JPA with Hibernate for ORM
4. THE Sales_API SHALL generate database schema automatically from entity classes
5. THE Sales_API SHALL support migration to PostgreSQL without schema changes

### Requirement 28: External API Mocking in Tests

**User Story:** As a developer, I want external APIs mocked in tests, so that tests run reliably without external dependencies.

#### Acceptance Criteria

1. THE Sales_API SHALL use WireMock or MockRestServiceServer to mock Product_API in tests
2. THE Sales_API SHALL use WireMock or MockRestServiceServer to mock Customer_API in tests
3. THE Sales_API SHALL configure mock responses for all external API calls used in tests
4. THE Sales_API SHALL verify that external API calls are made with correct parameters in tests

### Requirement 29: Test Coverage Requirements

**User Story:** As a developer, I want comprehensive test coverage, so that the code is reliable and maintainable.

#### Acceptance Criteria

1. THE Sales_API SHALL achieve at least 80 percent line coverage across the entire project
2. THE Sales_API SHALL achieve at least 90 percent line coverage in the service layer
3. THE Sales_API SHALL use JaCoCo to measure and report test coverage
4. THE Sales_API SHALL include unit tests for all service methods
5. THE Sales_API SHALL include integration tests for all API endpoints
6. THE Sales_API SHALL test all state transitions in the sale lifecycle
7. THE Sales_API SHALL test all error conditions and edge cases

### Requirement 30: Monetary Calculation Precision

**User Story:** As a system, I want precise monetary calculations, so that rounding errors do not occur in financial transactions.

#### Acceptance Criteria

1. THE Sales_API SHALL use BigDecimal for all monetary values
2. THE Sales_API SHALL use RoundingMode.HALF_UP for all rounding operations
3. THE Sales_API SHALL maintain 2 decimal places for all monetary values in responses
4. THE Sales_API SHALL perform intermediate calculations using integer arithmetic in cents
5. THE Sales_API SHALL convert between decimal and integer representations without precision loss

---

## Notes

This requirements document follows the EARS (Easy Approach to Requirements Syntax) patterns and INCOSE quality rules. Each requirement uses active voice, avoids vague terms, specifies measurable criteria, and focuses on what the system should do rather than how it should be implemented.

The acceptance criteria are designed to be testable and will be used to generate property-based tests and integration tests in the implementation phase.

External API contracts (Product_API and Customer_API) are specified at the interface level. The actual implementation will mock these services in tests using WireMock or similar tools.

All monetary calculations use BigDecimal with 2 decimal precision and internal integer arithmetic to avoid floating-point errors, which is critical for financial accuracy in a POS system.
