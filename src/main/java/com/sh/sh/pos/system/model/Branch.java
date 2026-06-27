package com.sh.sh.pos.system.model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sh.sh.pos.system.domain.BranchType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "branches")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Branch {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String name;

	private String address;

	private String phone;

	private String email;

	@ElementCollection
	private List<String> workingDays;

	private LocalTime openTime;

	private LocalTime closeTime;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private BranchType branchType = BranchType.STORE;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	@OneToOne(cascade = CascadeType.REMOVE)
	@JsonIgnore
	private User manager;

	@PrePersist
	protected void onCreated() {
		createdAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdated() {
		updatedAt = LocalDateTime.now();
	}

}
