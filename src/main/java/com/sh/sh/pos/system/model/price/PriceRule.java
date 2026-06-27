package com.sh.sh.pos.system.model.price;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.products.ProductVariant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "price_rules", indexes  = {
        @Index(name = "idx_price_rule_product_group", columnList = "product_id, price_group_id"),
        @Index(name = "idx_price_rule_branch", columnList = "branch_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @ManyToOne
    @JoinColumn(name = "price_group_id", nullable = false)
    private PriceGroup priceGroup;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    private BigDecimal fixedPrice;
    private BigDecimal discountPercent;
    private BigDecimal markupPercent;

    @Column(nullable = false)
    @Builder.Default
    private Integer minQuantity = 1;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 1;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
    }







    
}
