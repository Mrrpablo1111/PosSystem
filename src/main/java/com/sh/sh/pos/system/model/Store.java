 package com.sh.sh.pos.system.model;

import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.StoreStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class Store {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(nullable = false)
	@NotBlank(message = "brand name is required")
	private String brand;
	
	@OneToOne
	private User storeAdmin;
	
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	private String description;
	
	private String storeType; 
	
	private StoreStatus status;
	
	@Embedded
	private StoreContact contact = new StoreContact(); 
	
	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		status = StoreStatus.PENDDING;
	}
	
	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
	
	
}
