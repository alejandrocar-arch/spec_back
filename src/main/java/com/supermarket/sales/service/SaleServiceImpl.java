package com.supermarket.sales.service;

import com.supermarket.sales.dto.external.CustomerDTO;
import com.supermarket.sales.dto.external.ProductDTO;
import com.supermarket.sales.dto.request.*;
import com.supermarket.sales.dto.response.*;
import com.supermarket.sales.entity.Sale;
import com.supermarket.sales.entity.SaleItem;
import com.supermarket.sales.enums.CreditStatus;
import com.supermarket.sales.enums.PaymentType;
import com.supermarket.sales.enums.SaleStatus;
import com.supermarket.sales.exception.*;
import com.supermarket.sales.mapper.SaleMapper;
import com.supermarket.sales.repository.SaleItemRepository;
import com.supermarket.sales.repository.SaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of SaleService for managing sales lifecycle operations.
 */
@Service
@Transactional
public class SaleServiceImpl implements SaleService {
    
    private static final Logger logger = LoggerFactory.getLogger(SaleServiceImpl.class);
    
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductService productService;
    private final CustomerService customerService;
    private final CalculationService calculationService;
    private final SaleMapper saleMapper;
    
    @Value("${sales.tax-rate:0.19}")
    private BigDecimal taxRate;
    
    @Value("${sales.store-name:Supermarket XYZ}")
    private String storeName;
    
