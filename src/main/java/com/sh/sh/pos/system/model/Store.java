package com.sh.sh.pos.system.model;

import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.StoreStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(nullable = false)
	@NotBlank(message = "brand name is required")
	private String brand;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_admin_id", nullable = false)
	private User storeAdmin;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private String description;

	private String storeType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StoreStatus status;

	@Embedded
	private StoreContact contact;

	@PrePersist
	protected void onCreate() {
		 createdAt = LocalDateTime.now();
        if (status == null) {
            status = StoreStatus.PENDING;
        }
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

}
