package com.sh.sh.pos.system.model.products;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private String name;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(unique = true)
    private String barcode;
    private String itemCode;

    private BigDecimal costPrice;

    private BigDecimal sellingPrice;

    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy  = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariantValue> values = new ArrayList<>();
}