    public SaleServiceImpl(SaleRepository saleRepository,
                          SaleItemRepository saleItemRepository,
                          ProductService productService,
                          CustomerService customerService,
                          CalculationService calculationService,
                          SaleMapper saleMapper) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.productService = productService;
        this.customerService = customerService;
        this.calculationService = calculationService;
        this.saleMapper = saleMapper;
    }
    
    @Override
    public SaleDTO createSale(CreateSaleRequest request) {
        logger.info("Creating new sale for terminal: {}, cashier: {}", 
                   request.getTerminalId(), request.getCashierId());
        
        Sale sale = new Sale();
        sale.setTerminalId(request.getTerminalId());
        sale.setCashierId(request.getCashierId());
        sale.setCustomerId(request.getCustomerId());
        sale.setStatus(SaleStatus.ACTIVE);
        
        Sale savedSale = saleRepository.save(sale);
        logger.info("Created sale with ID: {}", savedSale.getId());
        
        return saleMapper.toDTO(savedSale);
    }
    
    @Override
    @Transactional(readOnly = true)
    public SaleDTO getSaleById(Long saleId) {
        logger.debug("Retrieving sale with ID: {}", saleId);
        
        Sale sale = findSaleById(saleId);
        return saleMapper.toDTO(sale);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SaleDTO> getSalesByTerminal(String terminalId, SaleStatus status, 
                                           LocalDate startDate, LocalDate endDate) {
        logger.debug("Retrieving sales for terminal: {}, status: {}", terminalId, status);
        
        List<Sale> sales;
        
        if (status != null && startDate != null && endDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            sales = saleRepository.findByTerminalIdAndStatusAndCreatedAtBetween(
                    terminalId, status, startDateTime, endDateTime);
        } else if (status != null) {
            sales = saleRepository.findByTerminalIdAndStatus(terminalId, status);
        } else {
            sales = saleRepository.findByTerminalIdOrderByCreatedAtDesc(terminalId);
        }
        
        return sales.stream()
                .map(saleMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public SaleDTO addItemByProductId(Long saleId, Long productId, Integer quantity) {
        logger.info("Adding item to sale {}: productId={}, quantity={}", 
                   saleId, productId, quantity);
        
        Sale sale = findSaleById(saleId);
        validateSaleIsActive(sale);
        
        // Get product details from Product API
        ProductDTO product = productService.getById(productId);
        
        // Validate stock availability
        productService.validateStockAvailability(productId, quantity);
        
        // Check if product already exists in sale
        Optional<SaleItem> existingItem = sale.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Increment quantity of existing item
            SaleItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            productService.validateStockAvailability(productId, newQuantity);
            item.updateQuantity(newQuantity);
            logger.info("Updated existing item quantity to: {}", newQuantity);
        } else {
            // Add new item
            SaleItem newItem = new SaleItem();
            newItem.setProductId(product.getId());
            newItem.setProductName(product.getName());
            newItem.setBarcode(product.getBarcode());
            newItem.setUnitPrice(product.getUnitPrice());
            newItem.setQuantity(quantity);
            newItem.calculateLineTotal();
            sale.addItem(newItem);
            logger.info("Added new item to sale");
        }
        
        // Recalculate sale totals
        calculationService.recalculateSaleTotals(sale);
        
        Sale savedSale = saleRepository.save(sale);
        return saleMapper.toDTO(savedSale);
    }
    
    @Override
    public SaleDTO addItemByBarcode(Long saleId, String barcode, Integer quantity) {
        logger.info("Adding item to sale {} by barcode: {}, quantity={}", 
                   saleId, barcode, quantity);
        
        Sale sale = findSaleById(saleId);
        validateSaleIsActive(sale);
        
        // Get product details from Product API by barcode
        ProductDTO product = productService.searchByBarcode(barcode);
        
        // Validate stock availability
        productService.validateStockAvailability(product.getId(), quantity);
        
        // Check if product already exists in sale
        Optional<SaleItem> existingItem = sale.getItems().stream()
                .filter(item -> item.getProductId().equals(product.getId()))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Increment quantity of existing item
            SaleItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            productService.validateStockAvailability(product.getId(), newQuantity);
            item.updateQuantity(newQuantity);
            logger.info("Updated existing item quantity to: {}", newQuantity);
        } else {
            // Add new item
            SaleItem newItem = new SaleItem();
            newItem.setProductId(product.getId());
            newItem.setProductName(product.getName());
            newItem.setBarcode(product.getBarcode());
            newItem.setUnitPrice(product.getUnitPrice());
            newItem.setQuantity(quantity);
            newItem.calculateLineTotal();
            sale.addItem(newItem);
            logger.info("Added new item to sale");
        }
        
        // Recalculate sale totals
        calculationService.recalculateSaleTotals(sale);
        
        Sale savedSale = saleRepository.save(sale);
        return saleMapper.toDTO(savedSale);
    }
    
    @Override
    public SaleDTO updateItemQuantity(Long saleId, Long itemId, Integer quantity) {
        logger.info("Updating item {} quantity in sale {} to: {}", 
                   itemId, saleId, quantity);
        
        Sale sale = findSaleById(saleId);
        validateSaleIsActive(sale);
        
        SaleItem item = findSaleItem(sale, itemId);
        
        // Validate stock availability for new quantity
        productService.validateStockAvailability(item.getProductId(), quantity);
        
        // Update quantity and recalculate line total
        item.updateQuantity(quantity);
        
        // Recalculate sale totals
        calculationService.recalculateSaleTotals(sale);
        
        Sale savedSale = saleRepository.save(sale);
        return saleMapper.toDTO(savedSale);
    }
    
    @Override
    public SaleDTO removeItem(Long saleId, Long itemId) {
        logger.info("Removing item {} from sale {}", itemId, saleId);
        
        Sale sale = findSaleById(saleId);
        validateSaleIsActive(sale);
        
        SaleItem item = findSaleItem(sale, itemId);
        
        // Remove item from sale
        sale.removeItem(item);
        
        // Recalculate sale totals
        calculationService.recalculateSaleTotals(sale);
        
        Sale savedSale = saleRepository.save(sale);
        return saleMapper.toDTO(savedSale);
    }
    
    @Override
    public SaleDTO applyDiscount(Long saleId, ApplyDiscountRequest request) {
        logger.info("Applying discount to sale {}: type={}, value={}", 
                   saleId, request.getDiscountType(), request.getDiscountValue());
        
        Sale sale = findSaleById(saleId);
        validateSaleIsActive(sale);
        
        // Validate discount value
        validateDiscount(request.getDiscountType(), request.getDiscountValue(), sale.getSubtotal());
        
        // Apply discount
        sale.setDiscountType(request.getDiscountType());
        sale.setDiscountValue(request.getDiscountValue());
        
        // Recalculate sale totals
        calculationService.recalculateSaleTotals(sale);
        
        Sale savedSale = saleRepository.save(sale);
        logger.info("Discount applied successfully");
        
        return saleMapper.toDTO(savedSale);
    }
    
    // Helper methods
    
    private Sale findSaleById(Long saleId) {
        return saleRepository.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException("Sale not found with ID: " + saleId));
    }
    
    private SaleItem findSaleItem(Sale sale, Long itemId) {
        return sale.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new SaleItemNotFoundException("Item not found in sale: " + itemId));
    }
    
    private void validateSaleIsActive(Sale sale) {
        if (!sale.canAddItems()) {
            throw new SaleNotActiveException("Sale is not active");
        }
    }
    
    private void validateDiscount(com.supermarket.sales.enums.DiscountType type, 
                                  BigDecimal value, BigDecimal subtotal) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDiscountException("Discount amount cannot be negative");
        }
        
        if (type == com.supermarket.sales.enums.DiscountType.PERCENTAGE) {
            if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(new BigDecimal("100")) > 0) {
                throw new InvalidDiscountException("Discount percentage must be between 0 and 100");
            }
        } else if (type == com.supermarket.sales.enums.DiscountType.FIXED_AMOUNT) {
            BigDecimal tax = calculationService.calculateTax(subtotal, taxRate);
            BigDecimal totalBeforeDiscount = subtotal.add(tax);
            if (value.compareTo(totalBeforeDiscount) > 0) {
                throw new InvalidDiscountException("Discount cannot exceed sale total");
            }
        }
    }
}

    @Override
    public CheckoutResponse checkout(Long saleId, CheckoutRequest request) {
        logger.info("Processing checkout for sale {}: paymentType={}", 
                   saleId, request.getPaymentType());
        
        Sale sale = findSaleById(saleId);
        
        // Validate sale can be checked out
        if (!sale.canCheckout()) {
            if (sale.getItems().isEmpty()) {
                throw new InvalidPaymentException("Cannot checkout empty sale");
            }
            throw new SaleNotActiveException("Sale is not active");
        }
        
        // Validate payment details based on payment type
        if (request.getPaymentType() == PaymentType.CASH) {
            validateCashPayment(request, sale);
        } else if (request.getPaymentType() == PaymentType.CREDIT) {
            validateCreditPayment(request, sale);
        }
        
        // Validate and decrement stock for all items
        for (SaleItem item : sale.getItems()) {
            productService.validateStockAvailability(item.getProductId(), item.getQuantity());
        }
        
        // Decrement stock for all items
        for (SaleItem item : sale.getItems()) {
            productService.decrementStock(item.getProductId(), item.getQuantity());
        }
        
        // Process payment
        if (request.getPaymentType() == PaymentType.CASH) {
            processCashPayment(sale, request.getAmountReceived());
        } else {
            processCreditPayment(sale, request.getCustomerId());
        }
        
        // Complete the sale
        sale.complete(request.getPaymentType());
        sale.setTransactionId(generateTransactionId());
        
        Sale savedSale = saleRepository.save(sale);
        logger.info("Checkout completed successfully. Transaction ID: {}", savedSale.getTransactionId());
        
        // Generate receipt
        ReceiptDTO receipt = generateReceipt(savedSale);
        
        CheckoutResponse response = new CheckoutResponse();
        response.setSale(saleMapper.toDTO(savedSale));
        response.setReceipt(receipt);
        
        return response;
    }
    
    @Override
    public SaleDTO freezeSale(Long saleId) {
        logger.info("Freezing sale: {}", saleId);
        
        Sale sale = findSaleById(saleId);
        sale.freeze();
        
        Sale savedSale = saleRepository.save(sale);
        logger.info("Sale frozen successfully");
        
        return saleMapper.toDTO(savedSale);
    }
    
    @Override
    public SaleDTO resumeSale(Long saleId) {
        logger.info("Resuming sale: {}", saleId);
        
        Sale sale = findSaleById(saleId);
        
        if (!sale.canResume()) {
            throw new SaleNotFrozenException("Only frozen sales can be resumed");
        }
        
        sale.resume();
        
        Sale savedSale = saleRepository.save(sale);
        logger.info("Sale resumed successfully");
        
        return saleMapper.toDTO(savedSale);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<FrozenSaleDTO> getFrozenSalesByTerminal(String terminalId) {
        logger.debug("Retrieving frozen sales for terminal: {}", terminalId);
        
        List<Sale> frozenSales = saleRepository.findFrozenSalesByTerminal(terminalId);
        
        return frozenSales.stream()
                .map(this::toFrozenSaleDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public SaleDTO cancelSale(Long saleId, String reason) {
        logger.info("Cancelling sale {}: reason={}", saleId, reason);
        
        Sale sale = findSaleById(saleId);
        
        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new SaleAlreadyCancelledException("Sale is already cancelled");
        }
        
        if (sale.getStatus() == SaleStatus.COMPLETED || 
            sale.getStatus() == SaleStatus.RETURNED || 
            sale.getStatus() == SaleStatus.PARTIALLY_RETURNED) {
            throw new BusinessException("Cannot cancel completed or returned sale");
        }
        
        sale.cancel(reason);
        
        Sale savedSale = saleRepository.save(sale);
        logger.info("Sale cancelled successfully");
        
        return saleMapper.toDTO(savedSale);
    }
    
    @Override
    public ReturnResponse processFullReturn(Long saleId, FullReturnRequest request) {
        logger.info("Processing full return for sale {}: reason={}", saleId, request.getReturnReason());
        
        Sale sale = findSaleById(saleId);
        
        // Validate sale can be returned
        if (sale.getStatus() != SaleStatus.COMPLETED) {
            throw new BusinessException("Only completed sales can be returned");
        }
        
        if (sale.getStatus() == SaleStatus.RETURNED) {
            throw new SaleAlreadyReturnedException("Sale has already been fully returned");
        }
        
        // Increment stock for all items
        for (SaleItem item : sale.getItems()) {
            int quantityToReturn = item.getAvailableForReturn();
            if (quantityToReturn > 0) {
                productService.incrementStock(item.getProductId(), quantityToReturn);
                item.incrementReturnedQuantity(quantityToReturn);
            }
        }
        
        // Mark sale as returned
        sale.setReturnReason(request.getReturnReason());
        sale.markAsReturned();
        
        Sale savedSale = saleRepository.save(sale);
        logger.info("Full return processed successfully");
        
        // Generate return receipt
        ReturnReceiptDTO returnReceipt = generateReturnReceipt(savedSale, savedSale.getItems());
        
        // Generate credit note if credit sale
        CreditNoteDTO creditNote = null;
        if (savedSale.getPaymentType() == PaymentType.CREDIT) {
            creditNote = generateCreditNote(savedSale, savedSale.getItems());
        }
        
        ReturnResponse response = new ReturnResponse();
        response.setSale(saleMapper.toDTO(savedSale));
        response.setReturnReceipt(returnReceipt);
        response.setCreditNote(creditNote);
        
        return response;
    }
    
    @Override
    public ReturnResponse processPartialReturn(Long saleId, PartialReturnRequest request) {
        logger.info("Processing partial return for sale {}: reason={}", saleId, request.getReturnReason());
        
        Sale sale = findSaleById(saleId);
        
        // Validate sale can be returned
        if (sale.getStatus() != SaleStatus.COMPLETED && sale.getStatus() != SaleStatus.PARTIALLY_RETURNED) {
            throw new BusinessException("Only completed sales can be returned");
        }
        
        if (sale.getStatus() == SaleStatus.RETURNED) {
            throw new SaleAlreadyReturnedException("Sale has already been fully returned");
        }
        
        // Validate and process return items
        List<SaleItem> returnedItems = new ArrayList<>();
        for (ReturnItemRequest returnItem : request.getReturnItems()) {
            SaleItem item = findSaleItem(sale, returnItem.getItemId());
            
            // Validate return quantity
            int availableForReturn = item.getAvailableForReturn();
            if (returnItem.getQuantity() > availableForReturn) {
                throw new InvalidReturnQuantityException(
                    "Return quantity exceeds purchased quantity for item: " + item.getProductName());
            }
            
            // Increment stock
            productService.incrementStock(item.getProductId(), returnItem.getQuantity());
            
            // Update returned quantity
            item.incrementReturnedQuantity(returnItem.getQuantity());
            returnedItems.add(item);
        }
        
        // Check if all items are now fully returned
        boolean allItemsReturned = sale.getItems().stream()
                .allMatch(item -> item.getAvailableForReturn() == 0);
        
        // Update sale status
        sale.setReturnReason(request.getReturnReason());
        if (allItemsReturned) {
            sale.markAsReturned();
        } else {
            sale.markAsPartiallyReturned();
        }
        
        Sale savedSale = saleRepository.save(sale);
        logger.info("Partial return processed successfully");
        
        // Generate return receipt for returned items only
        ReturnReceiptDTO returnReceipt = generateReturnReceipt(savedSale, returnedItems);
        
        // Generate credit note if credit sale
        CreditNoteDTO creditNote = null;
        if (savedSale.getPaymentType() == PaymentType.CREDIT) {
            creditNote = generateCreditNote(savedSale, returnedItems);
        }
        
        ReturnResponse response = new ReturnResponse();
        response.setSale(saleMapper.toDTO(savedSale));
        response.setReturnReceipt(returnReceipt);
        response.setCreditNote(creditNote);
        
        return response;
    }
    
    // Private helper methods for checkout
    
    private void validateCashPayment(CheckoutRequest request, Sale sale) {
        if (request.getAmountReceived() == null) {
            throw new InvalidPaymentException("Amount received is required for cash payments");
        }
        
        if (request.getAmountReceived().compareTo(sale.getTotal()) < 0) {
            throw new InvalidPaymentException("Amount received is insufficient");
        }
    }
    
    private void validateCreditPayment(CheckoutRequest request, Sale sale) {
        Long customerId = request.getCustomerId() != null ? request.getCustomerId() : sale.getCustomerId();
        
        if (customerId == null) {
            throw new CustomerRequiredException("Customer required for credit sales");
        }
        
        // Validate customer credit status
        customerService.validateCreditApproval(customerId);
    }
    
    private void processCashPayment(Sale sale, BigDecimal amountReceived) {
        sale.setAmountReceived(amountReceived);
        BigDecimal change = calculationService.calculateChange(sale.getTotal(), amountReceived);
        sale.setChange(change);
    }
    
    private void processCreditPayment(Sale sale, Long customerId) {
        if (customerId != null) {
            sale.setCustomerId(customerId);
        }
        sale.setCreditReferenceNumber(generateCreditReferenceNumber());
    }
    
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String generateCreditReferenceNumber() {
        return "CRD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private ReceiptDTO generateReceipt(Sale sale) {
        ReceiptDTO receipt = new ReceiptDTO();
        receipt.setStoreName(storeName);
        receipt.setTerminalId(sale.getTerminalId());
        receipt.setCashierId(sale.getCashierId());
        receipt.setTransactionId(sale.getTransactionId());
        receipt.setTimestamp(sale.getCompletedAt());
        
        // Add customer info if present
        if (sale.getCustomerId() != null) {
            try {
                CustomerDTO customer = customerService.getById(sale.getCustomerId());
                receipt.setCustomerInfo(customer);
            } catch (Exception e) {
                logger.warn("Could not retrieve customer info for receipt: {}", e.getMessage());
            }
        }
        
        // Add items
        List<ReceiptItemDTO> receiptItems = sale.getItems().stream()
                .map(this::toReceiptItemDTO)
                .collect(Collectors.toList());
        receipt.setItems(receiptItems);
        
        // Add totals
        receipt.setSubtotal(sale.getSubtotal());
        receipt.setTax(sale.getTax());
        receipt.setDiscount(sale.getDiscount());
        receipt.setTotal(sale.getTotal());
        receipt.setPaymentType(sale.getPaymentType());
        
        // Add payment-specific details
        if (sale.getPaymentType() == PaymentType.CASH) {
            receipt.setAmountReceived(sale.getAmountReceived());
            receipt.setChange(sale.getChange());
        } else if (sale.getPaymentType() == PaymentType.CREDIT) {
            receipt.setCreditReferenceNumber(sale.getCreditReferenceNumber());
        }
        
        return receipt;
    }
    
    private ReturnReceiptDTO generateReturnReceipt(Sale sale, List<SaleItem> returnedItems) {
        ReturnReceiptDTO returnReceipt = new ReturnReceiptDTO();
        returnReceipt.setOriginalTransactionId(sale.getTransactionId());
        returnReceipt.setReturnTimestamp(sale.getReturnedAt());
        returnReceipt.setReturnReason(sale.getReturnReason());
        
        // Calculate total refund amount
        BigDecimal totalRefund = returnedItems.stream()
                .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getReturnedQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        returnReceipt.setTotalRefundAmount(totalRefund);
        
        // Add returned items
        List<ReturnedItemDTO> returnedItemDTOs = returnedItems.stream()
                .map(this::toReturnedItemDTO)
                .collect(Collectors.toList());
        returnReceipt.setReturnedItems(returnedItemDTOs);
        
        return returnReceipt;
    }
    
    private CreditNoteDTO generateCreditNote(Sale sale, List<SaleItem> returnedItems) {
        CreditNoteDTO creditNote = new CreditNoteDTO();
        creditNote.setOriginalCreditReference(sale.getCreditReferenceNumber());
        creditNote.setCreditNoteNumber("CN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        creditNote.setIssueDate(LocalDateTime.now());
        
        // Calculate credit amount
        BigDecimal creditAmount = returnedItems.stream()
                .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getReturnedQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        creditNote.setTotalCreditAmount(creditAmount);
        
        return creditNote;
    }
    
    private FrozenSaleDTO toFrozenSaleDTO(Sale sale) {
        FrozenSaleDTO dto = new FrozenSaleDTO();
        dto.setSaleId(sale.getId());
        dto.setFrozenAt(sale.getFrozenAt());
        dto.setItemCount(sale.getItems().size());
        dto.setTotal(sale.getTotal());
        return dto;
    }
    
    private ReceiptItemDTO toReceiptItemDTO(SaleItem item) {
        ReceiptItemDTO dto = new ReceiptItemDTO();
        dto.setProductName(item.getProductName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setLineTotal(item.getLineTotal());
        return dto;
    }
    
    private ReturnedItemDTO toReturnedItemDTO(SaleItem item) {
        ReturnedItemDTO dto = new ReturnedItemDTO();
        dto.setProductName(item.getProductName());
        dto.setQuantity(item.getReturnedQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setRefundAmount(item.getUnitPrice().multiply(new BigDecimal(item.getReturnedQuantity())));
        return dto;
    }
}
