package com.sh.sh.pos.system.model.purchaseInvoiceItems;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.model.purchaseInvoices.PurchaseInvoice;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "purchase_invoice_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseInvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference
    private PurchaseInvoice purchaseInvoice;

    @ManyToOne
    private Product product;

    @ManyToOne
    private ProductVariant variant;

    private Integer quantity;

    private BigDecimal unitCost;

    private BigDecimal totalCost;

    private String batchNo;

    private LocalDate expiryDate;
}