package com.sh.sh.pos.system.model.store;

import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.StoreStatus;
import com.sh.sh.pos.system.model.storecontact.StoreContact;
import com.sh.sh.pos.system.model.user.User;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class Store {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	@EqualsAndHashCode.Include
	private Long id;

	@NotBlank(message = "brand name is required")
	@Size(max = 150, message = "Brand name cannot exceed 150 character")
	@Column(name = "brand", nullable = false, length = 150)
	private String brand;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_admin_id", nullable = false, unique = true)
	private User storeAdmin;

	@Column(name="created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name="updated_at")
	private LocalDateTime updatedAt;

	@Size(max = 1000, message = "Description cannot exceed 1000 characters")
	@Column(name = "description", length = 1000)
	private String description;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "store_type_id", nullable = false)
	private StoreType storeType;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	@Builder.Default
	private StoreStatus status = StoreStatus.PENDING;

	@Embedded
	private StoreContact contact;

	@PrePersist
	protected void onCreate() {
		LocalDateTime now = LocalDateTime.now();

		if (createdAt == null) {
			createdAt = now;
		}

		if (status == null) {
			status = StoreStatus.PENDING;
		}

		updatedAt = null;
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

}
