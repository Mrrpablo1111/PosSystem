package com.sh.sh.pos.system.repository.exchangiesRateRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.exchangiesRate.ExchangeRate;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    List<ExchangeRate> findByActive(Boolean active);

    Optional<ExchangeRate>
            findTopByFromCurrency_CodeAndToCurrency_CodeAndActiveTrueOrderByRateDateDesc(
                    String from,
                    String to);
}