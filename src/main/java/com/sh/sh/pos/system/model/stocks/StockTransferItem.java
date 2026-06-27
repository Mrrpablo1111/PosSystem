package com.sh.sh.pos.system.model.stocks;

import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductBatch;
import com.sh.sh.pos.system.model.products.ProductVariant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "stock_transfer_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransferItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "transfer_id", nullable = false)
    private StockTransfer transfer;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @ManyToOne
    @JoinColumn(name = "batch_id", nullable = false)
    private ProductBatch batch;

    @Column(nullable = false)
    private Integer requestedQty;

    @Column(nullable = false)
    private Integer shippedQty;

    @Builder.Default
    private Integer receivedQty = 0;

    @Builder.Default
    private Integer damagedQty = 0;

    @Builder.Default
    private Integer missingQty = 0;
}
