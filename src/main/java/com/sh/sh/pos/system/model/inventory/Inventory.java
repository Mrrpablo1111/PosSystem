package com.sh.sh.pos.system.model.inventory;

import java.time.LocalDateTime;

import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductVariant;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor        
@AllArgsConstructor
@Table(
    name = "inventories",
    uniqueConstraints = {
        // One stock row per product(+variant) per WAREHOUSE.
        // The old (branch_id, product_id) constraint would reject the same
        // product stocked in a branch's second warehouse.
        @UniqueConstraint(
            name = "uk_inventory_warehouse_product_variant",
            columnNames = { "warehouse_id", "product_id", "product_variant_id" })
    },
    indexes = {
        @Index(name = "idx_inventory_branch_product", columnList = "branch_id, product_id"),
        @Index(name = "idx_inventory_warehouse", columnList = "warehouse_id")
    }
)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // was AUTO — same fix you applied elsewhere
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    private Integer lowStockAlert;

    private LocalDateTime lastUpdate;

    @Column(nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    /**
     * @Builder.Default matters: rows created by the goods-receipt
     * find-or-create path don't set this, and the column is NOT NULL —
     * without the default every first receipt of a new product would fail.
     */
    @NotNull
    @Column(nullable = false)
    @Builder.Default
    private Integer reorderLevel = 0;

    @Transient
    public Integer getAvailableQuantity() {
        return Math.max(0, quantity - reservedQuantity);
    }

    @Transient
    public boolean isLowStock() {
        return getAvailableQuantity() <= reorderLevel;
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdate = LocalDateTime.now();
    }
}