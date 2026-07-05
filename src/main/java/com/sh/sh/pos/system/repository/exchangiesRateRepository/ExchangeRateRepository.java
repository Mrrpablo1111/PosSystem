package com.sh.sh.pos.system.repository.exchangiesraterepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sh.sh.pos.system.model.exchangiesrate.ExchangeRate;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    List<ExchangeRate> findByActive(Boolean active);

    @Query("""
            SELECT er FROM ExchangeRate er
            WHERE er.fromCurrency.code = :from
              AND er.toCurrency.code = :to
              AND er.active = true
            ORDER BY er.rateDate DESC
            LIMIT 1
            """)
    Optional<ExchangeRate> findLatestActiveRate(@Param("from") String from, @Param("to") String to);
}