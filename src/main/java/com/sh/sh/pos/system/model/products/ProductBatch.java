package com.sh.sh.pos.system.model.products;

import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


import com.sh.sh.pos.system.domain.ExpiryStatus;
import com.sh.sh.pos.system.model.Branch;

import com.sh.sh.pos.system.model.suppliers.Supplier;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_batches", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "branch_id", "product_id", "batch_no" })
}, indexes = {
        @Index(columnList = "expiry_date")
})
public class ProductBatch {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 50)
    private String batchNo; // e.g. lot or batch identifier

    @Column(nullable = false)
    private Integer quantity; // qty in this batch

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private BigDecimal purchasePrice;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(nullable = false)
    private Integer remainingQuantity; 

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @Transient
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());

    }

    @Transient
    public long getDaysLeft() {
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    @Transient
    public ExpiryStatus getExpiryStatus() {

        long days = getDaysLeft();

        if (days < 0) {
            return ExpiryStatus.EXPIRED;
        }

        if (days <= 3) {
            return ExpiryStatus.CRITICAL;
        }

        if (days <= 10) {
            return ExpiryStatus.WARNING;
        }

        if (days <= 30) {
            return ExpiryStatus.INFO;
        }

        return ExpiryStatus.SAFE;
    }

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
