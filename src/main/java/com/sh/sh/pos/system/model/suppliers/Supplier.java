package com.sh.sh.pos.system.model.suppliers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.SupplierStatus;
import com.sh.sh.pos.system.model.Store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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


@Entity
@Table(name = "suppliers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ERP UNIQUE CODE
    @Column(unique = true, nullable = false)
    private String code; // SUP-0001

    private String name;

    private String phone;

    private String email;

    private String address;

    private String taxNumber;

    private String contactPerson;

    private String country;

    // PAYMENT CONTROL (IMPORTANT ERP FIELD)
    private Integer paymentTermDays; // e.g. 30, 60, 90 days

    private BigDecimal creditLimit;

    private BigDecimal openingBalance;

    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    // STATUS CONTROL
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SupplierStatus status = SupplierStatus.ACTIVE;
    

    // RELATION
    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // AUDIT
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
