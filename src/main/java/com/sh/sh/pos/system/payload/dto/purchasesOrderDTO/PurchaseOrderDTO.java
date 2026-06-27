package com.sh.sh.pos.system.payload.dto.purchasesOrderDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.sh.sh.pos.system.payload.dto.currenciesDTO.CurrencyDTO;
import com.sh.sh.pos.system.payload.dto.purchaseInvoicesDTO.PurchaseInvoiceSummaryDTO;
import com.sh.sh.pos.system.payload.dto.purchasesOrderItemDTO.PurchaseOrderItemDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDTO {

    private Long id;
    private Long supplierId;
    private String supplierName;
    private Long branchId;
    private String branchName;
 
    private CurrencyDTO currency;
 
    private String poNumber;
    private String referenceNo;
    private String status;
 
    private LocalDate orderDate;
    private LocalDate expectedDelivery;
    private LocalDate receivedDate;
    private LocalDate paymentDueDate;
 
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal shippingCost;
    private BigDecimal totalAmount;
 
    private Integer paymentTermDays;
    private String carrierId;
    private String trackingNo;
 
    private String notes;
 
    private boolean fullyReceived;
    private boolean overdue;
 
    private LocalDateTime sentToSupplierAt;
    private LocalDateTime receiptConfirmationSentAt;
    private LocalDateTime createdAt;
 
    private List<PurchaseOrderItemDTO> items;
    private List<PurchaseInvoiceSummaryDTO> invoices;
}