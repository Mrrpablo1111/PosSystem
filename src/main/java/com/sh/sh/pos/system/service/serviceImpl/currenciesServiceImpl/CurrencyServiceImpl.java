package com.sh.sh.pos.system.service.serviceImpl.currenciesServiceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.mapper.currenciesMapper.CurrencyMapper;
import com.sh.sh.pos.system.model.currencies.Currency;
import com.sh.sh.pos.system.payload.dto.currenciesDTO.CurrencyDTO;
import com.sh.sh.pos.system.payload.request.CurrencyRequestDTO;
import com.sh.sh.pos.system.repository.currenciesRepository.CurrencyRepository;
import com.sh.sh.pos.system.service.currenciesService.CurrencyService;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository repo;
    private final CurrencyMapper     mapper;
 
    // ── Read ──────────────────────────────────────────────────────────────
 
    @Override
    public List<CurrencyDTO> getAll() {
        return repo.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
 
    @Override
    public List<CurrencyDTO> getActive() {
        return repo.findByActiveTrue().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
 
    @Override
    public CurrencyDTO getById(Long id) {
        return mapper.toDTO(findOrThrow(id));
    }
 
    @Override
    public CurrencyDTO getByCode(String code) {
        return repo.findByCode(code.toUpperCase())
                .map(mapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Currency not found: " + code));
    }
 
    // ── Create ────────────────────────────────────────────────────────────
 
    @Override
    @Transactional
    public CurrencyDTO create(CurrencyRequestDTO req) {
        String code = req.getCode().toUpperCase().trim();
        if (repo.existsByCode(code)) {
            throw new RuntimeException("Currency code already exists: " + code);
        }
        return mapper.toDTO(repo.save(mapper.toEntity(req)));
    }
 
    // ── Update ────────────────────────────────────────────────────────────
 
    @Override
    @Transactional
    public CurrencyDTO update(Long id, CurrencyRequestDTO req) {
        Currency c = findOrThrow(id);
 
        String newCode = req.getCode().toUpperCase().trim();
        if (!newCode.equals(c.getCode()) && repo.existsByCode(newCode)) {
            throw new RuntimeException("Currency code already exists: " + newCode);
        }
 
        mapper.updateEntity(c, req);
        return mapper.toDTO(repo.save(c));
    }
 
    // ── Activate / Deactivate ─────────────────────────────────────────────
 
    @Override
    @Transactional
    public CurrencyDTO setActive(Long id, boolean active) {
        Currency c = findOrThrow(id);
        c.setActive(active);
        return mapper.toDTO(repo.save(c));
    }
 
    // ── Delete ────────────────────────────────────────────────────────────
 
    @Override
    @Transactional
    public void delete(Long id) {
        repo.delete(findOrThrow(id));
    }
 
    // ── Private ───────────────────────────────────────────────────────────
 
    private Currency findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Currency not found: " + id));
    }
}
