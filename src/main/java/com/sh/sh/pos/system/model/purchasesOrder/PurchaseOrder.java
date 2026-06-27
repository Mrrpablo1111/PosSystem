package com.sh.sh.pos.system.model.purchasesOrder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sh.sh.pos.system.domain.PurchaseOrderStatus;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.currencies.Currency;
import com.sh.sh.pos.system.model.purchaseInvoices.PurchaseInvoice;
import com.sh.sh.pos.system.model.purchasesOrderItem.PurchaseOrderItem;
import com.sh.sh.pos.system.model.suppliers.Supplier;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "purchase_orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "purchaseOrder")
    @JsonManagedReference
    private List<PurchaseInvoice> purchaseInvoices;

    @Column(unique = true)
    private String poNumber;

    @Column(nullable = true)
    private String referenceNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = true)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseOrderStatus status;

    @Column(nullable = false)
    private LocalDate orderDate;

    @Column(nullable = true)
    private LocalDate expectedDelivery;

    @Column(nullable = true)
    private LocalDate receivedDate;

    private LocalDate paymentDueDate;

    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal shippingCost;
    private BigDecimal totalAmount;

     private Integer paymentTermDays;

     @Column(nullable = true)
    private String carrierId;
    @Column(nullable = true, length = 100)
    private String trackingNo;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = true)
    private LocalDateTime sentToSupplierAt;

    @Column(nullable = true)
    private LocalDateTime receiptConfirmationSentAt;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> items;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = PurchaseOrderStatus.DRAFT;
        computePaymentDueDate();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
        computePaymentDueDate();
    }

      private void computePaymentDueDate() {
        if (orderDate != null && paymentTermDays != null) {
            paymentDueDate = orderDate.plusDays(paymentTermDays);
        }
    }
}