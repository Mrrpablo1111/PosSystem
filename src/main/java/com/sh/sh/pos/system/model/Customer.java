package com.sh.sh.pos.system.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.sh.sh.pos.system.model.price.PriceGroup;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "fullName is mandatory")
	private String fullName;

	private String email;

	private String phone;

	@ManyToOne
	@JoinColumn(name = "price_group_id")
	private PriceGroup priceGroup;
	@Column(nullable = false, updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;

	@Column(nullable = false)
	@UpdateTimestamp
	private LocalDateTime updatedAt;
}
