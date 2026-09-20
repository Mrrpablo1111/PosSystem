package com.sh.sh.pos.system.model.goodreceipt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.sh.sh.pos.system.enums.GoodsReceiptStatus;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.purchasesorder.PurchaseOrder;
import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.model.warehouses.Warehouse;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "goods_receipts",
    uniqueConstraints = @UniqueConstraint(name = "uk_goods_receipt_grn_number", columnNames = "grn_number"),
    indexes = {
        @Index(name = "idx_grn_purchase_order", columnList = "purchase_order_id"),
        @Index(name = "idx_grn_status", columnList = "status"),
        @Index(name = "idx_grn_received_date", columnList = "received_date")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable GRN number, e.g. GRN-20260709-0001 */
    @Column(name = "grn_number", nullable = false, length = 30)
    private String grnNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GoodsReceiptStatus status = GoodsReceiptStatus.DRAFT;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by")
    private User receivedBy;

    @Column(length = 500)
    private String note;

    @OneToMany(mappedBy = "goodsReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GoodsReceiptItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Helpers ───────────────────────────────────────────────────────────

    public void addItem(GoodsReceiptItem item) {
        items.add(item);
        item.setGoodsReceipt(this);
    }

    public void clearItems() {
        items.forEach(i -> i.setGoodsReceipt(null));
        items.clear();
    }
}
