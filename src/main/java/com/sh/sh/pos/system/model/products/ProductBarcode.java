package com.sh.sh.pos.system.model.products;

import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.BarcodeSource;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
 * Barcode registry: a product (or one of its variants) can carry MANY
 * barcodes — the generated in-store EAN-13, the supplier's printed code,
 * a case/pack barcode, etc. The POS register resolves any of them to the
 * same sellable item via the lookup endpoint.
 */
@Entity
@Table(name = "product_barcodes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBarcode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Null for simple products; set when the barcode targets a variant. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant variant;

    @Column(nullable = false, unique = true, length = 64)
    private String barcode;

    /** Origin/standard of this barcode. */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private BarcodeSource source = BarcodeSource.INTERNAL;

    /** Free note: "supplier box code", "6-pack", … */
    @Column(length = 120)
    private String note;

    /** The barcode printed on shelf labels for this item. */
    @Builder.Default
    @Column(name = "is_primary", nullable = false)
    private Boolean primary = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
