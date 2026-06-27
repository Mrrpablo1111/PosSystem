package com.sh.sh.pos.system.service.serviceImpl.exchangiesRateServiceImpl;

import java.time.LocalDateTime;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.mapper.exchangiesRateMapper.ExchangeRateMapper;
import com.sh.sh.pos.system.model.currencies.Currency;
import com.sh.sh.pos.system.model.exchangiesRate.ExchangeRate;
import com.sh.sh.pos.system.payload.dto.exchangiesRateDTO.ExchangeRateDTO;
import com.sh.sh.pos.system.repository.currenciesRepository.CurrencyRepository;
import com.sh.sh.pos.system.repository.exchangiesRateRepository.ExchangeRateRepository;
import com.sh.sh.pos.system.service.exchangiesRateService.ExchangeRateService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExchangeRateServiceImpl implements ExchangeRateService {
    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;

    @Override
    public ExchangeRateDTO create(ExchangeRateDTO dto) {

        Currency fromCurrency = currencyRepository.findById(dto.getFromCurrencyId())
                .orElseThrow(() -> new RuntimeException("From currency not found"));

        Currency toCurrency = currencyRepository.findById(dto.getToCurrencyId())
                .orElseThrow(() -> new RuntimeException("To currency not found"));

        ExchangeRate exchangeRate = ExchangeRate.builder()
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .rate(dto.getRate())
                .rateDate(dto.getRateDate())
                .active(true)
                .source(dto.getSource())
                .createdAt(LocalDateTime.now())
                .build();

        return ExchangeRateMapper.toDTO(
                exchangeRateRepository.save(exchangeRate));
    }

    @Override
    public ExchangeRateDTO update(Long id, ExchangeRateDTO dto) {

        ExchangeRate exchangeRate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exchange rate not found"));

        Currency fromCurrency = currencyRepository.findById(dto.getFromCurrencyId())
                .orElseThrow(() -> new RuntimeException("From currency not found"));

        Currency toCurrency = currencyRepository.findById(dto.getToCurrencyId())
                .orElseThrow(() -> new RuntimeException("To currency not found"));

        exchangeRate.setFromCurrency(fromCurrency);
        exchangeRate.setToCurrency(toCurrency);
        exchangeRate.setRate(dto.getRate());
        exchangeRate.setRateDate(dto.getRateDate());
        exchangeRate.setSource(dto.getSource());
        exchangeRate.setUpdatedAt(LocalDateTime.now());

        return ExchangeRateMapper.toDTO(
                exchangeRateRepository.save(exchangeRate));

    }

    @Override
    public ExchangeRateDTO getById(Long id) {

        ExchangeRate exchangeRate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exchange rate not found"));

        return ExchangeRateMapper.toDTO(exchangeRate);

    }

    @Override
    public List<ExchangeRateDTO> getAll(Boolean active) {

        List<ExchangeRate> exchangeRates;

        if (active == null) {
            exchangeRates = exchangeRateRepository.findAll();
        } else {
            exchangeRates = exchangeRateRepository.findByActive(active);
        }

        return ExchangeRateMapper.toDTOList(exchangeRates);

    }

    @Override
    public void activate(Long id) {

        ExchangeRate exchangeRate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exchange rate not found"));

        exchangeRate.setActive(true);

        exchangeRateRepository.save(exchangeRate);

    }

    @Override
    public void deactivate(Long id) {

        ExchangeRate exchangeRate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exchange rate not found"));

        exchangeRate.setActive(false);

        exchangeRateRepository.save(exchangeRate);

    }

    @Override
    public ExchangeRateDTO getLatestRate(
            String fromCurrency,
            String toCurrency) {

        ExchangeRate exchangeRate = exchangeRateRepository
                .findTopByFromCurrency_CodeAndToCurrency_CodeAndActiveTrueOrderByRateDateDesc(
                        fromCurrency,
                        toCurrency)
                .orElseThrow(() -> new RuntimeException("Exchange rate not found"));

        return ExchangeRateMapper.toDTO(exchangeRate);

    }

}
