package com.sh.sh.pos.system.model;

import java.time.LocalDateTime;

import com.sh.sh.pos.system.model.products.Product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Table(name = "inventories", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "branch_id", "product_id" })
}, indexes = {
		@Index(name = "idx_inventory_branch_product", columnList = "branch_id, product_id")
})
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Builder

public class Inventory {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "branch_id", nullable = false)
	private Branch branch;

	@ManyToOne
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(nullable = false)
	@Builder.Default
	private Integer quantity = 0;

	
	private Integer lowStockAlert;

	private LocalDateTime lastUpdate;

	@Column(nullable = false)
	@Builder.Default
	private Integer reservedQuantity = 0;

	@NotNull
	@Column(nullable = false)
	private Integer reorderLevel;

	@Transient
	public Integer getAvailableQuantity() {
		return Math.max(0, quantity - reservedQuantity);
	}

	@PrePersist
	@PreUpdate
	protected void onUpdate() {
		lastUpdate = LocalDateTime.now();
	}

	@Transient
	public boolean isLowStock() {
		return getAvailableQuantity() <= reorderLevel;
	}

}
