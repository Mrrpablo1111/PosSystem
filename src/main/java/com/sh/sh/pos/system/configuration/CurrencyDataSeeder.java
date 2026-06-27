package com.sh.sh.pos.system.configuration;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.sh.sh.pos.system.model.currencies.Currency;
import com.sh.sh.pos.system.repository.currenciesRepository.CurrencyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrencyDataSeeder implements CommandLineRunner {
     private final CurrencyRepository repo;
 
    @Override
    public void run(String... args) {
        if (repo.count() > 0) return; // already seeded
 
        List<Currency> defaults = List.of(
            build("USD", "US Dollar",      "$"),
            build("KHR", "Khmer Riel",     "៛"),
            build("EUR", "Euro",           "€"),
            build("GBP", "British Pound",  "£"),
            build("JPY", "Japanese Yen",   "¥"),
            build("CNY", "Chinese Yuan",   "¥"),
            build("SGD", "Singapore Dollar","S$"),
            build("VND", "Vietnamese Dong","₫"),
            build("MYR", "Malaysian Ringgit","RM")
        );
 
        repo.saveAll(defaults);
        log.info("✅ Seeded {} default currencies", defaults.size());
    }
 
    private Currency build(String code, String name, String symbol) {
        return Currency.builder()
                .code(code)
                .name(name)
                .symbol(symbol)
                .active(true)
                .build();
    }
}
