package com.sh.sh.pos.system.payload.dto.purchaseInvoicesDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.sh.sh.pos.system.payload.dto.purchaseInvoiceItemsDTO.PurchaseInvoiceItemDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseInvoiceDTO {

    private Long id;

    @NotNull(message = "Store is required")
    private Long storeId;

    @NotNull(message = "Supplier is required")
    private Long supplierId;

    @NotNull(message = "Branch is required")
    private Long branchId;
    private Long purchaseOrderId;
    private String supplierInvoiceNo;
    private String invoiceNumber;

    @NotNull(message = "Invoice date is required")
    private LocalDate invoiceDate;

    private String status;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal totalAmount;

    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;

    private String paymentStatus;

    @NotNull(message = "At least one item is required")
    @Valid
    private List<PurchaseInvoiceItemDTO> items;

}