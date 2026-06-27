package com.sh.sh.pos.system.model.purchasesOrderItem;

import java.math.BigDecimal;

import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.model.purchasesOrder.PurchaseOrder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "purchase_order_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relations ─────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Optional — only set when the product has variants (size, color, etc.)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = true)
    private ProductVariant variant;

    // ── Quantity tracking ─────────────────────────────────────────────────

    /**
     * How many units were ordered from the supplier.
     * Set at PO creation and never changes.
     */
    @Column(nullable = false)
    private Integer requestedQuantity;

    /**
     * How many units have actually arrived so far.
     * Starts at 0. Incremented each time a receive action is performed.
     * Supports partial deliveries — can be updated multiple times.
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer receivedQuantity = 0;

    /**
     * requestedQuantity - receivedQuantity.
     * Stored for easy querying (e.g. "find all POs with outstanding items").
     * Always kept in sync via syncRemaining().
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer remainingQuantity = 0;

    // ── Pricing ───────────────────────────────────────────────────────────

    /**
     * Cost per unit agreed with supplier.
     */
    @Column(nullable = false)
    private BigDecimal unitCost;

    /**
     * Discount percentage for this line item (0–100).
     * Defaults to 0 if no discount.
     */
    @Builder.Default
    @Column(nullable = false)
    private BigDecimal discountPct = BigDecimal.ZERO;

    /**
     * unitCost × requestedQuantity × (1 - discountPct / 100)
     * Computed in the mapper and stored for reporting.
     */
    @Column(nullable = false)
    private BigDecimal totalCost;

    // ── Notes ─────────────────────────────────────────────────────────────

    /**
     * Optional per-line note e.g. "check expiry date", "fragile".
     * Also used to record damage notes when receiving goods.
     */
    @Column(nullable = true, length = 300)
    private String note;

    // ── Helpers ───────────────────────────────────────────────────────────

    /**
     * Call this every time receivedQuantity is updated
     * to keep remainingQuantity in sync.
     */
    public void syncRemaining() {
        if (requestedQuantity != null && receivedQuantity != null) {
            this.remainingQuantity = Math.max(0, requestedQuantity - receivedQuantity);
        }
    }

    /**
     * True only when this line has been fully received.
     * Used by the PO to compute fullyReceived and promote status to RECEIVED.
     */
    public boolean isFullyReceived() {
        return remainingQuantity != null && remainingQuantity == 0;
    }
}