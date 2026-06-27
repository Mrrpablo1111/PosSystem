package com.sh.sh.pos.system.payload.dto.suppliersDTO;


import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierPaymentHistoryDTO {

    private Long paymentId;

    private String paymentNumber;

    private Long invoiceId;

    private String invoiceNumber;

    private LocalDate paymentDate;

    private String paymentMethod;

    private BigDecimal amountPaid;

    private String note;
}