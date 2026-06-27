package com.sh.sh.pos.system.model.exchangiesRate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.sh.sh.pos.system.model.currencies.Currency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "exchange_rates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_currency_id", nullable = false)
    private Currency fromCurrency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_currency_id", nullable = false)
    private Currency toCurrency;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal rate;

    @Column(nullable = false)
    private LocalDate rateDate;

    @Builder.Default
    private Boolean active = true;

    private String source;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
