package com.sh.sh.pos.system.model.products;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_options", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "product_id", "name" })
})
@Getter
@Setter
public class ProductOption {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    private Product product;

    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOptionValue> values;
}
