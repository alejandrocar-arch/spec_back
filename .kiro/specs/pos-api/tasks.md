# Tasks: Sales API for Supermarket POS System

## Phase 1: Project Setup and Configuration

### Task 1.1: Initialize Spring Boot Project
- [x] Create Spring Boot 3.x project with Java 17+
- [x] Configure Maven/Gradle with required dependencies
- [x] Set up project package structure (controller, service, repository, entity, dto, exception, client, config)
- [x] Create main application class with @SpringBootApplication

### Task 1.2: Configure Dependencies
- [x] Add spring-boot-starter-web
- [x] Add spring-boot-starter-data-jpa
- [x] Add spring-boot-starter-validation
- [x] Add H2 database dependency
- [x] Add PostgreSQL driver
- [x] Add SpringDoc OpenAPI dependency
- [x] Add spring-retry and resilience4j dependencies
- [x] Add testing dependencies (JUnit 5, Mockito, WireMock)
- [x] Add JaCoCo plugin for code coverage

### Task 1.3: Configure Application Properties
- [x] Create application.yml with H2 database configuration
- [x] Configure JPA/Hibernate settings (ddl-auto, show-sql)
- [x] Configure custom properties (tax-rate, store-name, frozen-sale-expiration-hours)
- [x] Configure external API base URLs and timeouts
- [x] Configure SpringDoc OpenAPI paths
- [x] Create application-test.yml for test profile

## Phase 2: Domain Model Implementation

### Task 2.1: Create Enumerations
- [x] Create SaleStatus enum (ACTIVE, FROZEN, COMPLETED, CANCELLED, RETURNED, PARTIALLY_RETURNED)
- [x] Create PaymentType enum (CASH, CREDIT)
- [x] Create DiscountType enum (PERCENTAGE, FIXED_AMOUNT)
- [ ] Create CreditStatus enum (APPROVED, REJECTED, PENDING)

### Task 2.2: Implement Sale Entity
- [ ] Create Sale entity with @Entity annotation
- [ ] Add all fields (id, terminalId, cashierId, customerId, status, monetary fields, timestamps)
- [ ] Configure @OneToMany relationship with SaleItem (cascade ALL, orphan removal)
- [ ] Add indexes (@Table with @Index annotations)
- [ ] Implement business methods (addItem, removeItem, freeze, resume, cancel, complete, etc.)
- [ ] Implement state validation methods (canAddItems, canCheckout, canFreeze, etc.)
- [ ] Add @PrePersist for createdAt timestamp

### Task 2.3: Implement SaleItem Entity
- [ ] Create SaleItem entity with @Entity annotation
- [ ] Add all fields (id, saleId, productId, productName, barcode, unitPrice, quantity, lineTotal, returnedQuantity)
- [ ] Configure @ManyToOne relationship with Sale
- [ ] Add indexes for saleId and productId
- [ ] Implement business methods (updateQuantity, calculateLineTotal, getAvailableForReturn, incrementReturnedQuantity)

## Phase 3: Repository Layer

### Task 3.1: Create Sale Repository
- [ ] Create SaleRepository interface extending JpaRepository<Sale, Long>
- [ ] Add custom query method: findByTerminalIdOrderByCreatedAtDesc
- [ ] Add custom query method: findByTerminalIdAndStatus
- [ ] Add custom query method: findByTerminalIdAndStatusAndCreatedAtBetween
- [ ] Add custom query method: findByTransactionId

### Task 3.2: Create SaleItem Repository
- [ ] Create SaleItemRepository interface extending JpaRepository<SaleItem, Long>
- [ ] Add custom query method: findBySaleId
- [ ] Add custom query method: findByProductId

## Phase 4: Exception Handling

### Task 4.1: Create Base Exception Classes
- [ ] Create BusinessException extending RuntimeException
- [ ] Create ResourceNotFoundException extending RuntimeException
- [ ] Create ExternalServiceException extending RuntimeException

### Task 4.2: Create Specific Business Exceptions
- [ ] Create SaleNotActiveException
- [ ] Create SaleNotFrozenException
- [ ] Create SaleAlreadyCancelledException
- [ ] Create SaleAlreadyReturnedException
- [ ] Create InsufficientStockException
- [ ] Create InvalidDiscountException
- [ ] Create InvalidPaymentException
- [ ] Create CreditNotApprovedException
- [ ] Create CustomerRequiredException
- [ ] Create InvalidReturnQuantityException

