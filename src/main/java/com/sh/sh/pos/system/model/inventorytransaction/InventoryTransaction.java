package com.sh.sh.pos.system.model.inventorytransaction;



import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.model.warehouses.Warehouse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Immutable stock-movement ledger. One row per movement:
 * positive quantity = stock in, negative quantity = stock out.
 *
 * Rows are only ever INSERTED by business operations (goods receipt, sale,
 * return, adjustment, transfer) — never updated or deleted. Reversals are
 * new rows with the opposite sign referencing the same document.
 */
@Entity
@Table(
    name = "inventory_transactions",
    indexes = {
        @Index(name = "idx_invtxn_product",   columnList = "product_id"),
        @Index(name = "idx_invtxn_warehouse", columnList = "warehouse_id"),
        @Index(name = "idx_invtxn_reference", columnList = "reference_type, reference_id"),
        @Index(name = "idx_invtxn_created",   columnList = "created_at")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransaction {

    // Reference-type constants — keep in sync with every module that writes here
    public static final String REF_GOODS_RECEIPT = "GOODS_RECEIPT";
    public static final String REF_SALE          = "SALE";
    public static final String REF_SALE_RETURN   = "SALE_RETURN";
    public static final String REF_ADJUSTMENT    = "ADJUSTMENT";
    public static final String REF_TRANSFER      = "TRANSFER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    /** Positive = stock in, negative = stock out */
    @Column(nullable = false)
    private Integer quantity;

    /** Cost per unit at movement time — enables stock valuation reports */
    @Column(name = "unit_cost", precision = 19, scale = 4)
    private BigDecimal unitCost;

    /** Source document type, e.g. GOODS_RECEIPT / SALE / ADJUSTMENT */
    @Column(name = "reference_type", nullable = false, length = 30)
    private String referenceType;

    /** Id of the source document (GRN id, order id, …) */
    @Column(name = "reference_id")
    private Long referenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;

    @Column(length = 500)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
