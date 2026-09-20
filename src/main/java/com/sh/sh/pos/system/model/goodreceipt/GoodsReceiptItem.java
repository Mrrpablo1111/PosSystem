package com.sh.sh.pos.system.model.goodreceipt;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductVariant;
import com.sh.sh.pos.system.model.purchasesorderitem.PurchaseOrderItem;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "goods_receipt_items",
    indexes = {
        @Index(name = "idx_gri_goods_receipt", columnList = "goods_receipt_id"),
        @Index(name = "idx_gri_po_item", columnList = "purchase_order_item_id")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goods_receipt_id", nullable = false)
    private GoodsReceipt goodsReceipt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_item_id", nullable = false)
    private PurchaseOrderItem purchaseOrderItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    /** Snapshot of PO item requested quantity at receipt time */
    @Column(name = "requested_quantity", nullable = false)
    private Integer requestedQuantity;

    /** Physically delivered in THIS receipt */
    @Column(name = "received_quantity", nullable = false)
    private Integer receivedQuantity;

    /** Passed inspection → goes into stock. accepted + damaged + rejected = received */
    @Column(name = "accepted_quantity", nullable = false)
    private Integer acceptedQuantity;

    @Column(name = "damaged_quantity", nullable = false)
    @Builder.Default
    private Integer damagedQuantity = 0;

    @Column(name = "rejected_quantity", nullable = false)
    @Builder.Default
    private Integer rejectedQuantity = 0;

    /** Snapshot of PO item unit cost at receipt time */
    @Column(name = "unit_cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitCost;

    @Column(name = "batch_number", length = 60)
    private String batchNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(length = 500)
    private String note;
}
