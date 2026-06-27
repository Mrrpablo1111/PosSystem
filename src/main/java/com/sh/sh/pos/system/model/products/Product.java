package com.sh.sh.pos.system.model.products;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.sh.sh.pos.system.domain.UnitType;
import com.sh.sh.pos.system.model.Category;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.suppliers.Supplier;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
@Entity
@Builder
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String sku;

	private String description;

	@Column(nullable = false)
	private BigDecimal mrp;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UnitType unit;

	@Column(nullable = false)
	private BigDecimal costPrice;

	@Column(nullable = false)
	private BigDecimal sellingPrice;

	@Column(unique = true)
	private String barcode;

	private String brand;

	private String image;

	@Column(nullable = false)
	@Builder.Default
	private Boolean active = true;

	private Double weight;

	@ManyToOne
	@JoinColumn(name = "supplier_id")
	private Supplier supplier;

	@Column(nullable = false)
	private Integer defaultReorderLevel;
	
	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category;

	@ManyToOne
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProductOption> options;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProductVariant> variants;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();

	}

	@PreUpdate
	protected void onUpdated() {
		updatedAt = LocalDateTime.now();
	}

}
