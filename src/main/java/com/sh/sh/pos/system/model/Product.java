package com.sh.sh.pos.system.model;

import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.UnitType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
	private Double mrp;
	
	
    @Column(nullable = false)
	private Double sellingPrice;
	private String brand;
	 
	private String image;

	
	
	@ManyToOne 
	private Category category;  
	
	@ManyToOne
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;
	
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		
	}
	
	@PreUpdate
	protected void onUpdated() {
		updatedAt = LocalDateTime.now();
	}
	
}