### Task 4.3: Create Resource Not Found Exceptions
- [ ] Create SaleNotFoundException
- [ ] Create SaleItemNotFoundException
- [ ] Create ProductNotFoundException
- [ ] Create CustomerNotFoundException

### Task 4.4: Create External Service Exceptions
- [ ] Create ProductServiceUnavailableException
- [ ] Create CustomerServiceUnavailableException

### Task 4.5: Implement Global Exception Handler
- [ ] Create GlobalExceptionHandler with @RestControllerAdvice
- [ ] Create ErrorResponse DTO
- [ ] Create ValidationError DTO
- [ ] Implement handler for MethodArgumentNotValidException (400)
- [ ] Implement handler for BusinessException subclasses (400/409/422)
- [ ] Implement handler for ResourceNotFoundException subclasses (404)
- [ ] Implement handler for ExternalServiceException subclasses (503)
- [ ] Implement handler for HttpMessageNotReadableException (400)
- [ ] Implement handler for HttpRequestMethodNotSupportedException (405)
- [ ] Implement catch-all handler for Exception (500)

## Phase 5: DTOs and Request/Response Models

### Task 5.1: Create External DTOs
- [ ] Create ProductDTO (id, name, barcode, unitPrice, availableStock, category)
- [ ] Create CustomerDTO (id, fullName, documentType, documentNumber, creditStatus)

### Task 5.2: Create Request DTOs
- [ ] Create CreateSaleRequest with validation annotations
- [ ] Create AddItemByProductIdRequest with validation
- [ ] Create AddItemByBarcodeRequest with validation
- [ ] Create UpdateQuantityRequest with validation
- [ ] Create ApplyDiscountRequest with validation
- [ ] Create CheckoutRequest with validation
- [ ] Create CancelSaleRequest with validation
- [ ] Create FullReturnRequest with validation
- [ ] Create PartialReturnRequest with validation
- [ ] Create ReturnItemRequest with validation

### Task 5.3: Create Response DTOs
- [ ] Create SaleDTO
- [ ] Create SaleItemDTO
- [ ] Create ReceiptDTO
- [ ] Create ReceiptItemDTO
- [ ] Create ReturnReceiptDTO
- [ ] Create ReturnedItemDTO
- [ ] Create CheckoutResponse
- [ ] Create ReturnResponse
- [ ] Create FrozenSaleDTO
- [ ] Create CreditNoteDTO

### Task 5.4: Create Mappers
- [ ] Create SaleMapper (entity to DTO and vice versa)
- [ ] Create SaleItemMapper (entity to DTO and vice versa)

## Phase 6: External API Clients

### Task 6.1: Configure RestTemplate
- [ ] Create RestTemplateConfig class
- [ ] Configure RestTemplate bean with connection pooling
- [ ] Configure timeouts and error handling

### Task 6.2: Implement Product API Client
- [ ] Create ProductApiClient interface
- [ ] Create ProductApiClientImpl with @Component
- [ ] Implement searchProductsByName method
- [ ] Implement searchProductByBarcode method
- [ ] Implement getProductById method
- [ ] Implement decrementStock method
- [ ] Implement incrementStock method
- [ ] Add retry logic with @Retryable
- [ ] Add circuit breaker with @CircuitBreaker
- [ ] Handle exceptions and throw ProductServiceUnavailableException

### Task 6.3: Implement Customer API Client
- [ ] Create CustomerApiClient interface
- [ ] Create CustomerApiClientImpl with @Component
- [ ] Implement searchCustomersByName method
- [ ] Implement searchCustomerByDocument method
- [ ] Implement getCustomerById method
- [ ] Implement validateCreditStatus method
- [ ] Add retry logic with @Retryable
- [ ] Add circuit breaker with @CircuitBreaker
- [ ] Handle exceptions and throw CustomerServiceUnavailableException

## Phase 7: Service Layer Implementation

### Task 7.1: Implement CalculationService
- [ ] Create CalculationService interface
- [ ] Create CalculationServiceImpl with @Service
- [ ] Implement calculateLineTotal using BigDecimal
- [ ] Implement calculateSubtotal
- [ ] Implement calculateTax with configurable tax rate
- [ ] Implement calculateDiscountAmount (percentage and fixed)
- [ ] Implement calculateTotal
- [ ] Implement calculateChange
- [ ] Implement recalculateSaleTotals
- [ ] Use RoundingMode.HALF_UP for all operations
- [ ] Ensure 2 decimal places precision

