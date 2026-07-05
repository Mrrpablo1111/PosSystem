package com.sh.sh.pos.system.service.serviceimpl.exchangiesrateserviceimpl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.mapper.exchangesratemapper.ExchangeRateMapper;
import com.sh.sh.pos.system.model.currencies.Currency;
import com.sh.sh.pos.system.model.exchangiesrate.ExchangeRate;
import com.sh.sh.pos.system.payload.dto.exchangiesratedto.ExchangeRateDTO;
import com.sh.sh.pos.system.repository.currenciesrepository.CurrencyRepository;
import com.sh.sh.pos.system.repository.exchangiesraterepository.ExchangeRateRepository;
import com.sh.sh.pos.system.service.exchangiesrateservice.ExchangeRateService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;

    @Override
    @Transactional
    public ExchangeRateDTO create(ExchangeRateDTO dto) {

        Currency fromCurrency = findCurrency(dto.getFromCurrencyId(), "From");
        Currency toCurrency = findCurrency(dto.getToCurrencyId(), "To");

        ExchangeRate exchangeRate = ExchangeRate.builder()
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .rate(dto.getRate())
                .rateDate(dto.getRateDate())
                .active(true)
                .source(dto.getSource())
                .createdAt(LocalDateTime.now())
                .build();

        return ExchangeRateMapper.toDTO(exchangeRateRepository.save(exchangeRate));
    }

    @Override
    @Transactional
    public ExchangeRateDTO update(Long id, ExchangeRateDTO dto) {

        ExchangeRate exchangeRate = findExchangeRate(id);
        Currency fromCurrency = findCurrency(dto.getFromCurrencyId(), "From");
        Currency toCurrency = findCurrency(dto.getToCurrencyId(), "To");

        exchangeRate.setFromCurrency(fromCurrency);
        exchangeRate.setToCurrency(toCurrency);
        exchangeRate.setRate(dto.getRate());
        exchangeRate.setRateDate(dto.getRateDate());
        exchangeRate.setSource(dto.getSource());
        exchangeRate.setUpdatedAt(LocalDateTime.now());

        return ExchangeRateMapper.toDTO(exchangeRateRepository.save(exchangeRate));
    }

    @Override
    @Transactional(readOnly = true)
    public ExchangeRateDTO getById(Long id) {
        return ExchangeRateMapper.toDTO(findExchangeRate(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangeRateDTO> getAll(Boolean active) {

        List<ExchangeRate> exchangeRates = (active == null)
                ? exchangeRateRepository.findAll()
                : exchangeRateRepository.findByActive(active);

        return ExchangeRateMapper.toDTOList(exchangeRates);
    }

    @Override
    @Transactional
    public void activate(Long id) {
        setActiveStatus(id, true);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        setActiveStatus(id, false);
    }

    @Override
    @Transactional(readOnly = true)
    public ExchangeRateDTO getLatestRate(String fromCurrency, String toCurrency) {

        ExchangeRate exchangeRate = exchangeRateRepository
                .findLatestActiveRate(fromCurrency, toCurrency)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active exchange rate not found from " + fromCurrency + " to " + toCurrency));

        return ExchangeRateMapper.toDTO(exchangeRate);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private ExchangeRate findExchangeRate(Long id) {
        return exchangeRateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exchange rate not found with id: " + id));
    }

    private Currency findCurrency(Long id, String label) {
        return currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        label + " currency not found with id: " + id));
    }

    private void setActiveStatus(Long id, boolean active) {
        ExchangeRate exchangeRate = findExchangeRate(id);
        exchangeRate.setActive(active);
        exchangeRateRepository.save(exchangeRate);
    }
}