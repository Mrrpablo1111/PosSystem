package com.sh.sh.pos.system.model.stocks;

import java.time.LocalDateTime;
import java.util.List;

import com.sh.sh.pos.system.domain.StockRequestStatus;
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
@Table(name= "stock_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requestNo;

    @ManyToOne
    @JoinColumn(name = "from_branch_id", nullable = false)
    private Branch fromBranch; // warehouse

    @ManyToOne
    @JoinColumn(name = "to_branch_id", nullable = false)
    private Branch toBranch; // store

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StockRequestStatus status = StockRequestStatus.PENDING;

    private String note;

    @ManyToOne
    private User requestedBy;

    @ManyToOne
    private User approvedBy;

    private LocalDateTime approvedAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockRequestItem> items;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (requestNo == null) {
            requestNo = "REQ-" + System.currentTimeMillis();
        }
    }
    
}