### Task 7.2: Implement ProductService
- [ ] Create ProductService interface
- [ ] Create ProductServiceImpl with @Service
- [ ] Inject ProductApiClient
- [ ] Implement searchByName
- [ ] Implement searchByBarcode
- [ ] Implement getById
- [ ] Implement validateStockAvailability
- [ ] Implement decrementStock
- [ ] Implement incrementStock
- [ ] Handle exceptions appropriately

### Task 7.3: Implement CustomerService
- [ ] Create CustomerService interface
- [ ] Create CustomerServiceImpl with @Service
- [ ] Inject CustomerApiClient
- [ ] Implement searchByName
- [ ] Implement searchByDocument
- [ ] Implement getById
- [ ] Implement validateCreditApproval
- [ ] Handle exceptions appropriately

### Task 7.4: Implement SaleService - Part 1 (Sale Creation and Item Management)
- [ ] Create SaleService interface
- [ ] Create SaleServiceImpl with @Service and @Transactional
- [ ] Inject repositories, CalculationService, ProductService, CustomerService
- [ ] Implement createSale (Requirement 5)
- [ ] Implement addItemByProductId (Requirement 6)
- [ ] Implement addItemByBarcode (Requirement 7)
- [ ] Implement updateItemQuantity (Requirement 8)
- [ ] Implement removeItem (Requirement 9)
- [ ] Implement getSaleById (Requirement 22)
- [ ] Implement getSalesByTerminal (Requirement 23)

### Task 7.5: Implement SaleService - Part 2 (Discount and Checkout)
- [ ] Implement applyDiscount (Requirement 11)
- [ ] Implement checkout for CASH payment (Requirement 12)
- [ ] Implement checkout for CREDIT payment (Requirement 13)
- [ ] Generate unique transaction ID
- [ ] Generate credit reference number for credit sales
- [ ] Generate receipt with all required fields
- [ ] Call ProductService to decrement stock
- [ ] Call CustomerService to validate credit status

### Task 7.6: Implement SaleService - Part 3 (Freeze, Resume, Cancel)
- [ ] Implement freezeSale (Requirement 15)
- [ ] Implement resumeSale (Requirement 16)
- [ ] Implement getFrozenSalesByTerminal (Requirement 17)
- [ ] Implement cancelSale (Requirement 14)
- [ ] Validate state transitions for all operations

### Task 7.7: Implement SaleService - Part 4 (Returns)
- [ ] Implement processFullReturn (Requirement 19)
- [ ] Implement processPartialReturn (Requirement 20)
- [ ] Generate return receipt
- [ ] Generate credit note for credit sales (Requirement 21)
- [ ] Call ProductService to increment stock
- [ ] Validate return quantities

### Task 7.8: Implement Scheduled Task for Frozen Sale Expiration
- [ ] Create @Scheduled method to check expired frozen sales (Requirement 18)
- [ ] Configure cron expression for periodic execution
- [ ] Auto-cancel sales exceeding expiration timeout (default 2 hours)
- [ ] Set cancellation reason to "Automatically cancelled due to freeze timeout"

## Phase 8: Controller Layer Implementation

### Task 8.1: Implement ProductSearchController
- [ ] Create ProductSearchController with @RestController and @RequestMapping
- [ ] Implement GET /api/v1/products/search/by-name (Requirement 1)
- [ ] Implement GET /api/v1/products/search/by-barcode (Requirement 2)
- [ ] Add @Valid annotations for validation
- [ ] Add OpenAPI annotations (@Operation, @ApiResponse)

### Task 8.2: Implement CustomerSearchController
- [ ] Create CustomerSearchController with @RestController and @RequestMapping
- [ ] Implement GET /api/v1/customers/search/by-name (Requirement 3)
- [ ] Implement GET /api/v1/customers/search/by-document (Requirement 4)
- [ ] Add @Valid annotations for validation
- [ ] Add OpenAPI annotations

### Task 8.3: Implement SaleController - Part 1 (Sale Management)
- [ ] Create SaleController with @RestController and @RequestMapping
- [ ] Implement POST /api/v1/sales (Requirement 5)
- [ ] Implement GET /api/v1/sales/{saleId} (Requirement 22)
- [ ] Implement GET /api/v1/sales/terminal/{terminalId} (Requirement 23)
- [ ] Implement GET /api/v1/sales/terminal/{terminalId}/frozen (Requirement 17)
- [ ] Add @Valid annotations and OpenAPI annotations

