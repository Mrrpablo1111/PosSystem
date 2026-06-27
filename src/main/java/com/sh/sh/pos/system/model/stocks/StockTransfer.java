package com.sh.sh.pos.system.model.stocks;

import java.time.LocalDateTime;
import java.util.List;

import com.sh.sh.pos.system.domain.StockTransferStatus;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.User;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stock_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transferNo;

    @ManyToOne
    private StockRequest request;

    @ManyToOne
    @JoinColumn(name = "from_branch_id", nullable = false)
    private Branch fromBranch;

    @ManyToOne
    @JoinColumn(name = "to_branch_id", nullable = false)
    private Branch toBranch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StockTransferStatus status = StockTransferStatus.DRAFT;

    private String carrier;
    private String trackingNo;
    private String vehicleNo;
    private String driverName;
    private String driverPhone;

    private LocalDateTime shippedAt;
    private LocalDateTime receivedAt;
    private String note;

    @ManyToOne
    private User shippedBy;

    @ManyToOne
    private User receivedBy;

    @OneToMany(mappedBy = "transfer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockTransferItem> items;

    @OneToMany(mappedBy = "transfer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockTransferHistory> histories;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

   @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (transferNo == null) {
            transferNo = "TRF-" + System.currentTimeMillis();
        }
    }
}
