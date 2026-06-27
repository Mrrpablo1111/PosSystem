package com.sh.sh.pos.system.model;

import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.MovementType;
import com.sh.sh.pos.system.model.products.Product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_movements", indexes = {
    @Index(columnList = "branch_id, product_id, created_at")
})
public class InventoryMovement {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;    // positive (incoming) or negative (outgoing)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type;   // e.g. PURCHASE, SALE, RETURN, DAMAGE, TRANSFER_IN, TRANSFER_OUT, ADJUSTMENT

    @Column(length = 255)
    private String note;         // optional remark (e.g. supplier name, sale ID)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}