### Task 8.4: Implement SaleController - Part 2 (Item Management)
- [ ] Implement POST /api/v1/sales/{saleId}/items/by-product-id (Requirement 6)
- [ ] Implement POST /api/v1/sales/{saleId}/items/by-barcode (Requirement 7)
- [ ] Implement PUT /api/v1/sales/{saleId}/items/{itemId}/quantity (Requirement 8)
- [ ] Implement DELETE /api/v1/sales/{saleId}/items/{itemId} (Requirement 9)
- [ ] Add @Valid annotations and OpenAPI annotations

### Task 8.5: Implement SaleController - Part 3 (Operations)
- [ ] Implement POST /api/v1/sales/{saleId}/discount (Requirement 11)
- [ ] Implement POST /api/v1/sales/{saleId}/checkout (Requirements 12, 13)
- [ ] Implement POST /api/v1/sales/{saleId}/freeze (Requirement 15)
- [ ] Implement POST /api/v1/sales/{saleId}/resume (Requirement 16)
- [ ] Implement POST /api/v1/sales/{saleId}/cancel (Requirement 14)
- [ ] Add @Valid annotations and OpenAPI annotations

### Task 8.6: Implement SaleController - Part 4 (Returns)
- [ ] Implement POST /api/v1/sales/{saleId}/return/full (Requirement 19)
- [ ] Implement POST /api/v1/sales/{saleId}/return/partial (Requirement 20)
- [ ] Add @Valid annotations and OpenAPI annotations

## Phase 9: Unit Testing

### Task 9.1: Unit Test CalculationService
- [ ] Test calculateLineTotal with various prices and quantities
- [ ] Test calculateSubtotal with multiple items
- [ ] Test calculateTax with different tax rates
- [ ] Test calculateDiscountAmount for percentage discounts
- [ ] Test calculateDiscountAmount for fixed amount discounts
- [ ] Test calculateTotal with various combinations
- [ ] Test calculateChange
- [ ] Test BigDecimal precision and rounding
- [ ] Achieve 90%+ coverage

### Task 9.2: Unit Test ProductService
- [ ] Mock ProductApiClient
- [ ] Test searchByName success case
- [ ] Test searchByBarcode success and not found cases
- [ ] Test getById success and not found cases
- [ ] Test validateStockAvailability with sufficient and insufficient stock
- [ ] Test decrementStock and incrementStock
- [ ] Test ProductServiceUnavailableException handling
- [ ] Achieve 90%+ coverage

### Task 9.3: Unit Test CustomerService
- [ ] Mock CustomerApiClient
- [ ] Test searchByName success case
- [ ] Test searchByDocument success and not found cases
- [ ] Test getById success and not found cases
- [ ] Test validateCreditApproval with APPROVED, REJECTED, PENDING statuses
- [ ] Test CustomerServiceUnavailableException handling
- [ ] Achieve 90%+ coverage

### Task 9.4: Unit Test SaleService - Part 1 (Creation and Items)
- [ ] Mock all dependencies (repositories, services)
- [ ] Test createSale with valid and invalid inputs
- [ ] Test addItemByProductId - new item and existing item
- [ ] Test addItemByBarcode - new item and existing item
- [ ] Test addItemByProductId with insufficient stock
- [ ] Test addItemByProductId on non-active sale
- [ ] Test updateItemQuantity with valid and invalid quantities
- [ ] Test removeItem success and not found cases
- [ ] Test getSaleById success and not found cases
- [ ] Test getSalesByTerminal with various filters
- [ ] Achieve 90%+ coverage

### Task 9.5: Unit Test SaleService - Part 2 (Discount and Checkout)
- [ ] Test applyDiscount with percentage and fixed amount
- [ ] Test applyDiscount with invalid values
- [ ] Test applyDiscount on non-active sale
- [ ] Test checkout with CASH payment - success case
- [ ] Test checkout with CASH payment - insufficient amount
- [ ] Test checkout with CASH payment - empty sale
- [ ] Test checkout with CREDIT payment - success case
- [ ] Test checkout with CREDIT payment - no customer
- [ ] Test checkout with CREDIT payment - credit not approved
- [ ] Test checkout with insufficient stock
- [ ] Verify transaction ID and receipt generation
- [ ] Verify stock decrement calls
- [ ] Achieve 90%+ coverage

