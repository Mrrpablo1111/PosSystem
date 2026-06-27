package com.sh.sh.pos.system.model.products;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_option_values",  uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"option_id", "value"}
        )
    })
@Getter
@Setter
public class ProductOptionValue {
     @Id
    @GeneratedValue
    private Long id;

    private String value;

    @ManyToOne
    private ProductOption option;
}
