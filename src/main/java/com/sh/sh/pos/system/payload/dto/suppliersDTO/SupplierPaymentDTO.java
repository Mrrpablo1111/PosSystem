package com.sh.sh.pos.system.payload.dto.suppliersDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierPaymentDTO {

    private Long id;

    private String paymentNumber;

    private Long supplierId;

    private Long invoiceId;

    private LocalDate paymentDate;

    private String paymentMethod;

    private BigDecimal amountPaid;

    private String note;
}