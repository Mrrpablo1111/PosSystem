package com.sh.sh.pos.system.model.suppliers;



import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.PaymentMethod;
import com.sh.sh.pos.system.model.purchaseInvoices.PurchaseInvoice;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "supplier_payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentNumber;

    @ManyToOne
    private Supplier supplier;

    @ManyToOne
    private PurchaseInvoice purchaseInvoice;

    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private BigDecimal amountPaid;

    private String note;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}