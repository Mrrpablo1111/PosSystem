package com.sh.sh.pos.system.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.ReferenceType;
import com.sh.sh.pos.system.domain.StockMovementType;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductBatch;
import com.sh.sh.pos.system.model.products.ProductVariant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Table(name = "stock_movements", indexes = {
        @Index(name = "idx_stock_branch_product", columnList = "branch_id, product_id"),
        @Index(name = "idx_stock_variant", columnList = "variant_id"),
        @Index(name = "idx_stock_batch", columnList = "batch_id"),
        @Index(name = "idx_stock_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ProductBatch batch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StockMovementType movementType;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferenceType referenceType;

    @Column(nullable = false)
    private BigDecimal unitCost;

    @Column(nullable = false)
    private BigDecimal totalCost;

    private Long referenceId;

    @Column(length = 255)
    private String note;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
