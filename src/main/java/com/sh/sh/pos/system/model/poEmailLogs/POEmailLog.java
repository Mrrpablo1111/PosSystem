package com.sh.sh.pos.system.model.poEmailLogs;


import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.EmailType;
import com.sh.sh.pos.system.model.purchasesOrder.PurchaseOrder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "po_email_logs")
@Getter
@Setter 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
public class POEmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private PurchaseOrder purchaseOrder;

    /** PURCHASE_ORDER or GOODS_RECEIVED */
    @Enumerated(EnumType.STRING)
    private EmailType emailType;

    private String sentTo;       // supplier email
    private String subject;

    /** SUCCESS or FAILED */
    private String status;

    /** Error message if status = FAILED */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private LocalDateTime sentAt;

    @PrePersist
    public void onCreate() {
        sentAt = LocalDateTime.now();
    }
}