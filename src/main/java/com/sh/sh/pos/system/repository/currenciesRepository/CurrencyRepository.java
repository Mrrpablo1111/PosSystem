package com.sh.sh.pos.system.repository.currenciesRepository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.currencies.Currency;

import java.util.List;
import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    List<Currency> findByActiveTrue();

    Optional<Currency> findByCode(String code);

    boolean existsByCode(String code);

}