### Task 9.6: Unit Test SaleService - Part 3 (Freeze, Resume, Cancel)
- [ ] Test freezeSale on active sale
- [ ] Test freezeSale on non-active sale
- [ ] Test resumeSale on frozen sale
- [ ] Test resumeSale on non-frozen sale
- [ ] Test getFrozenSalesByTerminal
- [ ] Test cancelSale on active and frozen sales
- [ ] Test cancelSale on completed sale (should fail)
- [ ] Test cancelSale with invalid reason
- [ ] Achieve 90%+ coverage

### Task 9.7: Unit Test SaleService - Part 4 (Returns)
- [ ] Test processFullReturn on completed sale
- [ ] Test processFullReturn on non-completed sale (should fail)
- [ ] Test processFullReturn on already returned sale (should fail)
- [ ] Test processPartialReturn with valid quantities
- [ ] Test processPartialReturn with excessive quantities (should fail)
- [ ] Test processPartialReturn multiple times
- [ ] Test credit note generation for credit sales
- [ ] Verify stock increment calls
- [ ] Achieve 90%+ coverage

### Task 9.8: Unit Test Sale Entity
- [ ] Test state transition methods (freeze, resume, cancel, complete)
- [ ] Test validation methods (canAddItems, canCheckout, canFreeze, etc.)
- [ ] Test addItem and removeItem methods
- [ ] Test state transition violations

### Task 9.9: Unit Test SaleItem Entity
- [ ] Test updateQuantity and calculateLineTotal
- [ ] Test getAvailableForReturn
- [ ] Test incrementReturnedQuantity

### Task 9.10: Unit Test Repositories
- [ ] Test SaleRepository custom queries with test data
- [ ] Test SaleItemRepository custom queries with test data

## Phase 10: Integration Testing

### Task 10.1: Setup Integration Test Infrastructure
- [ ] Create base integration test class with @SpringBootTest
- [ ] Configure H2 test database
- [ ] Setup WireMock for external API mocking
- [ ] Create test data builders (SaleTestBuilder, ProductDTOTestBuilder, CustomerDTOTestBuilder)

### Task 10.2: Integration Test - Cash Sale Flow
- [ ] Create sale
- [ ] Add multiple items by product ID
- [ ] Add item by barcode
- [ ] Apply discount
- [ ] Checkout with cash payment
- [ ] Verify receipt generation
- [ ] Verify WireMock calls for stock decrement
- [ ] Verify sale status is COMPLETED

### Task 10.3: Integration Test - Credit Sale Flow
- [ ] Create sale with customer
- [ ] Add items
- [ ] Checkout with credit payment
- [ ] Verify credit validation WireMock call
- [ ] Verify receipt with credit reference
- [ ] Verify stock decrement WireMock calls
- [ ] Verify sale status is COMPLETED

### Task 10.4: Integration Test - Freeze and Resume Flow
- [ ] Create sale
- [ ] Add items
- [ ] Freeze sale
- [ ] Create another sale
- [ ] List frozen sales by terminal
- [ ] Resume first sale
- [ ] Complete checkout
- [ ] Verify state transitions

### Task 10.5: Integration Test - Full Return Flow
- [ ] Complete a cash sale
- [ ] Process full return
- [ ] Verify return receipt generation
- [ ] Verify stock increment WireMock calls
- [ ] Verify sale status is RETURNED

### Task 10.6: Integration Test - Partial Return Flow
- [ ] Complete a sale with 5 items
- [ ] Return 2 items
- [ ] Verify partial return receipt
- [ ] Verify partial stock increment WireMock calls
- [ ] Verify sale status is PARTIALLY_RETURNED
- [ ] Return remaining items
- [ ] Verify final status is RETURNED

### Task 10.7: Integration Test - Cancellation Flow
- [ ] Create sale
- [ ] Add items
- [ ] Cancel with reason
- [ ] Verify cannot modify cancelled sale
- [ ] Verify no stock changes

### Task 10.8: Integration Test - Error Scenarios
- [ ] Test add item with insufficient stock
- [ ] Test checkout empty sale
- [ ] Test checkout with insufficient cash
- [ ] Test credit sale without customer
- [ ] Test credit sale with rejected credit status
- [ ] Test return more quantity than purchased
- [ ] Test cancel completed sale
- [ ] Test resume non-frozen sale
- [ ] Test freeze non-active sale
- [ ] Test Product API unavailable (WireMock returns 503)
- [ ] Test Customer API unavailable (WireMock returns 503)

