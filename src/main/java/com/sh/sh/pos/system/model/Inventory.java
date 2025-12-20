package com.sh.sh.pos.system.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Builder

public class Inventory {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne
	private Branch branch;
	
	@ManyToOne
	private Product product;
	
	@Column(nullable = false)
	private Integer quantity;
	
	private LocalDateTime lastUpdate;
	
	@PrePersist
	@PreUpdate
	protected void onUpdate() {
		lastUpdate = LocalDateTime.now();
	}
	
	
	
}
