package com.sh.sh.pos.system.model.products;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_variant_values")
@Getter
@Setter

public class ProductVariantValue {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private ProductVariant variant;

    @ManyToOne
    private ProductOptionValue optionValue;

}
