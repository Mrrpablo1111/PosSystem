package com.sh.sh.pos.system.model.storeMailConfigs;
import com.sh.sh.pos.system.model.Store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "store_mail_configs")
@Getter 
@Setter 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
public class StoreMailConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false, unique = true)
    private Store store;

    /**
     * The Gmail address this store sends FROM.
     * e.g. "mystore@gmail.com"
     * Shown to supplier as the sender.
     */
    @Column(nullable = false)
    private String fromEmail;

    /**
     * Display name shown in the supplier's inbox.
     * e.g. "SH-POS — My Store Name"
     */
    private String fromName;

    /**
     * Gmail App Password (16 chars, no spaces).
     * Go to: Google Account → Security → 2-Step → App Passwords
     * In production: encrypt this column.
     */
   @Column(name = "app_password", nullable = false, columnDefinition = "TEXT")
    private String encryptedAppPassword;

    /** SMTP host — default smtp.gmail.com */
    @Builder.Default
    private String smtpHost = "smtp.gmail.com";

    /** SMTP port — default 587 */
    @Builder.Default
    private Integer smtpPort = 587;

    /** Whether this config is active */
    @Builder.Default
    private Boolean active = true;
}