### Task 10.9: Integration Test - All Controllers
- [ ] Test ProductSearchController endpoints
- [ ] Test CustomerSearchController endpoints
- [ ] Test all SaleController endpoints
- [ ] Verify HTTP status codes
- [ ] Verify error response format
- [ ] Verify validation error handling

## Phase 11: API Documentation

### Task 11.1: Configure SpringDoc OpenAPI
- [x] Verify SpringDoc dependency is configured
- [x] Add OpenAPI configuration class
- [x] Configure API info (title, version, description)
- [x] Configure server URLs

### Task 11.2: Add OpenAPI Annotations
- [x] Add @Tag annotations to controllers
- [x] Add @Operation annotations to all endpoints
- [x] Add @ApiResponse annotations for all status codes
- [x] Add @Schema annotations to DTOs
- [x] Add example values to request/response models

### Task 11.3: Verify API Documentation
- [x] Start application
- [x] Access /v3/api-docs endpoint
- [x] Access /swagger-ui.html endpoint
- [x] Verify all endpoints are documented
- [x] Verify request/response schemas are correct
- [x] Test endpoints through Swagger UI

## Phase 12: Code Coverage and Quality

### Task 12.1: Configure JaCoCo
- [x] Verify JaCoCo plugin is configured in pom.xml/build.gradle
- [x] Configure coverage rules (80% overall, 90% service layer)
- [x] Configure exclusions (DTOs, config classes, main class)
- [x] Configure report generation

### Task 12.2: Run Coverage Analysis
- [x] Run all tests with coverage
- [x] Generate JaCoCo HTML report
- [x] Verify overall coverage >= 80%
- [x] Verify service layer coverage >= 90%
- [x] Identify and test uncovered code paths

### Task 12.3: Code Quality Review
- [x] Review code for Spring Boot best practices
- [x] Verify proper use of @Transactional
- [x] Verify proper exception handling
- [x] Verify input validation on all endpoints
- [x] Verify BigDecimal usage for monetary calculations
- [x] Verify proper logging
- [x] Run static code analysis (optional: SonarQube)

## Phase 13: Final Integration and Testing

### Task 13.1: End-to-End Manual Testing
- [ ] Start application locally
- [x] Test all endpoints using Postman/Insomnia
- [x] Test complete cash sale workflow
- [x] Test complete credit sale workflow
- [x] Test freeze/resume workflow
- [x] Test return workflows
- [x] Test error scenarios
- [x] Verify H2 console data

### Task 13.2: Performance Testing (Optional)
- [x] Test concurrent sale creation
- [x] Test concurrent item additions
- [x] Test database query performance
- [x] Optimize slow queries if needed

### Task 13.3: Documentation Review
- [x] Review README.md (create if not exists)
- [x] Document how to run the application
- [x] Document how to run tests
- [x] Document API endpoints
- [x] Document configuration properties
- [x] Document external API contracts

## Phase 14: Deployment Preparation

### Task 14.1: PostgreSQL Configuration
- [x] Create application-prod.yml
- [x] Configure PostgreSQL datasource
- [x] Configure connection pooling (HikariCP)
- [x] Test schema generation with PostgreSQL
- [x] Verify data type compatibility

### Task 14.2: Production Readiness
- [x] Configure logging for production
- [x] Configure actuator endpoints (health, metrics)
- [x] Configure CORS if needed
- [x] Configure security headers
- [x] Review and secure sensitive configuration
- [x] Create Docker file (optional)
- [ ] Create deployment scripts (optional)

---

## Summary

This task list provides a comprehensive, ordered implementation plan for the Sales API covering:
- **Phase 1-3**: Project setup, domain model, and repositories
- **Phase 4-5**: Exception handling and DTOs
- **Phase 6-7**: External clients and service layer
- **Phase 8**: Controller layer
- **Phase 9-10**: Comprehensive unit and integration testing
- **Phase 11**: API documentation
- **Phase 12**: Code coverage and quality assurance
- **Phase 13**: Final testing and validation
- **Phase 14**: Production deployment preparation

**Total Tasks**: 200+ individual tasks organized into 14 phases

**Estimated Coverage**: 
- Unit tests: 90%+ service layer
- Integration tests: All major workflows
- Overall coverage: 80%+ as required

**Technologies**: Spring Boot 3.x, Java 17+, JPA/Hibernate, H2/PostgreSQL, JUnit 5, Mockito, WireMock, JaCoCo, SpringDoc OpenAPI
